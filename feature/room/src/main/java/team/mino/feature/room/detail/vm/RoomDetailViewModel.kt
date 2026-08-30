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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.core.common.android.extension.launchSafely
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.invite.InviteLinkBuilder
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.Place
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomMemberSummary
import team.mino.core.domain.repository.ProfileRepository
import team.mino.core.domain.repository.RoomPlacesRepository
import team.mino.core.domain.repository.RoomRepository
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.MinoDomainException
import team.mino.core.errorhandling.domainErrorEmitter
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import team.mino.core.navigation.activity.launcher.RoomFormLauncher
import team.mino.feature.room.detail.model.PlaceViewType
import team.mino.feature.room.main.component.DefaultMapCenter
import team.mino.feature.room.main.model.BottomSheetLevel
import team.mino.feature.room.main.model.toMemberSummary
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
    private val roomPlacesRepository: RoomPlacesRepository,
    private val profileRepository: ProfileRepository,
    private val inviteLinkBuilder: InviteLinkBuilder,
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

    /**
     * [loadRoomMembers]가 조회한 실제 멤버 아바타. [loadRoom]과 서로 다른 왕복이라 어느 쪽이 먼저
     * 끝날지 보장이 없다 — [loadRoomMembers]가 먼저 끝나면 아직 `room`이 `null`이라 그 자리에 바로
     * 못 얹으므로 여기 들고 있다가, [loadRoom]이 방을 채울 때 함께 적용한다.
     */
    private var pendingMemberSummary: RoomMemberSummary? = null

    fun processIntent(intent: RoomDetailIntent) {
        when (intent) {
            RoomDetailIntent.OnScreenEntered -> onScreenEntered()
            RoomDetailIntent.OnScreenExited -> onScreenExited()
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
            RoomDetailIntent.OnInviteConfirmClick -> onInviteConfirmClick()
            RoomDetailIntent.OnCopyInviteLinkClick -> onCopyInviteLinkClick()
            RoomDetailIntent.OnEditRoomClick -> onEditRoomClick()
            RoomDetailIntent.OnLeaveClick -> onLeaveClick()
            RoomDetailIntent.OnLeaveConfirm -> onLeaveConfirm()
            RoomDetailIntent.OnLeaveCancel -> onLeaveCancel()
            is RoomDetailIntent.OnOwnerDelegateSelected -> onOwnerDelegateSelected(intent.memberId)
            RoomDetailIntent.OnOwnerDelegateConfirm -> onOwnerDelegateConfirm()
            is RoomDetailIntent.OnRoomFormResult -> onRoomFormResult(intent.updated)
            is RoomDetailIntent.OnLocationPermissionResult -> onLocationPermissionResult(intent.granted)
        }
    }

    /**
     * [FR-001] 진입 조회 — `RoomRepository.getRoom` 단건 조회와 `RoomPlacesRepository.observePlaces` 구독을
     * 함께 시작한다. `isOwner`는 [ProfileRepository.currentUserId]로 얻은 서버 user id를
     * [Room.ownerId]와 비교해 판정한다 — Firebase 익명 로그인 uid와는 다른 식별자라 그걸로 비교하면
     * 항상 불일치한다(실기기 확인된 결함).
     *
     * 새 emit마다 현재 [RoomDetailUiState.sortOption]·[RoomDetailUiState.categoryFilter]를 다시
     * 적용한다 — 그렇지 않으면 재구독 결과가 사용자가 고른 정렬·필터를 되돌린다.
     */
    private fun onScreenEntered() {
        launchSafely { loadRoom() }
        launchSafely { loadRoomMembers() }
        if (hasLocationPermission()) {
            launchSafely {
                val center = resolveMapCenter(granted = true)
                updateState { copy(mapCenter = center, mapCenterRequestId = mapCenterRequestId + 1) }
            }
        } else {
            launchSafely { postSideEffect(RoomDetailSideEffect.RequestLocationPermission) }
        }
        launchSafely {
            roomPlacesRepository
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
     * 방 리스트로 복귀할 때 이 화면 전용 오버레이 상태를 지운다 — 이 인스턴스가 같은 `roomId`로 다시
     * 열릴 때 재사용되므로([RoomDetailIntent.OnScreenExited] KDoc), 여기서 지우지 않으면 예전에 열어
     * 두고 안 닫은 "다른 방에 공유" 시트가 다음 진입에 그대로 다시 뜬다.
     *
     * [leaveDialogState]·[selectedDelegateMemberId]도 같은 이유로 지운다 — 실기기 확인된 결함:
     * 위임 대상 선택 화면([LeaveDialogState.DelegateOwner])까지 열어 두고 확정하지 않은 채(뒤로가기 등)
     * 이 화면을 벗어나면, 다음 재진입에 그 선택 화면이 그대로 다시 뜬다.
     */
    private fun onScreenExited() {
        updateState {
            copy(
                showRoomSelectSheet = false,
                placeToShare = null,
                leaveDialogState = LeaveDialogState.None,
                selectedDelegateMemberId = null,
            )
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

    // 방 조회와 내 user id 조회는 서로 결과를 기다릴 필요가 없어 async로 함께 시작한다 — 순서대로
    // await하면 방 조회가 끝날 때까지 user id 요청이 시작조차 되지 않아 왕복이 그대로 더해진다.
    @OptIn(kotlin.time.ExperimentalTime::class)
    private suspend fun loadRoom() =
        coroutineScope {
            val currentUserIdDeferred = async { profileRepository.currentUserId() }
            runCatchingDomain { roomRepository.getRoom(roomId) }
                .onSuccess { room ->
                    val currentUserId = currentUserIdDeferred.await()
                    // 단건 조회 응답에는 멤버 아바타가 없다([RoomMapper.RoomResponse.toDomain]) —
                    // loadRoomMembers가 먼저 끝나 이미 실제 값을 구해 뒀다면 여기서 덮어쓰지 않고 이어받는다.
                    val roomWithMembers = pendingMemberSummary?.let { room.copy(memberSummary = it) } ?: room
                    updateState {
                        copy(
                            room = roomWithMembers,
                            isOwner = room.ownerId == currentUserId,
                            isPersonalRoom = room.isPersonal,
                        )
                    }
                }.onDomainFailure { updateState { copy(loadError = it) } }
        }

    /**
     * 헤더 첫 줄의 참여자 아바타 그룹이 보여줄 실제 멤버를 조회한다(`GET /rooms/{roomId}/members`).
     * [loadRoom]과 서로 다른 왕복이라 어느 쪽이 먼저 끝날지 보장이 없다 — [pendingMemberSummary] KDoc 참고.
     *
     * 실패해도 화면을 막지 않는다 — 아바타가 잠깐 안 보이는 것뿐이라 [loadRoom]처럼 `loadError`로
     * 올리지 않고 조용히 둔다.
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    private suspend fun loadRoomMembers() {
        runCatchingDomain { roomRepository.getMembers(roomId) }
            .onSuccess { members ->
                val summary = members.toMemberSummary()
                pendingMemberSummary = summary
                updateState { copy(room = room?.copy(memberSummary = summary)) }
            }
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
            runCatchingDomain { roomPlacesRepository.sharePlaces(place.id, targetRoomIds) }
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
            runCatchingDomain { roomPlacesRepository.deletePlace(roomId, place.id) }
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

    /**
     * [FR-011] "초대하기" — 이미 발급받은 [RoomDetailUiState.inviteCode]를 링크로 조립해 OS 공유 시트를
     * 연다. 버튼이 `inviteCode != null`일 때만 활성화되므로([RoomInviteSheet]) 여기서 다시 null을
     * 검사할 필요는 없지만, 방어적으로 null이면 아무 것도 하지 않는다.
     */
    private fun onInviteConfirmClick() {
        val code = state.value.inviteCode ?: return
        launchSafely { postSideEffect(RoomDetailSideEffect.ShareInviteLink(inviteLinkBuilder.build(code))) }
    }

    /** [FR-011] "링크 복사하기" — 같은 규칙으로 링크를 조립해 클립보드 복사를 요청한다. */
    private fun onCopyInviteLinkClick() {
        val code = state.value.inviteCode ?: return
        launchSafely { postSideEffect(RoomDetailSideEffect.CopyInviteLink(inviteLinkBuilder.build(code))) }
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
     * [SYS-007] 나가기 시작 — `isOwner`와 [RoomDetailUiState.room]의 [RoomMemberSummary]로 이미
     * 알고 있는 실제 멤버 수를 함께 본다.
     *
     * 예전엔 방장이면 멤버 수와 무관하게 항상 `ConfirmOwnerSingle`("나가면 방이 삭제돼요")부터 띄우고
     * `leaveRoom` 호출의 `409`로만 [LeaveDialogState.DelegateOwner]로 전이했다(서버 판정을 SSOT로 삼아
     * 클라이언트가 멤버 수를 중복 계산하지 않는다는 판단, `research.md` D15). 그런데 실기기 확인 결과
     * 공유 중인 방(멤버 2명 이상)의 방장에게도 "혼자라 방이 삭제된다"는 **사실과 다른 문구**가 먼저 보이는
     * 결함으로 드러났다 — 서버가 옳게 판정해도 그 사이 화면이 거짓말을 하는 것 자체가 문제였다.
     * [RoomDetailUiState.room]의 `memberSummary`는 화면 진입 시 [loadRoomMembers]가 이미
     * `GET /rooms/{roomId}/members` 실측으로 채워 두므로, 추가 왕복 없이 지금 바로 정확한 모달을
     * 고를 수 있다.
     */
    private fun onLeaveClick() {
        val memberCount = state.value.room
            ?.memberSummary
            ?.let { it.visibleAvatars.size + it.overflowCount }
            ?: 0
        updateState {
            copy(
                leaveDialogState = when {
                    !isOwner -> LeaveDialogState.ConfirmMember
                    memberCount > 1 -> LeaveDialogState.DelegateOwner
                    else -> LeaveDialogState.ConfirmOwnerSingle
                },
            )
        }
        if (state.value.leaveDialogState == LeaveDialogState.DelegateOwner) {
            launchSafely {
                runCatchingDomain { roomRepository.getMembers(roomId) }
                    .onSuccess { members -> updateState { copy(roomMembers = members.toImmutableList()) } }
                    .onDomainFailure(::emitDomainError)
            }
        }
    }

    /**
     * [SYS-007] 나가기 확정 — `leaveRoom` 호출 자체의 성공/`409` 응답으로 서버가 판정한 결과를
     * 그대로 분기에 반영한다. [onLeaveClick]이 이미 멤버 수로 [LeaveDialogState.DelegateOwner]를
     * 골랐다면 이 경로는 타지 않지만, 그 판단 이후 다른 멤버가 방을 나가는 등 경합이 생겨도 서버가
     * 최종 판정하므로 `409` 방어선은 그대로 둔다.
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
     *
     * `transferOwner`가 성공하는 즉시 `isOwner`를 내려놓고 모달을 닫는다 — 서버는 이미 방장이 바뀐
     * 상태이므로, 뒤이은 `leaveRoom`이 실패해 이 화면에 남더라도 "더보기"에 방장 전용 항목(방 편집)이나
     * 위임 대상 선택 화면이 잘못 남아있지 않게 한다(실기기 확인된 결함).
     */
    private fun onOwnerDelegateConfirm() {
        val nextOwnerId = state.value.selectedDelegateMemberId ?: return
        launchSafely {
            runCatchingDomain { roomRepository.transferOwner(roomId, nextOwnerId) }
                .onSuccess {
                    updateState {
                        copy(
                            isOwner = false,
                            leaveDialogState = LeaveDialogState.None,
                            selectedDelegateMemberId = null,
                        )
                    }
                    runCatchingDomain { roomRepository.leaveRoom(roomId) }
                        .onSuccess { postSideEffect(RoomDetailSideEffect.NavigateToRoomList) }
                        .onDomainFailure(::emitDomainError)
                }.onDomainFailure(::emitDomainError)
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
