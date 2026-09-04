package team.mino.feature.home.main.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.core.common.android.extension.launchSafely
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.DeckContext
import team.mino.core.domain.model.DeckKey
import team.mino.core.domain.model.DeckSort
import team.mino.core.domain.model.NextDeck
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.model.RoomType
import team.mino.core.domain.repository.HomeDeckRepository
import team.mino.core.domain.repository.HomePreferencesRepository
import team.mino.core.domain.repository.PlaceRepository
import team.mino.core.domain.usecase.ResolveNextDeckUseCase
import team.mino.core.domain.usecase.ResolveRoomEntryDeckUseCase
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.MinoDomainException
import team.mino.core.errorhandling.domainErrorEmitter
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import team.mino.feature.home.main.model.HomePhase
import team.mino.feature.home.main.model.HomeTooltip
import team.mino.feature.home.main.model.SavePickerState
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * 홈 탭 화면의 ViewModel.
 *
 * 사용자 조작 하나가 [HomeIntent] 하나로 들어와 [processIntent]의 한 분기로 간다 —
 * 목록은 `docs/specs/home-deck-exploration/contracts/home-ui.md` §2가 소유한다.
 *
 * 홈 안에서 끝나는 전환(방 시트·액션 메뉴·가이드)은 [HomeUiState]의 상태이고, 밖으로 나가는 것만
 * [HomeSideEffect]로 흘린다(같은 문서 §3).
 *
 * **도메인 에러는 실패의 성격이 통로를 가른다**(`docs/conventions/error_handling.md` §5).
 * 주 데이터(방 목록·덱) 로드 실패는 [HomeUiState.loadError]에 리프로 담기고, 사용자 액션의 일회성
 * 실패는 `DomainErrorEmitter`로 나가 `HomeRoute`가 수집한다.
 *
 * **진행 상태를 상태(state)와 필드로 나눠 든다.** 화면이 그리는 것만 [HomeUiState]에 있고, 순회 판정에만
 * 쓰이는 것([exhausted]·[previewedDecks])은 필드에 둔다 — 그 값들은 어느 픽셀에도 대응하지 않는다.
 * 둘 다 홈에 머무는 동안만 살아 있고 기기에 저장하지 않는다(`research.md` R-004).
 *
 * [rooms]만 양쪽에 있다 — 순회 판정이 쓰면서 방 시트도 그린다. 첫 조회 뒤 바뀌지 않으므로 그 지점에서
 * 한 번 상태로 옮기고, 이후 판정은 계속 필드를 읽는다.
 */
