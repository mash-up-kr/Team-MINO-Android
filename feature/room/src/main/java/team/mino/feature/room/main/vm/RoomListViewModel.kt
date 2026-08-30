package team.mino.feature.room.main.vm

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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.core.common.android.extension.launchSafely
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.common.kotlin.geo.distanceMetersTo
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.Place
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomListSortOption
import team.mino.core.domain.model.RoomMemberSummary
import team.mino.core.domain.repository.ProfileRepository
import team.mino.core.domain.repository.RoomPlacesRepository
import team.mino.core.domain.repository.RoomRepository
import team.mino.core.domain.usecase.EnsureAnonymousSessionUseCase
import team.mino.core.navigation.activity.launcher.RoomFormLauncher
import team.mino.feature.room.main.component.DefaultMapCenter
import team.mino.feature.room.main.model.BottomSheetLevel
import team.mino.feature.room.main.model.MapPinUiModel
import team.mino.feature.room.main.model.chip
import team.mino.feature.room.main.model.roomColor
import team.mino.feature.room.main.model.toMemberSummary
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 방 리스트 탭의 유일한 Route(RoomListMain) ViewModel.
 *
 * 계약: docs/specs/room-list/contracts/room-list-main-contract.md
 *
 * 지금 단계(US1)는 지도·3단 시트·필터 관련 Intent만 처리한다. 방 목록·상세 진입(US2)·공동방
 * 생성(US3)·Nudge(US4) 관련 Intent는 각 사용자 스토리 단계에서 채운다.
 */
