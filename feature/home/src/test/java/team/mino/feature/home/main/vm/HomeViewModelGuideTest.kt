package team.mino.feature.home.main.vm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
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
import team.mino.core.domain.usecase.ResolveRoomEntryDeckUseCase
import team.mino.feature.home.fake.FakeHomeDeckRepository
import team.mino.feature.home.fake.FakeHomePreferencesRepository
import team.mino.feature.home.fake.FakePlaceRepository
import team.mino.feature.home.main.model.HomePhase

/**
 * 홈 최초 진입 가이드 — 언제 뜨고, 떠 있는 동안 무엇을 막고, 왜 다시 뜨지 않는가.
 *
 * 다루는 범위는 TS-030(가이드 중 조작 차단)·TS-031(앱 생애 1회)과 EC-016(가이드가 떠 있는데 볼 카드가 없음)이다.
 * 규칙의 원문은 `spec.md` FR-019와 `contracts/home-ui.md` §2가 소유한다.
 *
 * **이 파일만 `guideDismissed`를 양쪽으로 세운다.** 다른 ViewModel 테스트는 스와이프를 보려고 setUp에서
 * `true`로 못박지만(가이드가 떠 있으면 넘김이 통째로 버려진다), 여기서는 `false`(최초 실행)와 `true`(닫은 이력)
 * 둘 다가 판정 대상이라 각 케이스가 직접 세운다.
 *
 * **여기서 보지 않는 것**과 그 자리를 메우는 것:
 *
 * | 미검증 | 메우는 것 |
 * |---|---|
 * | 딤·손 아이콘·안내 문구 2개의 노출(FR-019 후반, TS-029) | 가이드 오버레이 Composable — 이번 실행 보류(Figma 접근 차단) |
 * | 닫은 이력이 DataStore에 실제로 남는가 | `HomePreferencesRepository` 구현 테스트 |
 * | 빈 상태와 완료 상태를 어떻게 가르는가(FR-020, EC-011) | `HomeViewModel`의 전환 테스트(T041) |
 * | 가이드가 걷힌 뒤 스와이프가 정상 동작하는가(FR-001·002) | `HomeViewModelDeckTest` |
 *
 * 마지막 줄이 이 파일의 경계다. 여기서 보는 것은 **가이드가 조작을 통과시키지 않는다**까지이고, 통과한 조작이
 * 무엇을 하는지는 그 조작을 소유한 테스트의 몫이다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelGuideTest {
    private val deckRepository = FakeHomeDeckRepository()
    private val preferencesRepository = FakeHomePreferencesRepository()
    private val placeRepository = FakePlaceRepository()

    @Before
    fun setUp() {
        // viewModelScope가 Main에서 돌아 첫 덱 적재와 가이드 이력 조회가 생성 직후 끝나 있도록 한다.
        Dispatchers.setMain(UnconfinedTestDispatcher())

        deckRepository.rooms = listOf(roomSummary(ROOM_ID), roomSummary(OTHER_ROOM_ID))

        // 시작 방을 못박아 둔다 — 어느 방에서 시작하는가는 FR-022의 판정 대상이고 이 파일의 것이 아니다.
        preferencesRepository.lastRoomId = ROOM_ID
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * 닫은 이력이 없으면 가이드가 뜬다(FR-019, TS-029의 상태 부분).
     *
     * 이 케이스가 없으면 TS-031(`닫은 이력이 있으면 홈에 다시 들어와도 가이드가 뜨지 않는다`)은 **가이드를 아예
     * 구현하지 않은 코드에서도 통과한다** — `isGuideVisible`의 기본값이 `false`이기 때문이다. 「뜬다」를 먼저
     * 세워야 「뜨지 않는다」가 판정이 된다.
     */
    @Test
    fun `닫은 이력이 없는 첫 실행이면 홈 진입과 동시에 가이드가 뜬다`() =
        runTest {
            preferencesRepository.guideDismissed = false
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards(count = 3))

            val state = createViewModel().state.value

            assertTrue(state.isGuideVisible)
        }

    /**
     * 가이드가 떠 있는 동안 [HomeIntent.DismissGuide]를 뺀 모든 의도를 **버린다**(FR-019, TS-030).
     *
     * 상태 전체를 한 번에 비교하는 것이 이 케이스의 전부다 — 카드 수만 보면 시트가 열리거나 정렬이 바뀐 구현도
     * 통과한다. 저장소 호출까지 함께 보는 이유는 조작 중 셋이 상태에 흔적을 남기지 않기 때문이다.
     * `OpenPlaceDetail`은 상세 이동 SideEffect를, `SelectRoom`은 마지막 방 저장을, `SelectSort`는 덱 재요청을
     * 각각 상태 밖에서 일으킨다. 상태만 보면 **"화면은 그대로인데 상세로 넘어가고 DataStore에는 나갔다"** 를 놓친다.
     *
     * **전제부터 단언한다.** "아무 변화 없음"은 아무것도 하지 않는 구현에서도 성립하므로, 가이드가 실제로 떠
     * 있고 넘길 카드가 실려 있는지를 먼저 본다.
     */
    @Test
    fun `가이드가 떠 있는 동안 도착한 스와이프 정렬 시트 카드탭이 전부 버려진다`() =
        runTest {
            preferencesRepository.guideDismissed = false
            val cards = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            deckRepository.setDeck(ROOM_ID, DeckSort.LATEST, cards(count = 3, prefix = "latest"))
            val viewModel = createViewModel()

            val before = viewModel.state.value
            val deckRequestsBefore = deckRepository.deckRequests.size
            val effects = recordSideEffects(viewModel)
            assertTrue("가이드가 떠 있지 않으면 막을 구간 자체가 없다", before.isGuideVisible)
            assertEquals("버려짐을 보려면 넘길 카드가 실려 있어야 한다", cards, before.cards.toList())

            viewModel.processIntent(HomeIntent.SwipeForward)
            viewModel.processIntent(HomeIntent.SwipeBackward)
            viewModel.processIntent(HomeIntent.SelectSort(DeckSort.LATEST))
            viewModel.processIntent(HomeIntent.OpenRoomSheet)
            viewModel.processIntent(HomeIntent.SelectRoom(OTHER_ROOM_ID))
            viewModel.processIntent(HomeIntent.OpenActionMenu(cards.first().pinId))
            viewModel.processIntent(HomeIntent.OpenPlaceDetail(cards.first().pinId))

            assertEquals(before, viewModel.state.value)
            assertEquals("버려진 의도가 덱을 다시 받아오면 안 된다", deckRequestsBefore, deckRepository.deckRequests.size)
            assertEquals("카드 탭이 버려졌다면 상세로 넘어가지도 않는다", emptyList<HomeSideEffect>(), effects)
            assertEquals(
                "방 선택이 버려졌다면 마지막 방 저장도 나가지 않는다",
                emptyList<String>(),
                preferencesRepository.recordedLastRoomIds,
            )
            assertEquals("가이드는 DismissGuide로만 닫힌다", 0, preferencesRepository.dismissGuideCallCount)
        }

    /**
     * 버린다는 것은 **미루지 않는다**는 뜻이다(FR-019, `research.md` R-007의 큐 금지와 같은 결).
     *
     * 가이드가 걷힌 순간 밀린 스와이프가 한꺼번에 재생되면, 사용자는 딤 뒤에서 헛손질한 만큼 카드가 사라진
     * 화면을 만난다. 그래서 닫은 **뒤**를 한 번 더 본다 — 상태 비교만으로는 "미뤄 뒀다"와 "버렸다"가 구별되지 않는다.
     */
    @Test
    fun `가이드가 떠 있는 동안 버려진 넘김은 가이드를 닫은 뒤에도 되살아나지 않는다`() =
        runTest {
            preferencesRepository.guideDismissed = false
            val cards = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            val viewModel = createViewModel()

            repeat(2) { viewModel.processIntent(HomeIntent.SwipeForward) }
            viewModel.processIntent(HomeIntent.DismissGuide)

            assertEquals(
                "버린 입력이 가이드를 닫은 뒤 재생되면 카드가 두 장 빠진다",
                cards,
                viewModel.state.value.cards
                    .toList(),
            )
        }

    /**
     * 닫기는 통과하고, 닫은 이력이 **영속 저장된다**(FR-019, 유저 플로우 5의 3단계).
     *
     * 저장은 다음 설치 생애에서만 드러나므로 상태로는 보이지 않는다. `dismissGuide()`를 부르지 않고 상태만 끈
     * 구현은 앱을 다시 켤 때마다 가이드를 띄우고, 그 실패는 여기서만 잡힌다.
     */
    @Test
    fun `닫기를 누르면 가이드가 걷히고 닫은 이력이 저장된다`() =
        runTest {
            preferencesRepository.guideDismissed = false
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards(count = 3))
            val viewModel = createViewModel()

            viewModel.processIntent(HomeIntent.DismissGuide)

            assertFalse(viewModel.state.value.isGuideVisible)
            assertEquals("상태만 끄면 다음 실행에서 가이드가 또 뜬다", 1, preferencesRepository.dismissGuideCallCount)
        }

    /** 닫은 이력이 있으면 다시 뜨지 않는다 — 앱 생애 1회(FR-019, TS-031). */
    @Test
    fun `닫은 이력이 있으면 홈에 다시 들어와도 가이드가 뜨지 않는다`() =
        runTest {
            preferencesRepository.guideDismissed = true
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards(count = 3))

            val state = createViewModel().state.value

            assertFalse(state.isGuideVisible)
        }

    /**
     * 볼 카드가 하나도 없어도 가이드를 **먼저** 띄우고, 닫은 뒤에 빈 상태를 보여준다(EC-016).
     *
     * 가이드는 [HomePhase]와 직교한다는 것이 판정의 전부다. 「카드가 있을 때만 띄운다」로 구현하면 처음 설치한
     * 사용자 중 아직 방이 빈 사람은 조작 방법을 **영영 못 본다** — 이력은 남지 않았는데 띄울 기회도 지나간다.
     *
     * 방 둘을 [RoomSummary.placeCount] `0`으로 두는 것이 「애초에 볼 것이 없음」의 세움이다(EC-011). 덱을
     * 세우지 않은 조합은 빈 덱이므로 순회할 방도, 실을 카드도 없다.
     */
    @Test
    fun `볼 카드가 하나도 없어도 가이드를 먼저 띄우고 닫은 뒤 빈 상태가 남는다`() =
        runTest {
            preferencesRepository.guideDismissed = false
            deckRepository.rooms = listOf(emptyRoomSummary(ROOM_ID), emptyRoomSummary(OTHER_ROOM_ID))
            val viewModel = createViewModel()

            val before = viewModel.state.value
            assertTrue("빈 상태에서도 가이드가 먼저 떠야 한다", before.isGuideVisible)
            assertEquals(HomePhase.EMPTY, before.phase)

            viewModel.processIntent(HomeIntent.DismissGuide)

            val after = viewModel.state.value
            assertFalse(after.isGuideVisible)
            assertEquals("가이드를 걷어내도 빈 상태 안내는 남는다", HomePhase.EMPTY, after.phase)
        }

    /** [HomeSideEffect]는 Channel이라 흘려보낸 뒤에는 남지 않는다. 나온 순서대로 담아 두고 뒤에서 본다. */
    private fun TestScope.recordSideEffects(viewModel: HomeViewModel): List<HomeSideEffect> {
        val effects = mutableListOf<HomeSideEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.sideEffect.toList(effects) }
        return effects
    }

    private fun createViewModel(): HomeViewModel =
        HomeViewModel(
            homeDeckRepository = deckRepository,
            homePreferencesRepository = preferencesRepository,
            placeRepository = placeRepository,
            resolveNextDeck = ResolveNextDeckUseCase(),
            resolveRoomEntryDeck = ResolveRoomEntryDeckUseCase(),
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
    private fun roomSummary(id: String): RoomSummary = emptyRoomSummary(id).copy(placeCount = 10)

    /** 볼 수 있는 장소가 애초에 하나도 없는 방(EC-011). 순회 대상에서 빠진다. */
    private fun emptyRoomSummary(id: String): RoomSummary =
        RoomSummary(
            id = id,
            name = "$id 방",
            description = "",
            type = RoomType.GROUP,
            color = RoomColor.GRAY,
            placeCount = 0,
            thumbnailImageUrls = emptyList(),
        )

    private companion object {
        const val ROOM_ID = "room-1"

        const val OTHER_ROOM_ID = "room-2"
    }
}
