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
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
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
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.home.fake.FakeHomeDeckRepository
import team.mino.feature.home.fake.FakeHomePreferencesRepository
import team.mino.feature.home.main.model.HomePhase
import java.io.IOException

/**
 * 덱을 싣는 것, 넘기는 것, 되돌리는 것 — 홈에서 카드 한 장이 겪는 전부를 판정한다.
 *
 * 다루는 범위는 TS-004·005(덱 구성), TS-001·002(넘김·되돌리기), TS-007(전환 중 입력), EC-001·003(되돌릴 것이
 * 없거나 덱이 바뀐 경우)이다. 규칙의 원문은 `spec.md`와 `data-model.md` §2.2가 소유한다.
 *
 * **여기서 보지 않는 것**과 그 자리를 메우는 것:
 *
 * | 미검증 | 메우는 것 |
 * |---|---|
 * | 넘김이 서버를 부르지 않고 상세 진입만 부른다(FR-023, TS-034·035, EC-017) | `HomeViewModel`의 확인 이벤트 테스트(T035) |
 * | 전환 규칙이 다음 덱을 무엇으로 고르는가(FR-011·012·013) | `ResolveNextDeckUseCase` 테스트 |
 * | 시작 방·정렬 칩·방 시트·가이드(FR-009·010·018·019·022) | 각각의 ViewModel 테스트(T041·T052·T056) |
 * | 좌측 영역 스와이프 무시·임계값 미만 드래그(FR-003, EC-002·006) | 제스처를 소유하는 Compose 계층 |
 * | 담긴 리프를 어떤 문구로 보여주는가 | 화면 — 매핑은 그리는 쪽의 몫이다(`error_handling.md` §5) |
 *
 * 마지막 줄이 이 파일의 경계다. [HomeIntent.SwipeForward]가 도착했다는 것은 **제스처 판정이 이미 끝났다**는
 * 뜻이므로, 어디를 어떻게 문질렀는지는 ViewModel이 알지 못하고 알 필요도 없다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelDeckTest {
    private val deckRepository = FakeHomeDeckRepository()
    private val preferencesRepository = FakeHomePreferencesRepository()

    @Before
    fun setUp() {
        // viewModelScope가 Main에서 돌아 첫 덱 적재가 생성 직후 끝나 있도록 한다.
        Dispatchers.setMain(UnconfinedTestDispatcher())

        deckRepository.rooms = listOf(roomSummary(ROOM_ID), roomSummary(OTHER_ROOM_ID))

        // 시작 방을 못박아 둔다 — 어느 방에서 시작하는가는 FR-022의 판정 대상이고 이 파일의 것이 아니다.
        preferencesRepository.lastRoomId = ROOM_ID

        // 가이드가 떠 있으면 DismissGuide를 뺀 모든 Intent가 버려진다(FR-019). 스와이프를 보려면 닫힌 기기여야 한다.
        preferencesRepository.guideDismissed = true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * 서버가 잘라 준 10장을 **그대로** 싣는다(FR-004, TS-004).
     *
     * 후보를 10장으로 자르는 것은 서버의 몫이라(`data-model.md` §1.3) 홈이 보는 것은 이미 잘린 덱이다.
     * 그래서 여기서 판정하는 것은 "10장이 되는가"가 아니라 **홈이 받은 것을 다시 자르거나 순서를 바꾸지 않는가**다.
     * 그러지 않으면 이 케이스는 서버 계약을 두 번 검사하는 테스트가 된다.
     */
    @Test
    fun `열 장짜리 덱은 열 장 그대로 순서까지 유지된 채 실린다`() =
        runTest {
            val cards = cards(count = 10)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)

            val state = createViewModel().state.value

            assertEquals(HomePhase.DECK, state.phase)
            assertEquals(cards, state.cards.toList())
        }

    /** 모자란 덱을 채우지 않는다(FR-004, TS-005). 4장은 4장으로 남고, 부족분을 다른 정렬에서 끌어오지 않는다. */
    @Test
    fun `네 장짜리 덱은 채워지지 않고 네 장으로 실린다`() =
        runTest {
            val cards = cards(count = 4)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            // 끌어올 후보가 옆 덱에 있어도 손대지 않는다.
            deckRepository.setDeck(ROOM_ID, DeckSort.LATEST, cards(count = 10, prefix = "latest"))

            val state = createViewModel().state.value

            assertEquals(HomePhase.DECK, state.phase)
            assertEquals(cards, state.cards.toList())
        }

    /** 좌→우 스와이프는 최상단 카드를 덜어내고 다음 카드를 올린다(FR-001, TS-001). 덜어낸 카드는 되돌릴 대상이 된다. */
    @Test
    fun `좌에서 우로 넘기면 최상단 카드가 빠지고 되돌릴 카드로 남는다`() =
        runTest {
            val cards = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            val viewModel = createViewModel()

            viewModel.processIntent(HomeIntent.SwipeForward)

            val state = viewModel.state.value
            assertEquals(cards.drop(1), state.cards.toList())
            assertEquals(listOf(cards.first()), state.undoStack.toList())
        }

    /**
     * 우→좌 스와이프는 방금 넘긴 카드를 최상단으로 되돌린다(FR-002, TS-002).
     *
     * 되돌린 카드는 [HomeUiState.undoStack]에서 **빠져야** 한다. 빼지 않으면 같은 카드를 몇 번이고 되살릴 수
     * 있어 덱에 사본이 쌓인다.
     */
    @Test
    fun `넘긴 직후 되돌리면 그 카드가 최상단으로 복구된다`() =
        runTest {
            val cards = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            val viewModel = createViewModel()

            viewModel.processIntent(HomeIntent.SwipeForward)
            viewModel.processIntent(HomeIntent.TransitionSettled)
            viewModel.processIntent(HomeIntent.SwipeBackward)

            val state = viewModel.state.value
            assertEquals(cards, state.cards.toList())
            assertTrue("되돌린 카드가 이력에 남으면 같은 카드가 두 번 복구된다", state.undoStack.isEmpty())
        }

    /**
     * 되돌리기는 넘긴 만큼 이어진다(FR-002, `data-model.md` §2.2).
     *
     * **역순임을 카드 순서로 본다.** 되돌린 장수만 세면 아무 순서로나 되살리는 구현도 통과하는데, 그러면
     * 사용자가 방금 넘긴 카드가 아닌 다른 카드를 마주한다.
     */
    @Test
    fun `두 장을 넘긴 뒤 두 번 되돌리면 넘긴 역순으로 모두 복구된다`() =
        runTest {
            val cards = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            val viewModel = createViewModel()

            repeat(2) {
                viewModel.processIntent(HomeIntent.SwipeForward)
                viewModel.processIntent(HomeIntent.TransitionSettled)
            }
            assertEquals(
                cards.take(2),
                viewModel.state.value.undoStack
                    .toList(),
            )

            viewModel.processIntent(HomeIntent.SwipeBackward)
            viewModel.processIntent(HomeIntent.TransitionSettled)
            assertEquals(
                "두 번째로 넘긴 카드가 먼저 돌아온다",
                cards.drop(1),
                viewModel.state.value.cards
                    .toList(),
            )

            viewModel.processIntent(HomeIntent.SwipeBackward)
            viewModel.processIntent(HomeIntent.TransitionSettled)

            val state = viewModel.state.value
            assertEquals("첫 카드까지 돌아와 원래 덱이 된다", cards, state.cards.toList())
            assertTrue("다 되돌렸으면 이력이 비어야 한다", state.undoStack.isEmpty())
        }

    /**
     * 되돌릴 카드가 없으면 아무 일도 일어나지 않는다(EC-001).
     *
     * 상태 전체를 비교하는 것이 이 케이스의 전부다 — 카드 수만 보면 덱을 다시 받아 같은 수가 된 구현도 통과한다.
     * 덱 요청이 늘지 않았음을 함께 확인해 "조용히 다시 받아 왔다"를 배제한다.
     *
     * **전제부터 단언한다.** "아무 변화 없음"은 아무것도 하지 않는 구현에서도 성립하므로, 되돌리기를 넣기 전에
     * 카드가 실제로 실려 있는지를 먼저 본다. 그러지 않으면 덱을 아예 싣지 않는 구현이 이 케이스만 통과한다.
     */
    @Test
    fun `첫 카드에서 되돌리면 상태가 그대로다`() =
        runTest {
            val cards = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            val viewModel = createViewModel()
            val before = viewModel.state.value
            val deckRequestsBefore = deckRepository.deckRequests.size

            assertEquals("되돌릴 것이 없는 상태는 덱 최상단이 첫 카드인 상태다", cards, before.cards.toList())
            assertTrue(before.undoStack.isEmpty())

            viewModel.processIntent(HomeIntent.SwipeBackward)

            assertEquals(before, viewModel.state.value)
            assertEquals(deckRequestsBefore, deckRepository.deckRequests.size)
        }

    /**
     * 덱이 바뀌면 되돌리기 이력이 초기화된다(EC-003, `data-model.md` §2.2).
     *
     * 이력이 남으면 우→좌 한 번에 **이전 덱의 카드가 새 덱 위에 얹힌다** — 정렬이 `최신순`인데 `꾹 Pick`의 카드가
     * 최상단에 있는 상태이고, 화면의 정렬 칩과 실제 카드가 어긋난다(UX-004).
     *
     * 여기서 전환 대상이 `최신순`인 것은 [ResolveNextDeckUseCase]가 정한 결과이지 이 테스트가 검증하는 규칙이
     * 아니다. 보는 것은 **전환이 일어난 뒤 되돌리기가 이전 덱에 닿지 않는다**는 것 하나다.
     */
    @Test
    fun `덱이 바뀐 직후 되돌리면 이전 덱의 카드가 돌아오지 않는다`() =
        runTest {
            val ggukPick = cards(count = 1, prefix = "gguk")
            val latest = cards(count = 2, prefix = "latest")
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, ggukPick)
            deckRepository.setDeck(ROOM_ID, DeckSort.LATEST, latest)
            val viewModel = createViewModel()

            // 마지막 한 장을 넘겨 덱을 소진시킨다.
            viewModel.processIntent(HomeIntent.SwipeForward)
            viewModel.processIntent(HomeIntent.TransitionSettled)
            assertEquals("소진되면 같은 방의 다음 덱으로 넘어가 있어야 한다", DeckSort.LATEST, viewModel.state.value.sort)
            assertTrue(
                "덱이 바뀌는 순간 이력이 비워져야 한다",
                viewModel.state.value.undoStack
                    .isEmpty(),
            )

            viewModel.processIntent(HomeIntent.SwipeBackward)

            val state = viewModel.state.value
            assertEquals(latest, state.cards.toList())
            assertTrue(state.undoStack.isEmpty())
        }

    /**
     * 전환이 도는 동안 도착한 스와이프는 **버려진다**(UX-001, TS-007, R-007).
     *
     * 큐에 쌓지 않는다는 것이 이 케이스의 핵심이다. 그래서 전환이 끝난 뒤([HomeIntent.TransitionSettled])
     * 한 번 더 확인한다 — 미뤄 두고 나중에 처리하는 구현은 여기서 카드가 두 장 빠져 걸린다(SC-005).
     */
    @Test
    fun `전환 중 도착한 넘김은 버려지고 전환이 끝난 뒤에도 되살아나지 않는다`() =
        runTest {
            val cards = cards(count = 5)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            val viewModel = createViewModel()

            viewModel.processIntent(HomeIntent.SwipeForward)
            assertTrue("넘김이 전환을 시작시키지 않으면 무시할 구간 자체가 없다", viewModel.state.value.isTransitioning)

            viewModel.processIntent(HomeIntent.SwipeForward)
            val duringTransition = viewModel.state.value.cards
            assertEquals("전환 중 두 번째 넘김이 통과하면 카드가 두 장 빠진다", cards.drop(1), duringTransition)

            viewModel.processIntent(HomeIntent.TransitionSettled)

            val state = viewModel.state.value
            assertFalse(state.isTransitioning)
            assertEquals("버린 입력이 전환 후에 재생되면 카드가 두 장 넘어간다", cards.drop(1), state.cards.toList())
        }

    /** 전환 중에는 되돌리기도 버려진다(UX-001, `contracts/home-ui.md` §2). 전환이 끝난 뒤 다시 넣어야 복구된다. */
    @Test
    fun `전환 중 도착한 되돌리기는 버려진다`() =
        runTest {
            val cards = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            val viewModel = createViewModel()

            viewModel.processIntent(HomeIntent.SwipeForward)
            viewModel.processIntent(HomeIntent.SwipeBackward)

            val duringTransition = viewModel.state.value.cards
            assertEquals("전환 중 되돌리기가 통과하면 넘어가던 카드가 되돌아온다", cards.drop(1), duringTransition)

            viewModel.processIntent(HomeIntent.TransitionSettled)
            viewModel.processIntent(HomeIntent.SwipeBackward)

            assertEquals(
                cards,
                viewModel.state.value.cards
                    .toList(),
            )
        }

    /**
     * 덱을 받아 오지 못하면 **로딩에서 걷혀야 한다**(`docs/conventions/error_handling.md` §5 표 1행).
     *
     * 실패를 스낵바로만 흘리는 구현은 스낵바가 사라진 뒤 [HomePhase.LOADING]만 남긴다 — 사용자에게는
     * 영원히 도는 화면이고, 무엇이 잘못됐는지도 다시 볼 수 없다.
     *
     * **리프가 그대로 담기는지까지 본다.** 규약이 요구하는 것은 문구가 아니라 [MinoDomainException]이다.
     * 문구로 바꿔 담으면 매핑이 ViewModel로 새고, 화면은 재시도할 대상을 잃는다.
     */
    @Test
    fun `덱 로드가 실패하면 로딩에 고착되지 않고 실패가 상태에 담긴다`() =
        runTest {
            val failure = MinoDomainException.Network(IOException("offline"))
            deckRepository.getDeckFailure = failure
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards(count = 3))

            val state = createViewModel().state.value

            assertEquals("로딩이 걷히지 않으면 화면이 영영 돈다", HomePhase.ERROR, state.phase)
            assertSame("문구가 아니라 리프를 담는다", failure, state.loadError)
            assertTrue("싣지 못한 덱을 화면에 남기지 않는다", state.cards.isEmpty())
        }

    /**
     * 방 목록이 막히면 그 뒤가 전부 막힌다 — 시작 방도, 첫 덱도 정해지지 않는다.
     *
     * 덱 실패보다 앞선 관문이라 따로 본다. 여기서 상태에 담기지 않으면 사용자는 **아무것도 뜨지 않는**
     * 홈을 마주하고, 덱 실패 경로만 고친 구현은 그 사실을 끝까지 모른다.
     */
    @Test
    fun `방 목록 로드가 실패하면 로딩에 고착되지 않고 실패가 상태에 담긴다`() =
        runTest {
            val failure = MinoDomainException.Http(code = 500, cause = IOException("서버 오류"))
            deckRepository.getRoomSummariesFailure = failure

            val state = createViewModel().state.value

            assertEquals(HomePhase.ERROR, state.phase)
            assertSame("문구가 아니라 리프를 담는다", failure, state.loadError)
            assertNull("방 목록을 못 받았으면 보고 있는 방도 없다", state.room)
        }

    /**
     * 다시 받아 오는 데 성공하면 실패의 흔적이 **함께** 걷힌다.
     *
     * 재시도 버튼이 아직 없으므로 회복 경로는 다른 방을 고르는 것 하나뿐이다(FR-018). [HomePhase]만 되돌리고
     * [HomeUiState.loadError]를 남기는 구현은 화면에 리프가 남아 다음 실패와 구별되지 않는다.
     */
    @Test
    fun `실패 뒤 다른 방을 골라 덱을 받아 오면 실패 흔적이 걷힌다`() =
        runTest {
            deckRepository.getDeckFailure = MinoDomainException.Network(IOException("offline"))
            val viewModel = createViewModel()
            assertEquals("회복을 보려면 먼저 실패해 있어야 한다", HomePhase.ERROR, viewModel.state.value.phase)

            val recovered = cards(count = 3, prefix = "other")
            deckRepository.getDeckFailure = null
            deckRepository.setDeck(OTHER_ROOM_ID, DeckSort.GGUK_PICK, recovered)
            viewModel.processIntent(HomeIntent.SelectRoom(OTHER_ROOM_ID))

            val state = viewModel.state.value
            assertEquals(HomePhase.DECK, state.phase)
            assertNull("리프가 남으면 성공한 화면에 지난 실패가 붙어 있다", state.loadError)
            assertEquals(recovered, state.cards.toList())
        }

    private fun createViewModel(): HomeViewModel =
        HomeViewModel(
            homeDeckRepository = deckRepository,
            homePreferencesRepository = preferencesRepository,
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

    /** 방 전환 대상이 되도록 [RoomSummary.placeCount]를 0보다 크게 둔다(FR-013). */
    private fun roomSummary(id: String): RoomSummary =
        RoomSummary(
            id = id,
            name = "$id 방",
            description = "",
            type = RoomType.GROUP,
            color = RoomColor.GRAY,
            placeCount = 10,
            thumbnailImageUrls = emptyList(),
        )

    private companion object {
        const val ROOM_ID = "room-1"

        const val OTHER_ROOM_ID = "room-2"
    }
}