@HiltViewModel
class RoomListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val roomRepository: RoomRepository,
    private val roomPlacesRepository: RoomPlacesRepository,
    private val profileRepository: ProfileRepository,
    private val ensureAnonymousSessionUseCase: EnsureAnonymousSessionUseCase,
    val roomFormLauncher: RoomFormLauncher,
) : ViewModel(),
    MviContainer<RoomListUiState, RoomListSideEffect> by mviContainer(RoomListUiState()) {
    /** 방마다 조회한 장소. 방 목록·프로필이 바뀌어도 이미 받아 둔 장소는 유지하려고 상태 밖에 둔다. */
    private var placesByRoomId: Map<String, List<Place>> = emptyMap()

    init {
        observeMyRooms()
        launchSafely {
            profileRepository.observeProfile().collect { profile ->
                updateState { copy(myProfileAvatar = profile?.avatar) }
                refreshMapPins()
            }
        }
    }

    /**
     * [contracts/room-list-main-contract.md 「재조회」] `personalRoom`·`groupRooms`는
     * `RoomRepository.observeMyRooms()` 구독으로 항상 최신 유지된다. `groupRooms`가 갱신될 때마다
     * `showNudge`·`showGhostCard`를 `groupRooms.isEmpty()` 파생값으로 함께 계산한다
     * (FR-008~FR-010, [contracts/room-list-main-contract.md 「분기 규칙 — Nudge·Ghost Card 노출」]).
     *
     * **`init`·[onScreenEntered]·[onCloseRoomDetailClick]·[onRoomFormResult] 네 곳에서 부른다.**
     * 인스타그램 공유 시트처럼 외부 앱에 잠깐 다녀오는 동안 이 Activity는 살아있지만(프로세스가 죽지
     * 않는다) 화면 밖에서 핀이 새로 저장될 수 있다 — `Route`가 `ON_RESUME`마다
     * [RoomListIntent.OnScreenEntered]를 다시 보내므로, 그때도 이 함수를 다시 불러야 돌아올 때마다
     * 목록이 새로고침된다. 방 상세는 별도 Navigation 목적지가 아니라 이 화면 안의 로컬 상태 전환이라
     * 닫혀도 `ON_RESUME`이 안 나므로 [onCloseRoomDetailClick]에서 명시적으로 부른다. 콜드 스타트 시
     * `init` 직후 첫 `OnScreenEntered`가 한 번 더 부르는 중복은 무해하다(전부 1회성 조회라 구독이
     * 쌓이지 않는다).
     *
     * `ensureAnonymousSessionUseCase()`를 먼저 기다리는 이유: 앱을 콜드 스타트하면 이 `init`이 익명
     * 로그인이 끝나기도 전에 실행돼 `observeMyRooms()`의 첫 요청이 신원 증명 없이 나가 실패했다
     * (`MinoIdentityProofPlugin`). 이 실패는 `launchSafely`의 `CoroutineExceptionHandler`가 잡아
     * "알 수 없는 오류"로만 보여주고 재시도하지 않아, 그 뒤로 목록 구독 자체가 영영 멈췄다. 진입 화면이
     * 세션 확보를 전담하는 게 맞지만(`docs/adr/2026-08-22-session-retry-owned-by-caller.md`) 그
     * 화면이 아직 없어, 이 레이스를 막을 다른 호출자가 없다. `ensureSession()`은 멱등이라(같은 문서)
     * 이미 확보된 세션에서는 즉시 반환된다 — 매 호출마다 비용이 들지 않는다.
     */
    private fun observeMyRooms() {
        launchSafely {
            ensureAnonymousSessionUseCase()
            roomRepository.observeMyRooms().collect { rooms -> onRoomsUpdated(rooms) }
        }
    }

    private fun onRoomsUpdated(rooms: List<Room>) {
        val personal = rooms.firstOrNull { it.isPersonal }
        val group = rooms.filterNot { it.isPersonal }
        updateState {
            val sortedGroup = group.sortedByRoomListOption(roomListSort).toImmutableList()
            copy(
                personalRoom = personal,
                groupRooms = sortedGroup,
                showNudge = sortedGroup.isEmpty(),
                showGhostCard = sortedGroup.isEmpty(),
            )
        }
        refreshMapPins()
        rooms.forEach { room ->
            loadRoomPlaces(room.id)
            loadRoomMembers(room.id)
        }
    }

    /**
     * 방 카드·지도 카드가 보여줄 멤버 아바타를 채운다(`GET /rooms/{roomId}/members`).
     *
     * `RoomSummaryResponse`(방 목록 조회)에는 멤버 아바타가 없어 [memberCount]만 아는 채로 방 목록이
     * 먼저 그려지고, 방마다 이 호출이 끝나는 대로 [RoomListUiState.personalRoom]·[RoomListUiState.groupRooms]의
     * 해당 방 [Room.memberSummary]를 갈아 끼운다.
     */
    @OptIn(ExperimentalTime::class)
    private fun loadRoomMembers(roomId: String) {
        launchSafely {
            val summary = roomRepository.getMembers(roomId).toMemberSummary()
            updateState {
                copy(
                    personalRoom = personalRoom?.replaceMemberSummary(roomId, summary),
                    groupRooms = groupRooms.map { it.replaceMemberSummary(roomId, summary) }.toImmutableList(),
                )
            }
        }
    }

    /**
     * 방 하나에 저장된 장소를 조회한다 — 개인 방·공동방 모두 같은 방식으로 지도에 실좌표
     * (`Place.location`) 핀을 얹는다(PRD 「자신이 저장한 모든 장소를 지도뷰로 볼 수 있다」).
     */
    private fun loadRoomPlaces(roomId: String) {
        launchSafely {
            roomPlacesRepository.observePlaces(roomId).collect { places ->
                placesByRoomId = placesByRoomId + (roomId to places)
                refreshMapPins()
            }
        }
    }

    /**
     * [placesByRoomId]·현재 방 목록·내 프로필로 지도 핀 목록을 다시 만든다. 조회가 끝날 때마다뿐 아니라
     * 정렬·필터·`mapCenter`가 바뀔 때도 다시 불러 [RoomListUiState.mapPins]를 최신으로 맞춘다.
     *
     * 개인 방은 `RoomColor.GRAY`(색 미선택)라 방 색을 핀에 쓸 수 없어 내 프로필 색으로 대신한다
     * ([ProfileAvatarMapping.roomColor]). 공동방은 방 대표 색을 그대로 쓴다([RoomColorMapping.chip]).
     *
     * **정렬·필터는 전부 클라이언트 처리다.** `GET /pins` 계약이 "정렬/필터(5종)... 기획 TBD"로 못박아
     * 서버 파라미터가 없다 — `거리순`은 애초에 서버가 알 수 없는 사용자 위치 기준 계산이라 항상
     * 클라이언트 몫이다. `코멘트순`·`꾹 Pick`은 서버가 아직 `commentCount`를 안 내려줘 지금은 `전체`와
     * 같게 둔다(값이 전부 0이라 정렬해도 의미가 없다).
     */
    private fun refreshMapPins() {
        val rooms = listOfNotNull(state.value.personalRoom) + state.value.groupRooms
        val myColor = state.value.myProfileAvatar?.roomColor
        val allPins = rooms.flatMap { room ->
            val color = if (room.isPersonal) myColor else room.color.chip
            placesByRoomId[room.id].orEmpty().map { place -> MapPinUiModel(place = place, color = color) }
        }
        val center = state.value.mapCenter ?: DefaultMapCenter
        val pins = allPins
            .filteredByCategory(state.value.categoryFilter)
            .sortedByMapMarkerOption(state.value.mapMarkerSort, center)
        updateState { copy(mapPins = pins.toImmutableList()) }
    }

    fun processIntent(intent: RoomListIntent) {
        when (intent) {
            RoomListIntent.OnScreenEntered -> onScreenEntered()
            RoomListIntent.OnSheetDraggedUp -> onSheetDraggedUp()
            RoomListIntent.OnSheetDraggedDown -> onSheetDraggedDown()
            is RoomListIntent.OnMapSortSelected -> onMapSortSelected(intent.option)
            is RoomListIntent.OnCategoryFilterSelected -> onCategoryFilterSelected(intent.category)
            RoomListIntent.OnCurrentLocationClick -> onCurrentLocationClick()
            is RoomListIntent.OnLocationPermissionResult -> onLocationPermissionResult(intent.granted)
            is RoomListIntent.OnRoomListSortSelected -> onRoomListSortSelected(intent.option)
            is RoomListIntent.OnRoomCardClick -> onRoomCardClick(intent.roomId)
            RoomListIntent.OnCloseRoomDetailClick -> onCloseRoomDetailClick()
            RoomListIntent.OnAddRoomClick -> onAddRoomClick()
            is RoomListIntent.OnRoomFormResult -> onRoomFormResult(intent.createdRoomId)

            // [FR-008][FR-009] Ghost Card·Nudge의 [공동방 만들기]는 [+] 버튼(OnAddRoomClick)과 같은
            // 전환 결정을 낸다 — NavigateToRoomForm을 재사용한다(T046과 동일 SideEffect).
            RoomListIntent.OnGhostCardClick,
            RoomListIntent.OnNudgeCreateClick,
            -> onAddRoomClick()
        }
    }

    /**
     * [D8] 상태 캐싱 없이 매 진입마다 OS 권한을 직접 조회한다.
     *
     * 재진입마다 `groupRooms.isEmpty()`로 `showNudge`·`showGhostCard`를 다시 계산한다([research.md D9]).
     */
    private fun onScreenEntered() {
        observeMyRooms()
        if (hasLocationPermission()) {
            launchSafely {
                val center = resolveMapCenter(granted = true)
                updateState { copy(mapCenter = center, mapCenterRequestId = mapCenterRequestId + 1) }
                refreshMapPins()
            }
        } else {
            launchSafely { postSideEffect(RoomListSideEffect.RequestLocationPermission) }
        }
    }

    /** [EC-002] 거부 시 기본 디폴트 좌표, 허용 시 실제 위치로 `mapCenter`를 설정한다. */
    private fun onLocationPermissionResult(granted: Boolean) {
        launchSafely {
            val center = resolveMapCenter(granted)
            updateState { copy(mapCenter = center, mapCenterRequestId = mapCenterRequestId + 1) }
            refreshMapPins()
        }
    }

    /** [research.md D10] 현재 위치 버튼 최소 구현 — `mapCenter`만 갱신한다. */
    private fun onCurrentLocationClick() {
        if (!hasLocationPermission()) return
        launchSafely {
            val center = resolveMapCenter(granted = true)
            updateState { copy(mapCenter = center, mapCenterRequestId = mapCenterRequestId + 1) }
            refreshMapPins()
        }
    }

    /** [FR-011] 지도 마커 정렬 드롭다운 — `NEARBY`는 [refreshMapPins]가 `mapCenter` 기준 3km 반경으로 거른다. */
    private fun onMapSortSelected(option: MapMarkerSortOption) {
        updateState { copy(mapMarkerSort = option) }
        refreshMapPins()
    }

    /** [FR-011] 카테고리 칩 — 전체/카페/음식점. */
    private fun onCategoryFilterSelected(category: PlaceCategoryFilter) {
        updateState { copy(categoryFilter = category) }
        refreshMapPins()
    }

    /** 거부 시 기본 디폴트 좌표, 허용 시 실제 위치로 해석한다(EC-002). 세 호출부가 공유하는 단일 규칙. */
    private suspend fun resolveMapCenter(granted: Boolean): GeoPoint =
        if (granted) currentDeviceLocation() ?: DefaultMapCenter else DefaultMapCenter

    /** [contracts/room-list-main-contract.md 「분기 규칙 — 시트 드래그 전이」] */
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

    /** [FR-005] 개인방 고정은 [RoomListUiState.personalRoom]이 별도 필드라 자동으로 유지되고, 여기서는
     * [RoomListUiState.groupRooms]만 재정렬한다. */
    private fun onRoomListSortSelected(option: RoomListSortOption) {
        updateState {
            copy(
                roomListSort = option,
                groupRooms = groupRooms.sortedByRoomListOption(option).toImmutableList(),
            )
        }
    }

    /**
     * [FR-006] 방 카드 선택 — `selectedRoomId`를 설정해 같은 목적지 안에서 방 상세로 전환한다(별도
     * Navigation 목적지 전환이 아니다, `RoomNavigation.kt` KDoc 참고).
     */
    private fun onRoomCardClick(roomId: String) {
        updateState { copy(selectedRoomId = roomId) }
    }

    /**
     * 방 상세 [X] 닫기 — 리스트로 복귀한다.
     *
     * [observeMyRooms]를 [onRoomFormResult]와 같은 이유로 다시 부른다 — 방 상세는 별도 Navigation
     * 목적지가 아니라 이 화면 안의 로컬 상태 전환이라 닫을 때 `ON_RESUME`이 발생하지 않는다. 방 상세에서
     * 방을 나가면([SYS-007]) 이 화면으로 돌아오는데, 여기서 다시 불러오지 않으면 이미 나간 방이 목록에
     * 그대로 남는다(실기기 확인된 결함).
     */
    private fun onCloseRoomDetailClick() {
        updateState { copy(selectedRoomId = null) }
        observeMyRooms()
    }

    /** [FR-007] 시트 우상단 [+] → [RoomListSideEffect.NavigateToRoomForm] 발행(전환 결정만, 실제 호출은 Route). */
    private fun onAddRoomClick() {
        launchSafely { postSideEffect(RoomListSideEffect.NavigateToRoomForm) }
    }

    /**
     * [FR-007] 공동방 생성 폼 결과 수신. `createdRoomId`가 있으면(생성 완료) 곧바로 그 방의 상세로
     * 체이닝한다([FR-006]의 [onRoomCardClick]과 같은 규칙) — `null`이면(취소) 아무 것도 하지 않는다.
     *
     * [observeMyRooms]를 다시 부르는 이유: `RoomRepository.observeMyRooms()`는 서버를 계속 듣는
     * 진짜 스트림이 아니라 **호출 시점에 한 번** 조회해 흘려보내는 Flow다(로컬 캐시·웹소켓이 없다).
     * 그래서 방을 새로 만들어도 이 화면의 `groupRooms`는 저절로 갱신되지 않는다 — 방 생성 폼이 닫히는
     * 이 시점에 명시적으로 다시 불러와야 목록에 나타난다.
     */
    private fun onRoomFormResult(createdRoomId: String?) {
        if (createdRoomId == null) return
        observeMyRooms()
        updateState { copy(selectedRoomId = createdRoomId) }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * 기기 위치. `:core:map`은 지도 렌더링만 담당하고 위치 조회 인프라가 없어(README 확인 완료)
     * 프레임워크 `LocationManager`로 직접 조회한다 — 별도 SDK 의존을 새로 들이지 않는다.
     *
     * 캐시된 마지막 위치(`getLastKnownLocation`)부터 확인하고, 없으면(다른 앱이 최근에 위치를 요청한
     * 적이 없는 기기에서는 모든 provider가 `null`을 반환한다) 활성화된 provider로 새 위치를 능동적으로
     * 요청한다. `LOCATION_FETCH_TIMEOUT`을 넘기면 [resolveMapCenter]가 기본 좌표로 폴백한다.
     */
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

    /** [requestSingleUpdate]는 API 21부터 지원한다(minSdk 29) — `getCurrentLocation`(API 30+)보다 넓은 범위를 커버한다. */
    @SuppressLint("MissingPermission")
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

/**
 * [RoomListSortOption.ALL]은 서버가 내려준 원래 순서를 유지한다(별도 정렬 기준 없음).
 * 저장된 장소가 없어 [Room.lastPlaceSavedAt]이 `null`인 방은 "최근 저장 순"에서 가장 뒤로 보낸다.
 */
@OptIn(ExperimentalTime::class)
private fun List<Room>.sortedByRoomListOption(option: RoomListSortOption): List<Room> =
    when (option) {
        RoomListSortOption.ALL -> this
        RoomListSortOption.RECENTLY_SAVED -> sortedByDescending { it.lastPlaceSavedAt ?: Instant.DISTANT_PAST }
        RoomListSortOption.MOST_COMMENTED -> sortedByDescending { it.commentCount }
    }

private fun List<MapPinUiModel>.filteredByCategory(categoryFilter: PlaceCategoryFilter): List<MapPinUiModel> =
    if (categoryFilter == PlaceCategoryFilter.ALL) this else filter { it.place.category == categoryFilter }

/**
 * `ALL`·`GGUK_PICK`·`MOST_COMMENTED`는 서버 파라미터도 없고(`GET /pins` 계약 "정렬/필터 TBD")
 * `commentCount`도 서버가 아직 안 내려줘(항상 0) 지금은 원래 순서를 그대로 둔다. `NEARBY`(거리순)는
 * 서버가 알 수 없는 사용자 위치 기준이라 원래부터 클라이언트 계산이다 — [center] 3km 반경으로 거르고
 * 가까운 순으로 정렬한다.
 */
@OptIn(ExperimentalTime::class)
private fun List<MapPinUiModel>.sortedByMapMarkerOption(
    option: MapMarkerSortOption,
    center: GeoPoint,
): List<MapPinUiModel> =
    when (option) {
        MapMarkerSortOption.ALL,
        MapMarkerSortOption.GGUK_PICK,
        MapMarkerSortOption.MOST_COMMENTED,
        -> this

        MapMarkerSortOption.LATEST -> sortedByDescending { it.place.savedAt }

        MapMarkerSortOption.NEARBY ->
            filter { center.distanceMetersTo(it.place.location) <= NEARBY_RADIUS_METERS }
                .sortedBy { center.distanceMetersTo(it.place.location) }
    }

private const val NEARBY_RADIUS_METERS = 3_000.0

@OptIn(ExperimentalTime::class)
private fun Room.replaceMemberSummary(
    roomId: String,
    summary: RoomMemberSummary,
): Room = if (id == roomId) copy(memberSummary = summary) else this
