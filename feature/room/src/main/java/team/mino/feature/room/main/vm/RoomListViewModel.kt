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
import kotlinx.coroutines.flow.filterNotNull
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
import team.mino.core.domain.repository.PlaceRepository
import team.mino.core.domain.repository.RoomPlacesRepository
import team.mino.core.domain.repository.RoomPreferencesRepository
import team.mino.core.domain.repository.RoomRepository
import team.mino.core.domain.usecase.EnsureAnonymousSessionUseCase
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import team.mino.core.navigation.activity.launcher.RoomFormLauncher
import team.mino.core.navigation.entry.PlaceDetailEntryOrigin
import team.mino.core.navigation.entry.PlaceDetailRequestHolder
import team.mino.feature.room.component.chip
import team.mino.feature.room.main.component.DefaultMapCenter
import team.mino.feature.room.main.model.BottomSheetLevel
import team.mino.feature.room.main.model.MapPinUiModel
import team.mino.feature.room.main.model.toMemberSummary
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
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
    @param:ApplicationContext private val context: Context,
    private val roomRepository: RoomRepository,
    private val roomPlacesRepository: RoomPlacesRepository,
    private val roomPreferencesRepository: RoomPreferencesRepository,
    private val ensureAnonymousSessionUseCase: EnsureAnonymousSessionUseCase,
    private val placeRepository: PlaceRepository,
    private val placeDetailRequestHolder: PlaceDetailRequestHolder,
    val roomFormLauncher: RoomFormLauncher,
) : ViewModel(),
    MviContainer<RoomListUiState, RoomListSideEffect> by mviContainer(RoomListUiState()) {
    /** 방마다 조회한 장소. 방 목록이 바뀌어도 이미 받아 둔 장소는 유지하려고 상태 밖에 둔다. */
    private var placesByRoomId: Map<String, List<Place>> = emptyMap()

    init {
        observeMyRooms()
        observePlaceDetailRequests()
    }

    /**
     * 다른 탭(홈·알림)이 남긴 장소 상세 요청을 받아 연다
     * (`docs/specs/place-detail/contracts/place-detail-entry.md` §3,
     * `contracts/place-detail-main-contract.md` §2.3).
     *
     * 요청이 싣고 오는 값은 `pinId`와 진입 출처 둘이다 — 방은 핀 상세 응답의 `roomId`로 해석한다(같은
     * 계약 §3.4). 알림처럼 방을 특정하지 않는 진입도 이 해석으로 목적지가 정해진다(EC-001). 출처는
     * [나가기]가 홈으로 나갈지를 가르는 데만 쓴다(FR-009, 같은 계약 §4.2).
     *
     * **구독은 `init`에서 한 번만 연다.** [observeMyRooms]와 달리 화면 재진입마다 다시 열면 같은 요청을
     * 두 번 받는 구독이 겹친다. 탭을 오가도 이 ViewModel은 살아 있어(저장 탭 백스택이 복원된다) 이 구독
     * 하나로 충분하다.
     */
    private fun observePlaceDetailRequests() {
        launchSafely {
            placeDetailRequestHolder.pending
                .filterNotNull()
                .collect { request -> openRequestedPlaceDetail(request.pinId, request.origin) }
        }
    }

    /**
     * 요청받은 핀의 장소 상세를 연다 — 「지금 보고 있는 방」·그 방의 핀·카메라를 **함께** 세운다.
     *
     * 방과 핀을 함께 세워야 [나가기](FR-009)가 드러낼 방 상세가 이미 그 아래에 있다. 알림에서 들어오면
     * 방 상세가 아직 안 열려 있어, 핀만 세우면 [나가기]가 빈자리로 떨어진다(TS-007).
     *
     * **홈 진입은 [나가기]가 방 상세를 드러내지 않지만(홈 탭으로 되돌린다) 방은 똑같이 세운다.** 마커
     * 양식(§2.4)과 코멘트 목록이 「지금 보고 있는 방」을 따르고(FR-027), 사용자가 [저장된 방]으로 방을
     * 바꾸면 그 자리에서 기본 갈래로 넘어가기 때문이다(TS-057).
     *
     * **출처는 여기서 `returnsToHomeOnClose` 한 값으로 굳힌다.** 조회에 실패해 아무것도 열지 않았다면
     * 이 플래그도 서지 않는다 — 열리지 않은 화면의 나가기 규칙이 남지 않는다.
     *
     * **카메라도 여기서 옮긴다**(FR-002·TS-056). 카메라 이동은 진입점 넷 전부에 걸리므로 탭 간 진입도
     * 예외가 아니다. 좌표는 [onPlaceSelected]처럼 [placesByRoomId]에서 찾지 않고 **핀 상세 응답의
     * `location`**을 쓴다 — 홈에서 콜드 진입하면 그 목록이 아직 비어 있어 못 찾고, 못 찾으면 카메라가
     * 진입 직전 자리(현재 위치·기본 좌표)에 머물러 선택 핀이 화면 밖에 남는다(실기기 확인된 결함).
     *
     * **요청은 결과와 무관하게 먼저 비운다.** 남겨 두면 사용자가 [나가기]로 닫은 장소가 탭을 오갈 때마다
     * 다시 열린다(`PlaceDetailRequestHolder.pending` KDoc).
     *
     * 조회에 실패하면 아무것도 열지 않는다 — 열 화면이 없는 채로 빈 상세를 띄우지 않는다(계약 §2.3).
     * 이 화면의 주 데이터가 아니라 요청 하나의 해석이 실패한 것이라 오류 상태로 올리지 않고, 사용자는
     * 이미 그려져 있는 방 목록에 그대로 남는다.
     */
    private suspend fun openRequestedPlaceDetail(
        pinId: String,
        origin: PlaceDetailEntryOrigin,
    ) {
        placeDetailRequestHolder.consume()
        runCatchingDomain { placeRepository.getPlaceDetail(pinId) }
            .onSuccess { detail ->
                updateState {
                    copy(
                        selectedRoomId = detail.roomId,
                        selectedPinId = pinId,
                        returnsToHomeOnClose = origin == PlaceDetailEntryOrigin.HOME,
                    ).movingCameraTo(detail.location)
                }
                refreshMapPins()
            }.onDomainFailure {
                // 삼킨다는 것이 이 경로의 계약이므로, 결과를 버리는 대신 빈 소비로 드러낸다
                // (`docs/conventions/error_handling.md` §7-4).
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
     * [placesByRoomId]·현재 방 목록으로 지도 핀 목록을 다시 만든다. 조회가 끝날 때마다뿐 아니라
     * 정렬·필터·`mapCenter`가 바뀔 때도 다시 불러 [RoomListUiState.mapPins]를 최신으로 맞춘다.
     *
     * **선택 핀 판정은 `place.id == selectedPinId` 한 비교로 여기서만 한다**(FR-002, TS-002) —
     * `Place.id`가 곧 핀 id라(`Place` KDoc) 별도 조회가 없다. [RoomListMap]은 그 결과를 강조 외형으로
     * 흘리기만 하고 판정을 다시 하지 않는다
     * (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §2.4).
     *
     * 개인 방은 `RoomColor.GRAY`(색 미선택)라 `color`를 `null`로 둔다 — [RoomMapPin]이 `null`을
     * 기본(검정) 핀으로 그린다(`RoomColorMapping.chip`의 `RoomColor.GRAY -> null`과 같은 규칙). 예전엔
     * 이 자리에 내 프로필 색을 대신 얹었으나, 개인 방 핀은 항상 기본 검정이어야 한다는 확인에 따라
     * 정정했다(실기기 확인, 2026-08-31).
     *
     * **정렬·필터는 전부 클라이언트 처리다.** `GET /pins` 계약이 "정렬/필터(5종)... 기획 TBD"로 못박아
     * 서버 파라미터가 없다 — `거리순`은 애초에 서버가 알 수 없는 사용자 위치 기준 계산이라 항상
     * 클라이언트 몫이다. `코멘트순`·`꾹 Pick`은 서버가 아직 `commentCount`를 안 내려줘 지금은 `전체`와
     * 같게 둔다(값이 전부 0이라 정렬해도 의미가 없다).
     */
    private fun refreshMapPins() {
        val allRooms = listOfNotNull(state.value.personalRoom) + state.value.groupRooms
        // [FR-001] 방 상세(`selectedRoomId`)가 열려 있으면 그 방의 장소만 지도에 남긴다 — "해당 방의
        // 장소만 표시된 지도 초기화"(spec.md FR-001). 리스트로 돌아오면(`selectedRoomId == null`)
        // 다시 모든 방을 합친다.
        val selectedRoomId = state.value.selectedRoomId
        val rooms = if (selectedRoomId != null) allRooms.filter { it.id == selectedRoomId } else allRooms
        val selectedPinId = state.value.selectedPinId
        val allPins = rooms.flatMap { room ->
            val color = if (room.isPersonal) null else room.color.chip
            placesByRoomId[room.id].orEmpty().map { place ->
                MapPinUiModel(place = place, color = color, selected = place.id == selectedPinId)
            }
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
            is RoomListIntent.OnPlaceSelected -> onPlaceSelected(intent.pinId)
            RoomListIntent.OnClosePlaceDetailClick -> onClosePlaceDetailClick()
            is RoomListIntent.OnPlaceDetailRoomSwitched ->
                onPlaceDetailRoomSwitched(pinId = intent.pinId, roomId = intent.roomId)

            RoomListIntent.OnAddRoomClick -> onAddRoomClick()
            is RoomListIntent.OnRoomFormResult -> onRoomFormResult(intent.createdRoomId)

            // [FR-008][FR-009] Ghost Card·Nudge의 [공동방 만들기]는 [+] 버튼(OnAddRoomClick)과 같은
            // 전환 결정을 낸다 — NavigateToRoomForm을 재사용한다(T046과 동일 SideEffect).
            RoomListIntent.OnGhostCardClick,
            RoomListIntent.OnNudgeCreateClick,
            -> onAddRoomClick()

            RoomListIntent.OnNudgeDismissClick -> onNudgeDismissClick()
        }
    }

    /**
     * [D8] 상태 캐싱 없이 매 진입마다 OS 권한을 직접 조회한다.
     *
     * 재진입마다 `groupRooms.isEmpty()`로 `showNudge`·`showGhostCard`를 다시 계산한다([research.md D9]).
     * `nudgeSheetDismissed`는 [isNudgeSuppressionActive]가 저장된 마지막 닫힘 시각을 조회해 판정한다
     * (PRD 11.1.0 [SYS-009] — [나중에 만들래요] 클릭 시 2주 동안 재표출하지 않는다). 예전엔 매 진입마다
     * 무조건 `false`로 되돌려 세션당 1회 제한조차 없었는데(#290 QA로 발견), 그때는 아직 2주 억제 조건
     * 자체가 PRD에 없었다 — 이제는 그 조건이 생겼으니 저장된 값을 반영해야 한다.
     *
     * `nudgeSuppressionChecked`를 조회 완료와 같은 시점에 함께 세운다 — 조회가 끝나기 전(기본값
     * `nudgeSheetDismissed = false`)에 `observeMyRooms()` 응답으로 `showNudge`가 먼저 `true`가 되면
     * 억제 중이어야 할 팝업이 한 프레임 반짝 떴다 사라지는 콜드 스타트 결함이 있었다(실기기 확인) —
     * [RoomListUiState.isNudgeSheetVisible] KDoc 참고.
     */
    private fun onScreenEntered() {
        launchSafely {
            val suppressed = isNudgeSuppressionActive()
            updateState { copy(nudgeSheetDismissed = suppressed, nudgeSuppressionChecked = true) }
        }
        observeMyRooms()
        if (hasLocationPermission()) {
            moveCameraToResolvedLocation(granted = true)
        } else {
            launchSafely { postSideEffect(RoomListSideEffect.RequestLocationPermission) }
        }
    }

    /** [EC-002] 거부 시 기본 디폴트 좌표, 허용 시 실제 위치로 `mapCenter`를 설정한다. */
    private fun onLocationPermissionResult(granted: Boolean) {
        moveCameraToResolvedLocation(granted)
    }

    /**
     * 화면 진입에 딸린 자동 카메라 이동 — [onScreenEntered]·[onLocationPermissionResult]가 공유한다.
     *
     * **장소 상세가 열려 있으면 카메라를 옮기지 않는다**(spec EC-030). 홈·알림 진입은 탭 전환(→
     * [onScreenEntered])과 장소 상세 열기([openRequestedPlaceDetail])가 같은 순간에 일어나 두 카메라
     * 이동이 겹치는데, 어느 쪽이 이기는지가 위치 해석 속도에 달린다 — 캐시된 마지막 위치가 있으면 즉시
     * 끝나 장소 쪽이 이기지만, 없어서 활성 측위로 넘어가면 최대 `LOCATION_FETCH_TIMEOUT` 뒤에 도착해
     * 선택 핀에 맞춰 둔 카메라를 현재 위치가 덮는다. 순서로는 고정되지 않으므로 규칙으로 고정한다.
     *
     * **판정이 위치 해석 앞뒤로 두 번 있다.** 뒤의 것이 위 경합을 닫는다 — 앞에서만 보면 해석을 기다리는
     * 사이에 열린 장소 상세를 놓친다. 앞의 것은 낭비를 막는다 — 이미 열려 있는데 들어오면(탭 재진입)
     * 캐시가 없는 기기에서 GPS를 켜고 [LOCATION_FETCH_TIMEOUT]까지 기다린 뒤 결과를 버리게 된다.
     *
     * @param skipWhenPlaceDetailOpen 위 가드를 걸지 여부. 사용자가 직접 누른 [현재 위치]
     *  ([onCurrentLocationClick])만 `false`로 끈다 — 지목을 바꾸는 조작이라 규칙의 예외이며, 그 예외를
     *  「이 함수를 안 거치면 된다」가 아니라 인자로 두어야 새 호출부가 규칙 밖에서 태어나지 않는다.
     */
    private fun moveCameraToResolvedLocation(
        granted: Boolean,
        skipWhenPlaceDetailOpen: Boolean = true,
    ) {
        if (skipWhenPlaceDetailOpen && state.value.selectedPinId != null) return
        launchSafely {
            val center = resolveMapCenter(granted)
            if (skipWhenPlaceDetailOpen && state.value.selectedPinId != null) return@launchSafely
            updateState { movingCameraTo(center) }
            refreshMapPins()
        }
    }

    /**
     * [research.md D10] 현재 위치 버튼 최소 구현 — `mapCenter`만 갱신한다.
     *
     * **사용자가 지목한 이동이라 EC-030 가드를 끈다.** 이 버튼은 장소 상세 위에도 서 있어
     * (`PlaceDetailScreen`) 가드를 걸면 눌러도 지도가 안 움직인다.
     */
    private fun onCurrentLocationClick() {
        if (!hasLocationPermission()) return
        moveCameraToResolvedLocation(granted = true, skipWhenPlaceDetailOpen = false)
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
        refreshMapPins()
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
        refreshMapPins()
        observeMyRooms()
    }

    /**
     * [FR-002] 장소 상세를 연다 — `selectedPinId`를 세우고 카메라를 그 장소로 옮긴다.
     *
     * 좌표는 이미 받아 둔 [placesByRoomId]에서 찾는다. 이 인텐트는 저장 탭 **안**에서만 오므로 지금
     * 화면에 그려져 있는 장소가 그 목록에 이미 들어 있다. 그래도 못 찾으면 카메라를 건드리지 않는다 —
     * 좌표를 모르는 채로 [DefaultMapCenter] 같은 엉뚱한 곳으로 옮기면 사용자가 보던 자리를 잃는다.
     * 탭 밖에서 오는 진입은 그 목록이 아직 비어 있을 수 있어 이 길로 오지 않는다
     * ([openRequestedPlaceDetail]).
     *
     * [refreshMapPins]를 마지막에 부르는 이유는 두 가지다. 선택 표시(§2.4)를 핀에 반영해야 하고,
     * `mapCenter`가 바뀌면 `NEARBY` 정렬의 기준점도 함께 바뀌기 때문이다([onCurrentLocationClick]과
     * 같은 순서).
     *
     * 핀과 카메라를 한 번에 갱신하는 것은, 나눠 내보내면 장소는 골라졌는데 카메라는 아직 옛 자리인
     * 중간 상태가 화면에 한 프레임 드러나기 때문이다.
     *
     * **[RoomListUiState.returnsToHomeOnClose]를 내린다.** 저장 탭 안 진입은 홈 예외의 대상이 아니며
     * (FR-009), 홈에서 연 상세가 떠 있는 채로 지도 마커를 눌러 다른 장소로 옮겨간 경우에 그 플래그가
     * 따라붙는 것도 막는다 — 사용자는 이미 저장 탭 안에서 장소를 직접 고른 것이다.
     */
    private fun onPlaceSelected(pinId: String) {
        val location = placeLocationOf(pinId)
        updateState {
            val selected = copy(selectedPinId = pinId, returnsToHomeOnClose = false)
            if (location == null) selected else selected.movingCameraTo(location)
        }
        refreshMapPins()
    }

    /**
     * [FR-009] 장소 상세 [나가기] — 시스템 뒤로가기도 여기로 모인다. 나가는 자리가 둘로 갈린다.
     *
     * **기본은 `selectedPinId`만 비우는 것이다.** `selectedRoomId`를 그대로 두어 그 방의 방 상세가 다시
     * 드러난다(TS-006). [onCloseRoomDetailClick]과 달리 목록을 다시 불러오지 않는다 — 방을 나가는
     * 동작이 아니라 같은 방 안에서 한 겹 위로 올라오는 것이라 목록이 바뀔 일이 없다.
     *
     * **[RoomListUiState.returnsToHomeOnClose]면 홈 탭으로 되돌린다**(TS-037). [SCR-003] 홈 카드로
     * 들어왔고 [저장된 방]으로 방을 바꾼 적이 없는 경우다. 이때 **`selectedRoomId`도 함께 비운다**
     * (EC-031) — 홈 진입이 방과 핀을 함께 세우므로, 핀만 비우고 나가면 사용자가 연 적 없는 방 상세가
     * 저장 탭에 남아 다음 방문 때 튀어나온다. 이 결함은 [나가기] 직후 화면으로는 드러나지 않는다.
     *
     * 탭을 실제로 옮기는 것은 셸이다 — 이 모듈은 탭 목록을 모르므로 결정만 SideEffect로 올린다
     * (`docs/specs/place-detail/contracts/place-detail-entry.md` §4.2).
     */
    private fun onClosePlaceDetailClick() {
        val returnsToHome = state.value.returnsToHomeOnClose
        updateState {
            copy(
                selectedPinId = null,
                selectedRoomId = if (returnsToHome) null else selectedRoomId,
                returnsToHomeOnClose = false,
            )
        }
        refreshMapPins()
        if (returnsToHome) {
            launchSafely { postSideEffect(RoomListSideEffect.NavigateToHome) }
        }
    }

    /**
     * [FR-025] [저장된 방] 시트에서 다른 방을 골랐다 — 「지금 보고 있는 방」과 그 방의 핀을 함께 바꾼다.
     *
     * 둘을 한 번에 갱신해야 마커 양식(TS-045)과 [나가기] 목적지(TS-046)가 어긋나는 중간 상태가 없다.
     *
     * **여기서 홈 복귀 예외가 소멸한다**(TS-057). 방을 고른 것은 사용자가 방 맥락을 직접 선택한
     * 행위이므로, 마커 색·코멘트와 마찬가지로 나가는 자리도 바뀐 방을 따른다. 한 번 내린 플래그는
     * 원래 방으로 되돌려도 다시 올리지 않는다(EC-032) — 판정 기준이 "지금 어느 방인가"가 아니라
     * "방을 바꾼 적이 있는가"라, 되돌리는 것도 「바꾼 적」에 든다.
     */
    private fun onPlaceDetailRoomSwitched(
        pinId: String,
        roomId: String,
    ) {
        updateState { copy(selectedPinId = pinId, selectedRoomId = roomId, returnsToHomeOnClose = false) }
        refreshMapPins()
    }

    private fun placeLocationOf(pinId: String): GeoPoint? =
        placesByRoomId.values
            .asSequence()
            .flatten()
            .firstOrNull { it.id == pinId }
            ?.location

    /** [FR-007] 시트 우상단 [+] → [RoomListSideEffect.NavigateToRoomForm] 발행(전환 결정만, 실제 호출은 Route). */
    private fun onAddRoomClick() {
        launchSafely { postSideEffect(RoomListSideEffect.NavigateToRoomForm) }
    }

    /**
     * [FR-008][SYS-009] 자동 팝업 Nudge 닫기 — `showNudge`(=`groupRooms.isEmpty()`)는 건드리지 않는다.
     * 지금 시각을 [RoomPreferencesRepository]에 저장해, 다음 [onScreenEntered]부터 2주 동안
     * [isNudgeSuppressionActive]가 재표출을 막는다.
     */
    @OptIn(ExperimentalTime::class)
    private fun onNudgeDismissClick() {
        updateState { copy(nudgeSheetDismissed = true) }
        launchSafely { roomPreferencesRepository.setNudgeDismissedAt(Clock.System.now()) }
    }

    /** [SYS-009] 마지막으로 닫은 지 2주가 지나지 않았으면 `true` — 닫은 적이 없으면 `false`. */
    @OptIn(ExperimentalTime::class)
    private suspend fun isNudgeSuppressionActive(): Boolean {
        val dismissedAt = roomPreferencesRepository.getNudgeDismissedAt() ?: return false
        return Clock.System.now() < dismissedAt + NUDGE_SUPPRESSION_DURATION
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

    /** [requestSingleUpdate]는 API 21부터 지원한다(minSdk 29) — `getCurrentLocation`(API 30+)보다 넓은 범위를 커버한다.
     * `@Suppress("DEPRECATION")`은 그 이유로 대체하지 않고 그대로 쓰기로 한 의도적 선택이다. */
    @Suppress("DEPRECATION")
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

        /** [SYS-009] [나중에 만들래요] 클릭 시 Nudge 팝업을 재표출하지 않는 기간(PRD 11.1.0). */
        val NUDGE_SUPPRESSION_DURATION = 14.days
    }
}

/**
 * 카메라를 [center]로 옮긴 상태. `mapCenterRequestId`를 함께 올리는 것이 이동의 조건이라
 * ([RoomListUiState.mapCenterRequestId]) 두 필드를 따로 쓸 수 있게 두지 않는다 — 한쪽만 쓴 호출부는
 * 지도를 못 움직이고도 컴파일된다.
 */
private fun RoomListUiState.movingCameraTo(center: GeoPoint): RoomListUiState =
    copy(mapCenter = center, mapCenterRequestId = mapCenterRequestId + 1)

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
