package team.mino.feature.home.main.vm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import team.mino.core.domain.model.DeckSort
import team.mino.core.domain.model.PlaceCard
import team.mino.core.domain.model.PlaceLabel
import team.mino.core.domain.model.Registrant
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.model.RoomType
import team.mino.core.domain.usecase.ResolveNextDeckUseCase
import team.mino.feature.home.fake.FakeHomeDeckRepository
import team.mino.feature.home.fake.FakeHomePreferencesRepository
import team.mino.feature.home.fake.FakePlaceRepository
import team.mino.feature.home.main.model.HomePhase
import team.mino.feature.home.main.model.HomeTooltip

/**
 * 「홈 방 시트」를 열고, 방을 고르고, 그냥 닫는 세 갈래를 판정한다.
 *
 * 다루는 범위는 TS-025·026(뱃지·캐릭터로 열기), TS-028·028a·028b·028c(방 선택 즉시 적용과 그 안의
 * 정렬 초기화·소진 처리), EC-014(현재 방 재선택)·EC-020(장소 0개인 방)·EC-021(소진한 방 재선택)이다.
 * 규칙의 원문은 `spec.md` FR-017·FR-018·FR-024와 `contracts/home-ui.md` §2·§4.1이 소유한다.
 *
 * **시트를 여는 두 입구(방 뱃지·방 캐릭터)는 여기서 한 케이스다.** 계약이 둘을 같은
 * [HomeIntent.OpenRoomSheet] 하나로 받기로 했으므로(§2), ViewModel은 어디를 눌러 왔는지 알지 못한다.
 * 두 입구가 실제로 그 의도를 쏘는지는 Compose 계층이 소유한다.
 *
 * **여기서 보지 않는 것**과 그 자리를 메우는 것:
 *
 * | 미검증 | 메우는 것 |
 * |---|---|
 * | 3열 그리드·70dp 썸네일·첫 칸 `방 만들기`(FR-018, TS-027) | 시트 Composable — 이번 실행에서 보류 |
 * | 장소 0개인 방도 시트에서 체크·비활성으로 막지 않는 것(EC-022) | 시트 Composable(T101) |
 * | EC-015 `방 만들기` 선택 → [HomeSideEffect.NavigateToRoomForm] | **대응 Intent가 계약에 없다.** 첫 칸은 방 선택이 아니라 별개 입구여서 [HomeIntent.SelectRoom]으로 대신 쏠 수 없다 |
 * | 방 전환 툴팁이 3초 뒤 사라지는 것(FR-016) | 툴팁 수명을 소유하는 전환 테스트(T041) |
 * | 가이드가 떠 있는 동안 시트 Intent가 버려지는 것(FR-019, TS-030) | 가이드 테스트(T056) |
 *
 * TS-028이 말하는 툴팁도 **떴는지까지만** 본다 — 3초라는 수명은 방 전환 전반의 규칙이라 이 파일의 것이 아니다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelRoomSheetTest {
    private val deckRepository = FakeHomeDeckRepository()
    private val preferencesRepository = FakeHomePreferencesRepository()
    private val placeRepository = FakePlaceRepository()

    @Before
    fun setUp() {
        // viewModelScope가 Main에서 돌아 첫 덱 적재가 생성 직후 끝나 있도록 한다.
        Dispatchers.setMain(UnconfinedTestDispatcher())

        deckRepository.rooms = listOf(roomSummary(ROOM_ID), roomSummary(OTHER_ROOM_ID))

        // 시작 방을 못박아 둔다 — 어느 방에서 시작하는가는 FR-022의 판정 대상이고 이 파일의 것이 아니다.
        preferencesRepository.lastRoomId = ROOM_ID

        // 가이드가 떠 있으면 DismissGuide를 뺀 모든 Intent가 버려진다(FR-019). 시트를 보려면 닫힌 기기여야 한다.
        preferencesRepository.guideDismissed = true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * 시트를 여는 것은 **상태 한 칸만 바꾸는 일**이다(FR-017, TS-025·026).
     *
     * 상태 전체를 비교하는 이유는 여는 김에 덱을 다시 받아 오는 구현을 걸러내기 위해서다 — 그런 구현은 시트를
     * 여닫는 것만으로 보던 카드가 처음으로 되돌아간다. 덱 요청 수까지 함께 확인해 "받아 왔지만 같은 값이라
     * 상태는 그대로"인 경우도 배제한다.
     */
    @Test
    fun `시트를 열면 시트만 열리고 덱은 손대지 않는다`() =
        runTest {
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards(count = 3))
            val viewModel = createViewModel()
            val before = viewModel.state.value
            val deckRequestsBefore = deckRepository.deckRequests.size

            assertFalse("시트는 닫힌 채로 시작한다", before.isRoomSheetOpen)

            viewModel.processIntent(HomeIntent.OpenRoomSheet)

            assertEquals(before.copy(isRoomSheetOpen = true), viewModel.state.value)
            assertEquals(
                "시트를 여는 것만으로 덱을 다시 받으면 안 된다",
                deckRequestsBefore,
                deckRepository.deckRequests.size,
            )
        }

    /**
     * 방 카드를 누르는 것이 곧 확정이다(FR-018, TS-028). 체크박스도 확정 버튼도 없으므로
     * [HomeIntent.SelectRoom] 하나가 도착하면 시트는 닫혀 있고 새 방의 덱이 이미 실려 있어야 한다.
     *
     * **정렬을 기본값에서 옮겨 두고 시작한다.** 그러지 않으면 `꾹 Pick`으로 돌아왔다는 판정이 아무것도 하지 않은
     * 구현에서도 성립한다(FR-012·013). 이 「정렬이 `꾹 Pick`으로 되감긴다」가 TS-028a다.
     *
     * 마지막 방 저장(FR-022)을 여기서 함께 보는 것은 그것이 **상태로 드러나지 않기 때문**이다. 다음 실행에서만
     * 보이는 쓰기라 여기서 세지 않으면 통째로 빠뜨린 구현이 통과한다.
     */
    @Test
    fun `시트에서 다른 방을 고르면 시트가 닫히고 그 방의 꾹 Pick 덱이 실린다`() =
        runTest {
            val otherGgukPick = cards(count = 4, prefix = "other-gguk")
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards(count = 3, prefix = "gguk"))
            deckRepository.setDeck(ROOM_ID, DeckSort.LATEST, cards(count = 2, prefix = "latest"))
            deckRepository.setDeck(OTHER_ROOM_ID, DeckSort.GGUK_PICK, otherGgukPick)
            val viewModel = createViewModel()

            viewModel.processIntent(HomeIntent.SelectSort(DeckSort.LATEST))
            viewModel.processIntent(HomeIntent.TransitionSettled)
            assertEquals("기본 정렬에 머물러 있으면 정렬 복귀를 판정할 수 없다", DeckSort.LATEST, viewModel.state.value.sort)

            viewModel.processIntent(HomeIntent.OpenRoomSheet)
            viewModel.processIntent(HomeIntent.SelectRoom(OTHER_ROOM_ID))

            val state = viewModel.state.value
            assertFalse("방 카드를 누르는 것이 곧 확정이라 시트가 그 자리에서 닫힌다", state.isRoomSheetOpen)
            assertEquals(OTHER_ROOM_ID, state.room?.id)
            assertEquals(DeckSort.GGUK_PICK, state.sort)
            assertEquals(otherGgukPick, state.cards.toList())
            assertEquals(HomeTooltip.RoomChanged(roomName(OTHER_ROOM_ID)), state.tooltip)
            assertTrue("덱이 바뀌면 이전 덱의 되돌리기 이력은 비워진다", state.undoStack.isEmpty())

            val request = deckRepository.deckRequests.last()
            assertEquals(OTHER_ROOM_ID, request.roomId)
            assertEquals(DeckSort.GGUK_PICK, request.sort)
            assertEquals(
                "방을 바꿨으면 다음 실행에서 돌아올 방이 저장돼야 한다",
                OTHER_ROOM_ID,
                preferencesRepository.recordedLastRoomIds.lastOrNull(),
            )
        }

    /**
     * 고른 방의 `꾹 Pick`이 이미 소진이면 **탐색 범위를 그 방 안으로 한정**해 남은 덱 중 최고 순위를 연다
     * (FR-024, TS-028b). 다른 방(room-1)이 `꾹 Pick`을 갖고 있어도 그리로 넘어가면 안 된다 —
     * `ResolveRoomEntryDeckUseCase`는 `NextRoom`을 절대 내지 않는다(`contracts/home-ui.md` §4.1).
     *
     * **다른 방(room-1)에도 미소진 `꾹 Pick`을 심어 둔다.** 그러지 않으면 자동 전환용
     * `ResolveNextDeckUseCase`로 처리해도 이 케이스가 우연히 통과한다 — room-1이 격자 순회에서 room-2보다
     * 앞자리라 자동 전환은 그리로 튕긴다. 이 테스트가 잡는 것이 바로 그 튕김이다.
     */
    @Test
    fun `시트에서 고른 방의 꾹 Pick이 소진이면 다른 방으로 넘기지 않고 그 방의 남은 정렬을 연다`() =
        runTest {
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards(count = 3, prefix = "room1-gguk"))
            deckRepository.setDeck(OTHER_ROOM_ID, DeckSort.GGUK_PICK, emptyList())
            val otherLatest = cards(count = 2, prefix = "room2-latest")
            deckRepository.setDeck(OTHER_ROOM_ID, DeckSort.LATEST, otherLatest)
            val viewModel = createViewModel()
            val deckRequestsBeforeSelect = deckRepository.deckRequests.size

            viewModel.processIntent(HomeIntent.OpenRoomSheet)
            viewModel.processIntent(HomeIntent.SelectRoom(OTHER_ROOM_ID))

            val state = viewModel.state.value
            assertFalse(state.isRoomSheetOpen)
            assertEquals("다른 방으로 넘어가면 안 된다 — FR-024·SC-008", OTHER_ROOM_ID, state.room?.id)
            assertEquals("소진된 꾹 Pick 대신 남은 최고 순위 정렬이 떠야 한다", DeckSort.LATEST, state.sort)
            assertEquals(otherLatest, state.cards.toList())

            val newRequests = deckRepository.deckRequests.drop(deckRequestsBeforeSelect)
            assertTrue(
                "room-2를 골랐는데 room-1로 덱 요청이 나가면 다른 방으로 튕긴 것이다",
                newRequests.all { it.roomId == OTHER_ROOM_ID },
            )
        }

    /**
     * 고른 방의 저장 장소가 0개면 세 정렬이 모두 후보 0건이라 **그 방의 뱃지·캐릭터를 단 채** 완료 안내로
     * 간다(FR-024, FR-014, TS-028c, EC-020). 다른 방(room-1)에 볼 것이 남아 있어도 그리로 넘기지 않는다
     * (SC-008).
     *
     * 덱 요청 수가 시작 때 그대로인 것으로 「넘기지 않았다」를 확인한다 — room-2는 저장 장소가 0개라 애초에
     * 덱을 묻지 않고, room-1은 이미 보고 있던 방이라 다시 물을 이유가 없다. 요청이 하나라도 늘면 room-1로
     * 자동 전환을 태운 것이다.
     */
    @Test
    fun `시트에서 저장 장소가 0개인 방을 고르면 그 방을 단 채 완료 안내로 간다`() =
        runTest {
            deckRepository.rooms = listOf(roomSummary(ROOM_ID), roomSummary(OTHER_ROOM_ID, placeCount = 0))
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards(count = 3))
            val viewModel = createViewModel()
            val deckRequestsBeforeSelect = deckRepository.deckRequests.size

            viewModel.processIntent(HomeIntent.OpenRoomSheet)
            viewModel.processIntent(HomeIntent.SelectRoom(OTHER_ROOM_ID))

            val state = viewModel.state.value
            assertFalse(state.isRoomSheetOpen)
            assertEquals("다른 방으로 넘어가면 안 된다 — SC-008", OTHER_ROOM_ID, state.room?.id)
            assertEquals(HomePhase.ALL_EXHAUSTED, state.phase)
            assertTrue(state.cards.isEmpty())
            assertEquals(
                "room-2는 애초에 덱을 묻지 않고, room-1로도 넘기지 않았으면 요청이 늘지 않는다",
                deckRequestsBeforeSelect,
                deckRepository.deckRequests.size,
            )
        }

    /**
     * **모든 방이 비어 있으면 시트로 방을 골라도 빈 상태다**(EC-011). 「고른 방만 비어 있으면 완료 안내,
     * 모든 방을 통틀어 볼 장소가 없으면 빈 상태」가 spec §5의 확정이고, 둘을 가르는 것은 `[공동방 만들기]` CTA다.
     *
     * 자동 순회(`advance`)는 이 판정을 이미 하고 있었으나 수동 선택(`enterRoom`)이 무조건 완료 안내로 보내,
     * 볼 장소가 하나도 없는 사용자가 시트를 열었다 닫는 것만으로 방 생성 CTA를 잃었다.
     */
    @Test
    fun `모든 방이 비어 있으면 시트에서 방을 골라도 빈 상태 안내가 유지된다`() =
        runTest {
            deckRepository.rooms =
                listOf(
                    roomSummary(ROOM_ID, placeCount = 0),
                    roomSummary(OTHER_ROOM_ID, placeCount = 0),
                )
            val viewModel = createViewModel()
            assertEquals("볼 장소가 없는 계정은 빈 상태로 시작한다", HomePhase.EMPTY, viewModel.state.value.phase)

            viewModel.processIntent(HomeIntent.OpenRoomSheet)
            viewModel.processIntent(HomeIntent.SelectRoom(OTHER_ROOM_ID))

            val state = viewModel.state.value
            assertEquals("고른 방을 단 채로 머문다 — SC-008", OTHER_ROOM_ID, state.room?.id)
            assertEquals(
                "볼 장소가 하나도 없으므로 완료 안내가 아니라 빈 상태다 — CTA를 잃으면 안 된다(EC-011, FR-020)",
                HomePhase.EMPTY,
                state.phase,
            )
        }

    /**
     * 현재 보고 있는 방을 다시 고르면 **시트만 닫는다**(EC-014).
     *
     * 넘긴 카드가 있는 상태에서 고르는 것이 핵심이다 — 덱을 다시 받는 구현은 이미 넘긴 카드가 되살아나고,
     * 사용자는 시트를 잘못 눌렀다는 이유로 보던 자리를 잃는다.
     *
     * 상태가 그대로인 것만으로는 **"다시 받아 왔는데 같은 값이라 티가 안 나는 것"** 과 구별되지 않는다. 그래서
     * 덱 요청 수와 방 목록 조회 수가 늘지 않았음을 함께 본다 — 이것이 EC-014의 실제 판정 근거다.
     */
    @Test
    fun `현재 보고 있는 방을 다시 고르면 시트만 닫히고 덱을 다시 구성하지 않는다`() =
        runTest {
            val ggukPick = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, ggukPick)
            val viewModel = createViewModel()

            // 지킬 「진행 상태」를 만들어 둔다. 아무것도 진행하지 않았으면 유지 여부를 판정할 수 없다.
            viewModel.processIntent(HomeIntent.SwipeForward)
            viewModel.processIntent(HomeIntent.TransitionSettled)
            viewModel.processIntent(HomeIntent.OpenRoomSheet)

            val before = viewModel.state.value
            val deckRequestsBefore = deckRepository.deckRequests.size
            val roomSummaryCallsBefore = deckRepository.getRoomSummariesCallCount
            val lastRoomWritesBefore = preferencesRepository.recordedLastRoomIds.size

            assertEquals("다시 고를 대상은 지금 보고 있는 방이어야 한다", ROOM_ID, before.room?.id)
            assertEquals("넘긴 카드가 빠져 있어야 진행 상태 유지를 판정할 수 있다", ggukPick.drop(1), before.cards.toList())
            assertTrue(before.isRoomSheetOpen)

            viewModel.processIntent(HomeIntent.SelectRoom(ROOM_ID))

            assertEquals(
                "시트가 닫히는 것 말고는 무엇도 달라지지 않는다 — 잔여 카드·정렬·되돌리기 이력 전부",
                before.copy(isRoomSheetOpen = false),
                viewModel.state.value,
            )
            assertEquals(
                "현재 방을 다시 골랐는데 덱 요청이 나가면 넘긴 카드가 되살아난다",
                deckRequestsBefore,
                deckRepository.deckRequests.size,
            )
            assertEquals(roomSummaryCallsBefore, deckRepository.getRoomSummariesCallCount)
            assertEquals(
                "바뀐 것이 없으므로 마지막 방을 다시 쓸 일도 없다",
                lastRoomWritesBefore,
                preferencesRepository.recordedLastRoomIds.size,
            )
        }

    /**
     * 이미 소진한 방(EC-020으로 완료 안내에 가 있는 방)을 시트에서 다시 골라도 **소진 상태가 되살아나지
     * 않는다**(FR-024, EC-021). 덱을 다시 채워 주면 같은 방을 반복해 고르는 것이 스펙아웃된
     * `장소 더 보기`의 우회로가 된다(spec §3.2) — 그래서 재선택은 새 덱 요청 없이 완료 안내 그대로여야 한다.
     *
     * **다른 방(room-1)을 한 번 거쳐 되돌아온다.** room-2가 계속 현재 방이면 이 재선택이 EC-014("현재 방
     * 재선택")와 구별되지 않는다 — room-1로 옮겼다가 다시 room-2를 골라야 "이미 소진한 다른 방을 다시
     * 고른" 것이 된다.
     */
    @Test
    fun `이미 소진한 방을 다시 골라도 소진 상태가 되살아나지 않는다`() =
        runTest {
            deckRepository.rooms = listOf(roomSummary(ROOM_ID), roomSummary(OTHER_ROOM_ID, placeCount = 0))
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards(count = 3))
            val viewModel = createViewModel()

            viewModel.processIntent(HomeIntent.OpenRoomSheet)
            viewModel.processIntent(HomeIntent.SelectRoom(OTHER_ROOM_ID))
            assertEquals(
                "이 테스트의 전제가 성립하려면 room-2가 먼저 완료 안내에 가 있어야 한다",
                HomePhase.ALL_EXHAUSTED,
                viewModel.state.value.phase,
            )

            // room-1로 옮겨 room-2가 "현재 방"이 아니게 만든다 — 그래야 다음 SelectRoom(room-2)이
            // EC-014(현재 방 재선택)가 아니라 EC-021(소진한 다른 방 재선택)이 된다.
            viewModel.processIntent(HomeIntent.OpenRoomSheet)
            viewModel.processIntent(HomeIntent.SelectRoom(ROOM_ID))
            assertEquals(ROOM_ID, viewModel.state.value.room?.id)

            val deckRequestsBeforeReselect = deckRepository.deckRequests.size

            viewModel.processIntent(HomeIntent.OpenRoomSheet)
            viewModel.processIntent(HomeIntent.SelectRoom(OTHER_ROOM_ID))

            val state = viewModel.state.value
            assertFalse(state.isRoomSheetOpen)
            assertEquals(OTHER_ROOM_ID, state.room?.id)
            assertEquals("소진 상태가 되살아나면 안 된다", HomePhase.ALL_EXHAUSTED, state.phase)
            assertTrue(state.cards.isEmpty())
            assertEquals(
                "이미 소진한 방을 다시 골랐는데 덱 요청이 나가면 소진 상태를 되살리려 한 것이다",
                deckRequestsBeforeReselect,
                deckRepository.deckRequests.size,
            )
        }

    /**
     * 방을 고르지 않고 닫으면 연 적 없는 것과 같은 상태로 돌아온다(FR-017). 여닫기가 진행 상태를 축내지 않는다.
     *
     * **전제부터 단언한다.** "아무 변화 없음"은 아무것도 하지 않는 구현에서도 성립하므로, 덱이 실려 있는지와
     * 시트가 실제로 열렸는지를 먼저 본다. 그러지 않으면 시트를 열지도 못하는 구현이 이 케이스만 통과한다.
     */
    @Test
    fun `방을 고르지 않고 시트를 닫으면 열기 전 상태로 돌아온다`() =
        runTest {
            val ggukPick = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, ggukPick)
            val viewModel = createViewModel()
            val before = viewModel.state.value
            val deckRequestsBefore = deckRepository.deckRequests.size

            assertEquals("닫기가 지켜야 할 진행 상태가 있어야 한다", ggukPick, before.cards.toList())

            viewModel.processIntent(HomeIntent.OpenRoomSheet)
            assertTrue("열리지 않은 시트를 닫는 것은 판정 대상이 아니다", viewModel.state.value.isRoomSheetOpen)

            viewModel.processIntent(HomeIntent.DismissRoomSheet)

            assertEquals(before, viewModel.state.value)
            assertEquals(deckRequestsBefore, deckRepository.deckRequests.size)
            assertTrue(
                "고르지 않고 닫았는데 방이 저장되면 다음 실행에서 엉뚱한 방이 열린다",
                preferencesRepository.recordedLastRoomIds.isEmpty(),
            )
        }

    private fun createViewModel(): HomeViewModel =
        HomeViewModel(
            homeDeckRepository = deckRepository,
            homePreferencesRepository = preferencesRepository,
            placeRepository = placeRepository,
            resolveNextDeck = ResolveNextDeckUseCase(),
        )

    /** 순서를 눈으로 구별할 수 있게 pinId에 번호를 매긴다. 카드의 나머지 필드는 판정에 쓰이지 않는다. */
    private fun cards(
        count: Int,
        prefix: String = "pin",
    ): List<PlaceCard> =
        List(count) { index ->
            PlaceCard(
                pinId = "$prefix-$index",
                placeName = "장소 $index",
                address = "서울시 어딘가 $index",
                imageUrls = emptyList(),
                label = PlaceLabel.WORTH_VISITING,
                registrant = Registrant(userId = "user-1", nickname = "민호", avatar = null),
            )
        }

    /** 방 전환 툴팁이 담는 것은 방 이름이라(FR-016) 세우는 쪽과 단언하는 쪽이 같은 규칙을 쓰게 한다. */
    private fun roomName(id: String): String = "$id 방"

    /**
     * 기본은 [RoomSummary.placeCount]를 0보다 크게 둔다(FR-013) — 방 전환 대상이 되려면 필요하다.
     * [placeCount]를 0으로 주면 TS-028c·EC-020·EC-021이 쓰는 「저장 장소가 0개인 방」이 된다.
     */
    private fun roomSummary(
        id: String,
        placeCount: Int = 10,
    ): RoomSummary =
        RoomSummary(
            id = id,
            name = roomName(id),
            description = "",
            type = RoomType.GROUP,
            color = RoomColor.GRAY,
            placeCount = placeCount,
            thumbnailImageUrls = emptyList(),
        )

    private companion object {
        const val ROOM_ID = "room-1"

        const val OTHER_ROOM_ID = "room-2"
    }
}
