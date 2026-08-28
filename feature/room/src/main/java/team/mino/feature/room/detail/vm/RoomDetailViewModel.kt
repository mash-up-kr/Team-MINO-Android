package team.mino.feature.room.detail.vm

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.core.common.android.extension.launchSafely
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.Place
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.model.Room
import team.mino.core.domain.repository.PlaceRepository
import team.mino.core.domain.repository.RoomRepository
import team.mino.core.domain.usecase.EnsureAnonymousSessionUseCase
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.MinoDomainException
import team.mino.core.errorhandling.domainErrorEmitter
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import team.mino.core.navigation.activity.launcher.RoomFormLauncher
import team.mino.feature.room.detail.model.PlaceViewType
import team.mino.feature.room.main.component.DefaultMapCenter
import team.mino.feature.room.main.model.BottomSheetLevel
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

/**
 * 방 상세의 유일한 화면(`RoomListScreen`이 `RoomMain` 안에서 로컬 상태로 그리는 상세 모드)의 ViewModel.
 *
 * 계약: docs/specs/room-detail/contracts/room-detail-main-contract.md
 *
 * `roomId`는 더 이상 별도 Navigation 목적지의 route 인자가 아니다 — 방 상세가 `RoomDetailMain`이라는
 * 자기 목적지를 갖지 않고 `RoomMain` 안에서 로컬 상태로 전환되므로(`RoomNavigation.kt` KDoc 참고),
 * `SavedStateHandle.toRoute()` 대신 [RoomDetailRoute]가 `@AssistedInject`로 직접 넘겨준다.
 */