@HiltViewModel
internal class HomeViewModel
    @Inject
    constructor(
        private val homeDeckRepository: HomeDeckRepository,
        private val homePreferencesRepository: HomePreferencesRepository,
        private val resolveNextDeck: ResolveNextDeckUseCase,
        private val placeRepository: PlaceRepository,
        private val resolveRoomEntryDeck: ResolveRoomEntryDeckUseCase = ResolveRoomEntryDeckUseCase(),
    ) :
    ViewModel(),
        MviContainer<HomeUiState, HomeSideEffect> by mviContainer(HomeUiState()),
        DomainErrorEmitter by domainErrorEmitter() {
        /** 순회 대상 방 목록. 홈에 머무는 동안 다시 받지 않는다 — 받아 오는 사이 순서가 바뀌면 전환 대상이 뒤바뀐다. */
        private var rooms: List<RoomSummary> = emptyList()

        /** 다 본 덱과 **빈 덱**. 빈 덱을 같이 담는 것이 EC-009·EC-013을 예외 분기 없이 흡수하는 지점이다. */
        private val exhausted = mutableSetOf<DeckKey>()

        /** 예고 툴팁을 이미 띄운 덱. 되돌리기로 잔여가 늘었다 줄어도 다시 띄우지 않는다(spec §4 가정). */
        private val previewedDecks = mutableSetOf<DeckKey>()

        /** 허용받은 좌표. 거부는 `null`로 남아 다음 `가까운순`에서 다시 묻는다(EC-009). */
        private var grantedLocation: GeoPoint? = null

        /** 권한 응답을 기다리는 방. 응답이 왔을 때 어느 방의 `가까운순`이었는지는 상태에 남지 않는다. */
        private var roomAwaitingLocation: RoomSummary? = null

        /** 홈에 들어온 뒤 카드를 한 장이라도 띄웠는가. 완료 안내와 빈 상태 안내를 가르는 값이다(EC-011). */
        private var hasShownCard = false

        private var tooltipJob: Job? = null

        init {
            start()
        }

        fun processIntent(intent: HomeIntent) {
            // 가이드는 phase와 직교하므로 어느 화면에서든 여기서 먼저 막힌다(FR-019, EC-016).
            // 막는 것은 사용자 조작이다 — 권한 다이얼로그 응답은 시스템이 돌려주는 것이라 버리면 덱을 받을
            // 차례가 영영 오지 않는다(EC-009).
            if (state.value.isGuideVisible &&
                intent != HomeIntent.DismissGuide &&
                intent !is HomeIntent.LocationPermissionResult
            ) {
                return
            }

            when (intent) {
                HomeIntent.SwipeForward -> swipeForward()
                HomeIntent.SwipeBackward -> swipeBackward()
                HomeIntent.TransitionSettled -> updateState { copy(isTransitioning = false) }
                is HomeIntent.SelectSort -> selectSort(intent.sort)
                is HomeIntent.OpenActionMenu -> updateState { copy(actionMenuTarget = intent.pinId) }
                HomeIntent.DismissActionMenu -> updateState { copy(actionMenuTarget = null) }
                is HomeIntent.SaveToAnotherRoom -> saveToAnotherRoom(intent.pinId)
                is HomeIntent.ToggleSaveTargetRoom -> toggleSaveTargetRoom(intent.roomId)
                HomeIntent.ConfirmSaveTargets -> confirmSaveTargets()
                HomeIntent.DismissSavePicker -> updateState { copy(savePicker = null) }
                is HomeIntent.OpenPlaceDetail -> openPlaceDetail(intent.pinId)
                HomeIntent.OpenRoomSheet -> updateState { copy(isRoomSheetOpen = true) }
                is HomeIntent.SelectRoom -> selectRoom(intent.roomId)
                HomeIntent.DismissRoomSheet -> dismissRoomSheet()
                HomeIntent.DismissGuide -> dismissGuide()
                is HomeIntent.LocationPermissionResult -> onLocationPermissionResult(intent.location)
            }
        }

        /**
         * 시작 방은 마지막으로 보던 방, 없으면 개인방이다(FR-022, TS-032·033).
         *
         * 저장은 **바뀌었을 때만** 한다. 들어올 때마다 같은 값을 다시 쓰면 무엇이 사용자의 방 변경이었는지
         * 기록에서 구별되지 않는다.
         */
        private fun start() =
            launchSafely {
                val guideDismissed = homePreferencesRepository.isGuideDismissed()
                updateState { copy(isGuideVisible = !guideDismissed) }

                rooms = getRoomSummaries() ?: return@launchSafely
                // 순회 판정은 위 필드가 하고, 방 시트가 그릴 목록만 상태로 함께 올린다(FR-018).
                val listedRooms = rooms.toImmutableList()
                updateState { copy(rooms = listedRooms) }

                val lastRoomId = homePreferencesRepository.getLastRoomId()
                val startRoom =
                    rooms.firstOrNull { it.id == lastRoomId }
                        ?: rooms.firstOrNull { it.type == RoomType.PERSONAL }
                        ?: rooms.firstOrNull()
                if (startRoom == null) {
                    updateState { copy(phase = HomePhase.EMPTY) }
                    return@launchSafely
                }
                if (startRoom.id != lastRoomId) homePreferencesRepository.setLastRoomId(startRoom.id)

                updateState { copy(room = startRoom) }
                openDeck(startRoom, DeckSort.GGUK_PICK)
            }

        /**
         * 좌→우 넘김. **서버를 부르지 않는다** — 넘김은 화면 안에서 끝나는 「카드 열람 확인」이다(FR-023, TS-035).
         *
         * 액션 메뉴가 열려 있으면 메뉴만 닫고 카드에 반영하지 않으며(EC-004), 전환 중이면 버린다. 큐에 쌓지
         * 않으므로 전환이 끝나도 되살아나지 않는다(UX-001, R-007).
         */
        private fun swipeForward() {
            if (closeActionMenuIfOpen() || state.value.isTransitioning) return
            val top = state.value.cards.firstOrNull() ?: return

            updateState {
                copy(
                    cards = cards.drop(1).toImmutableList(),
                    undoStack = (undoStack + top).toImmutableList(),
                    isTransitioning = true,
                )
            }
            if (state.value.cards.isEmpty()) exhaustCurrentDeck() else showDeckAhead()
        }

        /**
         * 우→좌 되돌리기. 넘긴 순서의 **역순으로 한 장씩** 되돌린다(FR-002, `data-model.md` §2.2) —
         * 이 덱에서 넘긴 카드가 남아 있는 한 몇 번이든 이어서 되돌아간다.
         *
         * [SCR-006]이 이미 기록한 「경과일 초기화 확인」은 취소하지 않는다 — 보상 호출을 흘리지 않는 것이 EC-017이다.
         * 되돌릴 것이 없으면 상태를 건드리지 않는다(EC-001).
         */
        private fun swipeBackward() {
            if (closeActionMenuIfOpen() || state.value.isTransitioning) return
            val restored = state.value.undoStack.lastOrNull() ?: return

            updateState {
                copy(
                    cards = (listOf(restored) + cards).toImmutableList(),
                    undoStack = undoStack.dropLast(1).toImmutableList(),
                    isTransitioning = true,
                )
            }
            rearmDeckAhead()
        }

        /**
         * 되돌려서 잔여가 임계값 위로 올라갔으면 예고를 **다시 띄울 수 있게** 푼다(FR-015).
         *
         * 노출 이력이 덱 단위로 굳어 있으면, 되돌리기로 덱이 다시 길어져도 임계값을 다시 지날 때 아무 안내가
         * 없다. 사용자에게는 「2장 남았는데 예고가 안 뜬다」로만 보인다 — 잔여 2장과 예고를 잇는 규칙이
         * 조용히 깨진 것이다.
         *
         * 임계값 **위로 올라갔을 때만** 푼다. 2장에서 되돌려 3장이 되지 않는 한 되돌릴 때마다 다시 뜨지 않는다.
         */
        private fun rearmDeckAhead() {
            val room = state.value.room ?: return
            if (state.value.cards.size <= DECK_AHEAD_THRESHOLD) return
            previewedDecks -= DeckKey(roomId = room.id, sort = state.value.sort)
        }

        /** 메뉴가 열려 있었으면 닫고 `true`. 스와이프도 바깥 탭도 결과가 같다(EC-004·005). */
        private fun closeActionMenuIfOpen(): Boolean {
            if (state.value.actionMenuTarget == null) return false
            updateState { copy(actionMenuTarget = null) }
            return true
        }

        /**
         * 카드 본문 탭. 상세로 보내는 것이 전부이고 **덱의 진행 상태를 어느 것도 건드리지 않는다**(FR-023, TS-013).
         *
         * **「경과일 초기화 확인」을 여기서 보내지 않는다**(FR-007·023, TS-034). 이동한 [SCR-006] 장소 상세가
         * 열리면서 기록하며(`docs/specs/place-detail/spec.md` FR-026 — 진입 경로와 무관하게 기록한다), 홈까지
         * 부르면 같은 `POST /pins/{pinId}/accesses`가 카드 한 번 탭에 두 건 쌓인다.
         *
         * **[PlaceRepository]가 주입돼 있어도 [PlaceRepository.recordAccess]는 부르지 않는다.** 그 의존은
         * `다른 방 저장`의 [PlaceRepository.duplicatePin] 때문에 있는 것이지 기록 때문이 아니다 — R-019가
         * 처음 세울 때는 기록도 이 경로였으나 spec 4.0.0이 소유를 [SCR-006]으로 넘겼다.
         */
        private fun openPlaceDetail(pinId: String) =
            launchSafely { postSideEffect(HomeSideEffect.NavigateToPlaceDetail(pinId)) }

        /**
         * 정렬 칩 직접 선택(FR-010, TS-020·021).
         *
         * 보던 덱을 소진으로 넣지 않는다 — 건너뛴 덱은 방을 넘기기 전에 다시 와야 한다.
         */
        private fun selectSort(sort: DeckSort) =
            launchSafely {
                val room = state.value.room ?: return@launchSafely
                if (sort == state.value.sort) return@launchSafely
                openDeck(room, sort)
            }

        /**
         * 액션 메뉴의 `다른 방 저장`. 메뉴를 닫고 「방 선택 시트」를 여는 것까지가 여기다(TS-011).
         * 덱도 「홈 방 시트」도 건드리지 않는다(FR-005) — 서로 다른 시트라 [HomeUiState.isRoomSheetOpen]과
         * 값을 공유하지 않는다.
         */
        private fun saveToAnotherRoom(pinId: String) {
            updateState { copy(actionMenuTarget = null, savePicker = SavePickerState(pinId = pinId)) }
        }

        /** 「방 선택 시트」의 체크박스 탭. 시트가 닫혀 있으면 무시한다. */
        private fun toggleSaveTargetRoom(roomId: String) {
            val picker = state.value.savePicker ?: return
            val selected =
                if (roomId in picker.selectedRoomIds) {
                    picker.selectedRoomIds - roomId
                } else {
                    picker.selectedRoomIds + roomId
                }
            updateState { copy(savePicker = picker.copy(selectedRoomIds = selected)) }
        }

        /**
         * 「방 선택 시트」의 `저장하기`. 선택이 비어 있으면 확정하지 않는다(EC-018).
         *
         * 성공과 실패가 서로 다른 통로로 나간다 — 실패는 사용자 액션의 일회성 실패라 SideEffect가 아니라
         * `emitDomainError`다(`docs/conventions/error_handling.md` §5). **성패와 무관하게 시트는 닫는다** —
         * 실패해도 다시 고르게 붙잡아 두지 않는다. 덱·현재 방·되돌리기 이력 어느 것도 건드리지 않는다.
         */
        private fun confirmSaveTargets() {
            val picker = state.value.savePicker ?: return
            if (picker.selectedRoomIds.isEmpty()) return
            launchSafely {
                updateState { copy(savePicker = null) }
                runCatchingDomain { placeRepository.duplicatePin(picker.pinId, picker.selectedRoomIds.toList()) }
                    .onSuccess { postSideEffect(HomeSideEffect.ShowSaveResult) }
                    .onDomainFailure(::emitDomainError)
            }
        }

        /** 방을 고르지 않고 닫는다 — 저장도 없던 일이다. */
        private fun dismissRoomSheet() {
            updateState { copy(isRoomSheetOpen = false) }
        }

        /**
         * 「홈 방 시트」의 방 선택(FR-024, SC-008). 보던 방을 다시 골랐으면 시트만 닫는다(EC-014).
         *
         * [ResolveRoomEntryDeckUseCase]로 그 방 안에서만 다음 칸을 고른다 — [advance]로 넘기면 그 방이
         * 소진일 때 다른 방으로 튕긴다.
         */
        private fun selectRoom(roomId: String) {
            val room = rooms.firstOrNull { it.id == roomId } ?: return
            if (room.id == state.value.room?.id) {
                updateState { copy(isRoomSheetOpen = false) }
                return
            }
            launchSafely {
                homePreferencesRepository.setLastRoomId(room.id)
                enterRoom(room)
            }
        }

        /**
         * 정렬을 `꾹 Pick`으로 되감아 그 방에 들어간다. 세 칸 모두 소진이면 그 방을 단 채 완료 안내로 간다.
         *
         * 고른 덱이 막상 비어 있으면 [advance]로 다른 방으로 튕기지 않고, [openDeck]에 자기 자신을
         * `onExhausted`로 넘겨 같은 방 안에서 다시 판정한다 — [ResolveRoomEntryDeckUseCase]는 애초에
         * [NextDeck.NextRoom]을 내지 않는다(FR-024, SC-008). 재귀는 최대 3단계(정렬 셋을 각각 한 번씩
         * 소진 처리)로 반드시 [NextDeck.AllExhausted]에 닿아 끝난다.
         */
        private suspend fun enterRoom(room: RoomSummary) {
            when (val next = resolveRoomEntryDeck(deckContext(room.id, exhausted), room.id)) {
                is NextDeck.SameRoom -> {
                    updateState {
                        copy(room = room, isRoomSheetOpen = false, undoStack = persistentListOf())
                    }
                    showTooltip(HomeTooltip.RoomChanged(room.name))
                    openDeck(room, next.sort, onExhausted = ::enterRoom)
                }

                NextDeck.AllExhausted ->
                    updateState {
                        copy(
                            room = room,
                            isRoomSheetOpen = false,
                            sort = DeckSort.GGUK_PICK,
                            phase = HomePhase.ALL_EXHAUSTED,
                            cards = persistentListOf(),
                            isTransitioning = false,
                            undoStack = persistentListOf(),
                            loadError = null,
                        )
                    }

                is NextDeck.NextRoom ->
                    error("ResolveRoomEntryDeckUseCase는 NextRoom을 내지 않는다 — 계약 위반")
            }
        }

        private fun dismissGuide() =
            launchSafely {
                updateState { copy(isGuideVisible = false) }
                homePreferencesRepository.dismissGuide()
            }

        /**
         * 권한 응답. **거부는 방별 값이 아니다** — `가까운순 × 모든 방`을 통째로 소진 집합에 넣고 판정을
         * 다시 불러 방마다 다시 묻지 않는다(EC-009).
         */
        private fun onLocationPermissionResult(location: GeoPoint?) {
            val room = roomAwaitingLocation ?: return
            roomAwaitingLocation = null
            grantedLocation = location
            if (location == null) {
                rooms.forEach { exhausted += DeckKey(roomId = it.id, sort = DeckSort.NEAREST) }
                launchSafely { advance(room) }
            } else {
                launchSafely { loadDeck(room, DeckSort.NEAREST, location) }
            }
        }

        /**
         * 방 전환. 수동·자동을 구분하지 않는다(FR-012·016). 되돌리기 이력은 비운다(EC-003).
         *
         * [sort]는 호출자가 정한다 — 자동 전환은 [NextDeck.NextRoom]이 실어 온 정렬을 그대로 쓰고
         * 초기화하지 않는다(FR-012·016).
         */
        private suspend fun switchRoom(
            room: RoomSummary,
            sort: DeckSort,
        ) {
            homePreferencesRepository.setLastRoomId(room.id)
            updateState {
                copy(
                    room = room,
                    sort = sort,
                    isRoomSheetOpen = false,
                    undoStack = persistentListOf(),
                )
            }
            showTooltip(HomeTooltip.RoomChanged(room.name))
            openDeck(room, sort)
        }

        /**
         * 덱 하나를 연다.
         *
         * 저장 장소가 0개인 방은 어느 정렬로도 빈 덱이므로 요청도 권한 요구도 없이 통째로 소진 처리한다
         * (FR-013). 그러지 않으면 볼 것이 하나도 없는 사용자가 빈 덱을 받으려고 위치 권한부터 마주한다.
         */
        private suspend fun openDeck(
            room: RoomSummary,
            sort: DeckSort,
            onExhausted: suspend (RoomSummary) -> Unit = ::advance,
        ) {
            if (room.placeCount == 0) {
                DeckSort.entries.forEach { exhausted += DeckKey(roomId = room.id, sort = it) }
                onExhausted(room)
                return
            }
            if (sort == DeckSort.NEAREST && grantedLocation == null) {
                roomAwaitingLocation = room
                updateState { copy(phase = HomePhase.LOADING, isTransitioning = false) }
                postSideEffect(HomeSideEffect.RequestLocationPermission)
                return
            }
            loadDeck(room, sort, grantedLocation.takeIf { sort == DeckSort.NEAREST }, onExhausted)
        }

        /** 받아 온 덱이 0장이면 **노출하지 않고** 소진으로 보고 규칙을 다시 적용한다(EC-013). */
        private suspend fun loadDeck(
            room: RoomSummary,
            sort: DeckSort,
            location: GeoPoint?,
            onExhausted: suspend (RoomSummary) -> Unit = ::advance,
        ) {
            val deck =
                runCatchingDomain { homeDeckRepository.getDeck(room.id, sort, location) }
                    .onDomainFailure(::showLoadError)
                    .getOrNull() ?: return

            if (deck.cards.isEmpty()) {
                exhausted += DeckKey(roomId = room.id, sort = sort)
                onExhausted(room)
                return
            }

            hasShownCard = true
            updateState {
                copy(
                    phase = HomePhase.DECK,
                    sort = sort,
                    cards = deck.cards.toImmutableList(),
                    isTransitioning = false,
                    undoStack = persistentListOf(),
                    loadError = null,
                )
            }
            showDeckAhead()
        }

        /** 보던 덱을 소진 처리하고 다음으로 넘긴다. */
        private fun exhaustCurrentDeck() =
            launchSafely {
                val room = state.value.room ?: return@launchSafely
                exhausted += DeckKey(roomId = room.id, sort = state.value.sort)
                advance(room)
            }

        /**
         * 다음에 무엇을 보여줄지 [ResolveNextDeckUseCase]에 묻고 그대로 따른다 — 규칙은 여기 있지 않다.
         *
         * 소진 집합을 들고 매번 다시 묻는 것이 FR-011이다. 방에 들어올 때 계산해 두면 그 사이 비워진 덱이나
         * 거부된 권한이 반영되지 않는다.
         */
        private suspend fun advance(fromRoom: RoomSummary) {
            when (val next = resolveNextDeck(deckContext(fromRoom.id, exhausted))) {
                is NextDeck.SameRoom -> openDeck(fromRoom, next.sort)
                is NextDeck.NextRoom ->
                    rooms.firstOrNull { it.id == next.roomId }?.let { switchRoom(it, next.sort) }

                NextDeck.AllExhausted ->
                    updateState {
                        copy(
                            // 볼 것이 있었는지가 완료 안내와 빈 상태 안내를 가른다(EC-011, FR-020).
                            phase = if (hasShownCard) HomePhase.ALL_EXHAUSTED else HomePhase.EMPTY,
                            // 남은 칸이 없어 도달한 화면이라 칩은 마지막 정렬이 아니라 `꾹 Pick`에 머문다(FR-014).
                            sort = DeckSort.GGUK_PICK,
                            cards = persistentListOf(),
                            isTransitioning = false,
                            undoStack = persistentListOf(),
                            loadError = null,
                        )
                    }
            }
        }

        /**
         * 잔여 2장 이하가 되면 **실제로 다음에 올** 덱·방을 예고한다(FR-015, TS-022, EC-012).
         *
         * 지금 덱을 소진 집합에 **넣어서** 묻는다. 넣지 않으면 판정이 [DeckSort] 선언 순서를 처음부터 훑어
         * 방금 넘기던 덱을 그대로 돌려주고, 예고가 지금 보고 있는 덱을 가리킨다.
         *
         * 가리킬 대상이 없으면 띄우지 않는다(TS-023).
         */
        private fun showDeckAhead() {
            val room = state.value.room ?: return
            val current = DeckKey(roomId = room.id, sort = state.value.sort)
            if (state.value.cards.size > DECK_AHEAD_THRESHOLD || !previewedDecks.add(current)) return

            val tooltip =
                when (val next = resolveNextDeck(deckContext(room.id, exhausted + current))) {
                    is NextDeck.SameRoom -> HomeTooltip.DeckAhead.NextSort(next.sort)
                    is NextDeck.NextRoom ->
                        rooms.firstOrNull { it.id == next.roomId }?.let { HomeTooltip.DeckAhead.NextRoom(it.name) }

                    NextDeck.AllExhausted -> null
                }
            showTooltip(tooltip ?: return)
        }

        /**
         * 툴팁은 한 번에 하나이고 마지막 것이 이긴다(R-008) — 앞선 타이머를 끊어야 새 툴팁이 그 시계에
         * 걸려 일찍 사라지지 않는다.
         */
        private fun showTooltip(tooltip: HomeTooltip) {
            tooltipJob?.cancel()
            updateState { copy(tooltip = tooltip) }
            tooltipJob =
                launchSafely {
                    delay(TOOLTIP_DURATION)
                    updateState { copy(tooltip = null) }
                }
        }

        private fun deckContext(
            roomId: String,
            exhausted: Set<DeckKey>,
        ): DeckContext =
            DeckContext(
                rooms = rooms,
                currentRoomId = roomId,
                currentSort = state.value.sort,
                exhausted = exhausted,
            )

        private suspend fun getRoomSummaries(): List<RoomSummary>? =
            runCatchingDomain { homeDeckRepository.getRoomSummaries() }
                .onDomainFailure(::showLoadError)
                .getOrNull()

        /**
         * 화면의 주 데이터(방 목록·덱)를 못 받은 것은 상태다 — 스낵바로만 흘리면 [HomePhase.LOADING]이
         * 걷히지 않아 화면이 로딩에 갇힌다(`docs/conventions/error_handling.md` §5 1행).
         *
         * 담는 것은 문구가 아니라 리프다. 사용자 액션의 일회성 실패는 여기로 오지 않는다(같은 표 2행).
         */
        private fun showLoadError(error: MinoDomainException) =
            updateState { copy(phase = HomePhase.ERROR, isTransitioning = false, loadError = error) }

        private companion object {
            /** 예고 툴팁이 뜨는 잔여 카드 수(FR-015). */
            const val DECK_AHEAD_THRESHOLD = 2

            /** 툴팁 노출 시간(FR-015·016). */
            val TOOLTIP_DURATION = 3.seconds
        }
    }