@HiltViewModel(assistedFactory = RoomDetailViewModel.Factory::class)
internal class RoomDetailViewModel @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    @Assisted private val roomId: String,
    private val roomRepository: RoomRepository,
    private val placeRepository: PlaceRepository,
    private val ensureAnonymousSessionUseCase: EnsureAnonymousSessionUseCase,
    val roomFormLauncher: RoomFormLauncher,
) : ViewModel(),
    MviContainer<RoomDetailUiState, RoomDetailSideEffect> by mviContainer(RoomDetailUiState()),
    DomainErrorEmitter by domainErrorEmitter() {
    @AssistedFactory
    internal interface Factory {
        fun create(roomId: String): RoomDetailViewModel
    }

    /**
     * [FR-011] `observePlaces` 재구독 시 정렬·필터가 되돌아가지 않도록 서버 원본 순서를 별도로 들고
     * 있는다 — [RoomDetailUiState.places]는 이 값에서 파생된 정렬·필터 결과만 노출한다.
     */
    private var rawPlaces: List<Place> = emptyList()

    fun processIntent(intent: RoomDetailIntent) {
        when (intent) {
            RoomDetailIntent.OnScreenEntered -> onScreenEntered()
            RoomDetailIntent.OnSheetDraggedUp -> onSheetDraggedUp()
            RoomDetailIntent.OnSheetDraggedDown -> onSheetDraggedDown()
            is RoomDetailIntent.OnSortSelected -> onSortSelected(intent.option)
            is RoomDetailIntent.OnCategoryFilterSelected -> onCategoryFilterSelected(intent.category)
            is RoomDetailIntent.OnViewTypeSelected -> onViewTypeSelected(intent.viewType)
            RoomDetailIntent.OnCloseClick -> onCloseClick()
            RoomDetailIntent.OnPlaceClick -> Unit
            is RoomDetailIntent.OnPlaceMoreClick -> onPlaceMoreClick(intent.place)
            RoomDetailIntent.OnPlaceMoreDismiss -> onPlaceMoreDismiss()
            is RoomDetailIntent.OnShareToOtherRoomClick -> onShareToOtherRoomClick(intent.place)
            is RoomDetailIntent.OnPlaceDeleteClick -> onPlaceDeleteClick(intent.place)
            RoomDetailIntent.OnPlaceDeleteConfirm -> onPlaceDeleteConfirm()
            RoomDetailIntent.OnPlaceDeleteCancel -> onPlaceDeleteCancel()
            is RoomDetailIntent.OnRoomSelectConfirm -> onRoomSelectConfirm(intent.targetRoomIds)
            RoomDetailIntent.OnRoomSelectDismiss -> onRoomSelectDismiss()
            RoomDetailIntent.OnShareCreateRoomClick -> onShareCreateRoomClick()
            is RoomDetailIntent.OnShareRoomFormResult -> onShareRoomFormResult(intent.createdRoomId)
            RoomDetailIntent.OnMoreMenuClick -> onMoreMenuClick()
            RoomDetailIntent.OnMoreMenuDismiss -> onMoreMenuDismiss()
            RoomDetailIntent.OnInviteClick -> onInviteClick()
            RoomDetailIntent.OnInviteSheetDismiss -> onInviteSheetDismiss()
            RoomDetailIntent.OnEditRoomClick -> onEditRoomClick()
            RoomDetailIntent.OnLeaveClick -> onLeaveClick()
            RoomDetailIntent.OnLeaveConfirm -> onLeaveConfirm()
            RoomDetailIntent.OnLeaveCancel -> onLeaveCancel()
            is RoomDetailIntent.OnOwnerDelegateSelected -> onOwnerDelegateSelected(intent.memberId)
            RoomDetailIntent.OnOwnerDelegateConfirm -> onOwnerDelegateConfirm()
            is RoomDetailIntent.OnRoomFormResult -> onRoomFormResult(intent.updated)
            RoomDetailIntent.OnCurrentLocationClick -> onCurrentLocationClick()
            is RoomDetailIntent.OnLocationPermissionResult -> onLocationPermissionResult(intent.granted)
        }
    }

    /**
     * [FR-001] 진입 조회 — `RoomRepository.getRoom` 단건 조회와 `PlaceRepository.observePlaces` 구독을
     * 함께 시작한다. `isOwner`는 [EnsureAnonymousSessionUseCase]로 확보한 현재 세션의 `userId`를
     * [Room.ownerId]와 비교해 판정한다(ADR 2026-08-22 익명 인증 — 이 앱의 유일한 사용자 식별 수단).
     *
     * 새 emit마다 현재 [RoomDetailUiState.sortOption]·[RoomDetailUiState.categoryFilter]를 다시
     * 적용한다 — 그렇지 않으면 재구독 결과가 사용자가 고른 정렬·필터를 되돌린다.
     */
    private fun onScreenEntered() {
        launchSafely { loadRoom() }
        if (hasLocationPermission()) {
            launchSafely {
                val center = resolveMapCenter(granted = true)
                updateState { copy(mapCenter = center, mapCenterRequestId = mapCenterRequestId + 1) }
            }
        } else {
            launchSafely { postSideEffect(RoomDetailSideEffect.RequestLocationPermission) }
        }
        launchSafely {
            placeRepository
                .observePlaces(roomId)
                .catch { throwable ->
                    if (throwable is MinoDomainException) {
                        updateState { copy(loadError = throwable) }
                    } else {
                        throw throwable
                    }
                }.collect { places ->
                    rawPlaces = places
                    updateState { copy(places = filteredAndSorted()) }
                }
        }
    }

    /**
     * [rawPlaces]에 현재(또는 인자로 넘긴) 정렬·필터를 적용한 목록. 필터·정렬·삭제 각 분기가 같은 파생
     * 규칙을 공유하므로 한 곳에 둔다 — 갈리면 한쪽만 규칙이 바뀌는 사고가 난다.
     */
    private fun filteredAndSorted(
        category: PlaceCategoryFilter = state.value.categoryFilter,
        sort: MapMarkerSortOption = state.value.sortOption,
    ): ImmutableList<Place> = rawPlaces.filterByCategory(category).sortedByOption(sort).toImmutableList()

    /** [FR-011] 정렬 옵션 변경 — 현재 [RoomDetailUiState.categoryFilter]를 유지한 채 [rawPlaces]를 재정렬한다. */
    private fun onSortSelected(option: MapMarkerSortOption) {
        updateState { copy(sortOption = option, places = filteredAndSorted(sort = option)) }
    }

    /**
     * [FR-011][EC-003] 카테고리 필터 변경 — 현재 [RoomDetailUiState.sortOption]을 유지한 채 [rawPlaces]를
     * 재필터링한다. 해당 카테고리 장소가 없으면 빈 목록을 그대로 반영한다.
     */
    private fun onCategoryFilterSelected(category: PlaceCategoryFilter) {
        updateState { copy(categoryFilter = category, places = filteredAndSorted(category = category)) }
    }

    /** [FR-007] 장소 리스트형/카드형 뷰 토글. */
    private fun onViewTypeSelected(viewType: PlaceViewType) {
        updateState { copy(viewType = viewType) }
    }

    private suspend fun loadRoom() {
        runCatchingDomain { roomRepository.getRoom(roomId) }
            .onSuccess { room ->
                val currentUserId = ensureAnonymousSessionUseCase().userId
                updateState {
                    copy(
                        room = room,
                        isOwner = room.ownerId == currentUserId,
                        isPersonalRoom = room.isPersonal,
                    )
                }
            }.onDomainFailure { updateState { copy(loadError = it) } }
    }

    /** [contracts/room-detail-main-contract.md 「분기 규칙 — 시트 드래그 전이」] room-list와 동일 패턴. */
    private fun onSheetDraggedUp() {
        updateState {
            when (sheetLevel) {
                BottomSheetLevel.PEEK -> copy(sheetLevel = BottomSheetLevel.HALF)
                BottomSheetLevel.HALF -> copy(sheetLevel = BottomSheetLevel.FULL)
                BottomSheetLevel.FULL -> this
            }
        }
    }

    private fun onSheetDraggedDown() {
        updateState {
            when (sheetLevel) {
                BottomSheetLevel.PEEK -> this
                BottomSheetLevel.HALF -> copy(sheetLevel = BottomSheetLevel.PEEK)
                BottomSheetLevel.FULL -> copy(sheetLevel = BottomSheetLevel.HALF)
            }
        }
    }

    /** [X] 닫기 — 전환 결정만 발행한다(실제 팝백은 Route가 수행, T032). */
    private fun onCloseClick() {
        launchSafely { postSideEffect(RoomDetailSideEffect.NavigateBack) }
    }

    /** [FR-008] 장소 카드 더보기[⋮] — 대상 장소를 기록해 PlaceActionMenu가 소비하게 한다. */
    private fun onPlaceMoreClick(place: Place) {
        updateState { copy(menuTargetPlace = place) }
    }

    /** [FR-008] 더보기[⋮] 메뉴 바깥을 눌러 닫기 — 열려 있던 대상 장소 기록을 지운다. */
    private fun onPlaceMoreDismiss() {
        updateState { copy(menuTargetPlace = null) }
    }

    /**
     * [FR-009] 다른 방에 공유 — 공유 대상 장소를 기록하고 시트를 연다.
     *
     * 시트가 열릴 때마다 매번 [RoomRepository.observeMyRooms]를 구독한다 — 화면 진입 시점부터 미리
     * 구독해 둘 이유가 없고(시트가 열리기 전엔 목록이 필요 없다), 열릴 때마다 최신 방 목록을 받는 편이
     * 오래 열어둔 화면에서 새로 만든 방이 후보에서 빠지는 사고를 막는다.
     */
    private fun onShareToOtherRoomClick(place: Place) {
        // menuTargetPlace를 같이 지워야 카드 더보기 메뉴([PlaceActionMenu])가 닫힌다 — 이걸 빼먹으면
        // 그 메뉴가 계속 열린 채로 이 공유 시트 위/아래에 겹쳐 보인다(실기기 확인).
        updateState { copy(showRoomSelectSheet = true, placeToShare = place, menuTargetPlace = null) }
        launchSafely {
            roomRepository
                .observeMyRooms()
                .catch { throwable ->
                    if (throwable is MinoDomainException) {
                        emitDomainError(throwable)
                    } else {
                        throw throwable
                    }
                }.collect { rooms ->
                    updateState { copy(myRooms = rooms.toImmutableList()) }
                }
        }
    }

    /**
     * [FR-009] 방 선택 확정 — 선택한 방들에 장소를 공유한다.
     *
     * 일회성 사용자 액션 실패이므로 [loadRoom]과 달리 `loadError`가 아니라 [DomainErrorEmitter]로
     * 방출한다(`409 DUPLICATE_PIN_IN_ROOM` 포함 모든 실패, `docs/conventions/error_handling.md` §5).
     */
    private fun onRoomSelectConfirm(targetRoomIds: ImmutableList<String>) {
        val place = state.value.placeToShare ?: return
        launchSafely {
            runCatchingDomain { placeRepository.sharePlaces(place.id, targetRoomIds) }
                .onSuccess {
                    postSideEffect(RoomDetailSideEffect.ShowShareCompleteToast)
                    updateState { copy(showRoomSelectSheet = false, placeToShare = null) }
                }.onDomainFailure(::emitDomainError)
        }
    }

    /** [FR-009] 방 선택 시트 닫기 — 공유 관련 상태를 초기화한다. */
    private fun onRoomSelectDismiss() {
        updateState { copy(showRoomSelectSheet = false, placeToShare = null) }
    }

    /** [FR-009] 공유 시트의 [+ 새 방 만들기] — 전환 결정만 발행한다(실제 호출은 Route, `OnEditRoomClick`과 같은 패턴). */
    private fun onShareCreateRoomClick() {
        launchSafely { postSideEffect(RoomDetailSideEffect.NavigateToCreateRoomForm) }
    }

    /**
     * [FR-009] 공유 시트에서 새로 만든 방을 [RoomDetailUiState.myRooms] 목록에 더한다 — 시트를 다시 열지
     * 않아도 방금 만든 방이 바로 보여야 공유 대상으로 고를 수 있다. `observeMyRooms()`는 한 번만 emit하는
     * 콜드 플로우라 자동으로 갱신되지 않는다.
     */
    private fun onShareRoomFormResult(createdRoomId: String?) {
        val id = createdRoomId ?: return
        launchSafely {
            runCatchingDomain { roomRepository.getRoom(id) }
                .onSuccess { room -> updateState { copy(myRooms = (myRooms + room).toImmutableList()) } }
                .onDomainFailure(::emitDomainError)
        }
    }

    /**
     * [FR-010] 장소 삭제 확인 다이얼로그를 띄우기 위해 대상 장소를 기록한다. [onShareToOtherRoomClick]과
     * 같은 이유로 `menuTargetPlace`도 같이 지운다 — 안 그러면 카드 더보기 메뉴가 확인 다이얼로그 뒤에
     * 계속 열려 있는다.
     */
    private fun onPlaceDeleteClick(place: Place) {
        updateState { copy(placeToDelete = place, menuTargetPlace = null) }
    }

    /**
     * [FR-010] 장소 삭제 확정 — 성공하면 [rawPlaces]에서 즉시 제거하고 현재 정렬·필터를 다시 적용한다.
     * `deletePlace`는 대응 엔드포인트가 없어 현재는 no-op이지만(`docs/specs/room-detail/research.md`
     * D14), 서버 확정 시 자동으로 동작하도록 정상 배선해 둔다.
     */
    private fun onPlaceDeleteConfirm() {
        val place = state.value.placeToDelete ?: return
        launchSafely {
            runCatchingDomain { placeRepository.deletePlace(roomId, place.id) }
                .onSuccess {
                    rawPlaces = rawPlaces.filterNot { it.id == place.id }
                    updateState { copy(placeToDelete = null, places = filteredAndSorted()) }
                }.onDomainFailure(::emitDomainError)
        }
    }

    /** [FR-010] 삭제 취소 — 확인 다이얼로그를 닫는다. */
    private fun onPlaceDeleteCancel() {
        updateState { copy(placeToDelete = null) }
    }

    /** [FR-012] 더보기[⋮] 메뉴 열기. */
    private fun onMoreMenuClick() {
        updateState { copy(showMoreMenu = true) }
    }

    /** [FR-012] 더보기[⋮] 메뉴 바깥을 눌러 닫기. */
    private fun onMoreMenuDismiss() {
        updateState { copy(showMoreMenu = false) }
    }

    /**
     * [FR-013] 초대 시트 열기 — 참여자 목록과 초대 코드를 각각 조회한다.
     *
     * 일회성 사용자 액션 실패이므로 [DomainErrorEmitter]로 방출한다.
     */
    private fun onInviteClick() {
        updateState { copy(showInviteSheet = true) }
        launchSafely {
            runCatchingDomain { roomRepository.getMembers(roomId) }
                .onSuccess { members -> updateState { copy(roomMembers = members.toImmutableList()) } }
                .onDomainFailure(::emitDomainError)
        }
        launchSafely {
            runCatchingDomain { roomRepository.createInvitation(roomId) }
                .onSuccess { code -> updateState { copy(inviteCode = code) } }
                .onDomainFailure(::emitDomainError)
        }
    }

    /** [FR-013] 초대 시트 닫기 — 다음에 열 때 재조회하도록 관련 상태를 초기화한다. */
    private fun onInviteSheetDismiss() {
        updateState { copy(showInviteSheet = false, inviteCode = null, roomMembers = persistentListOf()) }
    }

    /** [FR-012] 방 편집 — 전환 결정만 발행한다(실제 launch 호출은 Route가 수행, T054). */
    private fun onEditRoomClick() {
        launchSafely { postSideEffect(RoomDetailSideEffect.NavigateToRoomForm) }
    }

    /**
     * [FR-012] `RoomFormLauncher` 편집 모드 결과 수신(T054) — `updated`가 아니면(취소 등) 아무 것도
     * 하지 않는다. `updated`면 방 정보를 다시 조회해 헤더를 갱신하고 완료 스낵바를 띄운다
     * (`docs/specs/group-room-form/contracts/room-form-launcher.md` §3).
     */
    private fun onRoomFormResult(updated: Boolean) {
        if (!updated) return
        launchSafely {
            loadRoom()
            postSideEffect(RoomDetailSideEffect.ShowEditCompleteSnackbar)
        }
    }

    /**
     * [SYS-007] 나가기 시작 — 방 멤버 수를 사전에 세지 않는다([contracts/room-detail-main-contract.md]
     * "분기 규칙 — 나가기 플로우"). `isOwner` 여부로 확인 모달만 고른다.
     */
    private fun onLeaveClick() {
        updateState {
            copy(
                leaveDialogState =
                    if (isOwner) LeaveDialogState.ConfirmOwnerSingle else LeaveDialogState.ConfirmMember,
            )
        }
    }

    /**
     * [SYS-007] 나가기 확정 — `leaveRoom` 호출 자체의 성공/`409` 응답으로 서버가 판정한 결과를
     * 그대로 분기에 반영한다. `409 OWNER_TRANSFER_REQUIRED`면 위임 대상 선택으로 전이한다.
     */
    private fun onLeaveConfirm() {
        launchSafely {
            runCatchingDomain { roomRepository.leaveRoom(roomId) }
                .onSuccess { postSideEffect(RoomDetailSideEffect.NavigateToRoomList) }
                .onDomainFailure { throwable ->
                    if (throwable is MinoDomainException.Http && throwable.code == 409) {
                        updateState { copy(leaveDialogState = LeaveDialogState.DelegateOwner) }
                        runCatchingDomain { roomRepository.getMembers(roomId) }
                            .onSuccess { members -> updateState { copy(roomMembers = members.toImmutableList()) } }
                            .onDomainFailure(::emitDomainError)
                    } else {
                        emitDomainError(throwable)
                    }
                }
        }
    }

    /** [SYS-007] 나가기 취소 — 확인 다이얼로그를 닫는다. */
    private fun onLeaveCancel() {
        updateState { copy(leaveDialogState = LeaveDialogState.None) }
    }

    /** [SYS-007] 방장 위임 대상 선택 — 목록에서 고른 대상을 상태에 기록한다. */
    private fun onOwnerDelegateSelected(memberId: String) {
        updateState { copy(selectedDelegateMemberId = memberId) }
    }

    /**
     * [SYS-007] 방장 위임 확정 — `transferOwner` 성공 후 이어서 `leaveRoom`을 호출한다(계약 명시 순서).
     */
    private fun onOwnerDelegateConfirm() {
        val nextOwnerId = state.value.selectedDelegateMemberId ?: return
        launchSafely {
            runCatchingDomain { roomRepository.transferOwner(roomId, nextOwnerId) }
                .onSuccess {
                    runCatchingDomain { roomRepository.leaveRoom(roomId) }
                        .onSuccess { postSideEffect(RoomDetailSideEffect.NavigateToRoomList) }
                        .onDomainFailure(::emitDomainError)
                }.onDomainFailure(::emitDomainError)
        }
    }

    /** [research.md D10] 현재 위치 버튼 최소 구현 — room-list와 같은 동작으로 `mapCenter`만 갱신한다. */
    private fun onCurrentLocationClick() {
        if (!hasLocationPermission()) return
        launchSafely {
            val center = resolveMapCenter(granted = true)
            updateState { copy(mapCenter = center, mapCenterRequestId = mapCenterRequestId + 1) }
        }
    }

    /** [EC-002] 거부 시 기본 디폴트 좌표, 허용 시 실제 위치로 `mapCenter`를 설정한다. */
    private fun onLocationPermissionResult(granted: Boolean) {
        launchSafely {
            val center = resolveMapCenter(granted)
            updateState { copy(mapCenter = center, mapCenterRequestId = mapCenterRequestId + 1) }
        }
    }

    /** 거부 시 기본 디폴트 좌표, 허용 시 실제 위치로 해석한다(EC-002). room-list와 같은 규칙. */
    private suspend fun resolveMapCenter(granted: Boolean): GeoPoint =
        if (granted) currentDeviceLocation() ?: DefaultMapCenter else DefaultMapCenter

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** [RoomListViewModel.currentDeviceLocation]과 같은 이유·같은 구현 — `:core:map`에 위치 조회 인프라가 없다. */
    @SuppressLint("MissingPermission")
    private suspend fun currentDeviceLocation(): GeoPoint? {
        val locationManager = context.getSystemService(LocationManager::class.java) ?: return null
        val cached = locationManager.allProviders
            .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
        if (cached != null) return cached.toGeoPoint()

        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return null
        }
        return withTimeoutOrNull(LOCATION_FETCH_TIMEOUT) { requestSingleLocationUpdate(locationManager, provider) }
    }

    private suspend fun requestSingleLocationUpdate(
        locationManager: LocationManager,
        provider: String,
    ): GeoPoint? =
        suspendCancellableCoroutine { continuation ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    if (continuation.isActive) continuation.resume(location.toGeoPoint())
                }
            }
            continuation.invokeOnCancellation { locationManager.removeUpdates(listener) }
            runCatching {
                locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            }.onFailure {
                if (continuation.isActive) continuation.resume(null)
            }
        }

    private fun Location.toGeoPoint(): GeoPoint = GeoPoint(latitude = latitude, longitude = longitude)

    private companion object {
        val LOCATION_FETCH_TIMEOUT = 10.seconds
    }
}

/** [PlaceCategoryFilter.ALL]은 원래 목록을 그대로 유지한다(별도 필터 없음). */
private fun List<Place>.filterByCategory(category: PlaceCategoryFilter): List<Place> =
    when (category) {
        PlaceCategoryFilter.ALL -> this
        else -> filter { it.category == category }
    }

/**
 * [MapMarkerSortOption.ALL]은 서버가 내려준 원래 순서를 유지한다(별도 정렬 기준 없음).
 * [MapMarkerSortOption.NEARBY]에서 [Place.distanceMeters]가 `null`인 장소는 가장 뒤로 보낸다.
 */
@OptIn(kotlin.time.ExperimentalTime::class)
private fun List<Place>.sortedByOption(option: MapMarkerSortOption): List<Place> =
    when (option) {
        MapMarkerSortOption.ALL -> this
        MapMarkerSortOption.GGUK_PICK -> sortedByDescending { it.isGgukPick }
        MapMarkerSortOption.LATEST -> sortedByDescending { it.savedAt }
        MapMarkerSortOption.NEARBY -> sortedBy { it.distanceMeters ?: Double.MAX_VALUE }
        MapMarkerSortOption.MOST_COMMENTED -> sortedByDescending { it.commentCount }
    }
