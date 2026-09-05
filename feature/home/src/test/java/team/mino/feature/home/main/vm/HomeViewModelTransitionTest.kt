package team.mino.feature.home.main.vm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import team.mino.core.common.kotlin.geo.GeoPoint
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
import team.mino.feature.home.main.model.HomeTooltip

/**
 * 덱이 바뀔 때 **화면이 무엇을 보여주는가**를 판정한다 — 정렬 칩(FR-010, UX-004), 예고 툴팁(FR-015),
 * 그리고 위치 권한 거부가 덱에 닿는 경로(EC-009).
 *
 * 다루는 범위는 TS-020·TS-022·TS-023과 EC-009·EC-012, spec §4 가정(예고 툴팁의 재노출 조건)이다.
 *
 * **3.0.0 개정으로 자동 방 전환 절이 늘었다** — T049가 세운 「방이 바뀌면 정렬을 꾹 Pick으로 초기화」가
 * 폐기되고 자동 전환이 정렬을 유지하게 됐다(spec 4.0.0 FR-012·FR-025). 여기서 보는 것은 **어느 칸으로
 * 갈지가 아니라**(그건 `ResolveNextDeckUseCaseTest`의 몫이다) `NextDeck.NextRoom(roomId, sort)`가 실어 온
 * `sort`를 `HomeViewModel`이 그대로 써서 여는가(TS-015·016), 그리고 그 방 전환에 툴팁이 하나만
 * 뜨는가(TS-018)다. `EC-019`(장소 있는 방이 하나뿐)는 반대로 방이 안 바뀌는 경우를 본다 — 정렬만 넘어갈
 * 때는 방 전환 툴팁이 없어야 한다.
 *
 * **여기서 보지 않는 것**과 그 자리를 메우는 것:
 *
 * | 미검증 | 메우는 것 |
 * |---|---|
 * | 전환 규칙이 다음 덱으로 **어느 칸**을 고르는가(FR-011·013·014, TS-017·019·019a·021·024) | `ResolveNextDeckUseCaseTest` |
 * | 넘김·되돌리기·전환 중 입력(FR-001·002·023, TS-001·002·007) | [HomeViewModelDeckTest] |
 * | 방 전환 툴팁의 노출·소멸 자체와 방 시트(FR-016·017·018, TS-025~028) | `HomeViewModelRoomSheetTest` |
 * | 툴팁의 위치·페이드·조작 비차단(UX-003) | 툴팁을 그리는 Compose 계층 |
 *
 * **예고 툴팁이 가리키는 대상은 「지금 보는 덱」이 아니라 「이 덱을 다 봤을 때 올 덱」이다**(FR-015, SC-004).
 * [ResolveNextDeckUseCase]는 [team.mino.core.domain.model.DeckContext.currentSort]를 보지 않고 소진 집합만
 * 보므로, 예고를 물을 때 **지금 덱을 소진 집합에 넣어** 물어야 한다. 넣지 않으면 판정이 [DeckSort] 선언 순서를
 * 처음부터 훑어 지금 보고 있는 덱을 그대로 되돌려 주고, 툴팁은 방금 넘기던 덱을 가리키게 된다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTransitionTest {
    private val deckRepository = FakeHomeDeckRepository()
    private val preferencesRepository = FakeHomePreferencesRepository()
    private val placeRepository = FakePlaceRepository()

    @Before
    fun setUp() {
        // viewModelScope가 Main에서 돌아 첫 덱 적재가 생성 직후 끝나 있도록 한다.
        // 툴팁 타이머도 이 스케줄러를 쓰므로 advanceTimeBy로 3초를 흘려보낼 수 있다.
        Dispatchers.setMain(UnconfinedTestDispatcher())

        deckRepository.rooms = listOf(roomSummary(ROOM_ID), roomSummary(NEXT_ROOM_ID))

        // 시작 방은 못박아 둔다 — 어느 방에서 시작하는가는 FR-022의 판정 대상이고 이 파일의 것이 아니다.
        preferencesRepository.lastRoomId = ROOM_ID

        // 가이드가 떠 있으면 DismissGuide를 뺀 모든 Intent가 버려진다(FR-019).
        preferencesRepository.guideDismissed = true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── 정렬 칩 (FR-010, UX-004) ──────────────────────────────────────────

    /**
     * 칩을 누르면 그 덱으로 즉시 바뀐다(FR-010, TS-020).
     *
     * 칩 위치만 보면 상태의 [HomeUiState.sort]만 갈아끼운 구현도 통과한다. 그래서 **실제 카드**와 **나간 요청**을
     * 함께 본다 — 셋이 어긋나는 것이 UX-004가 막으려는 상태다.
     */
    @Test
    fun `정렬 칩을 누르면 그 덱으로 즉시 바뀌고 칩 표시가 함께 옮겨진다`() =
        runTest {
            stage(DeckSort.GGUK_PICK, count = 3)
            val latest = stage(DeckSort.LATEST, count = 3)
            val viewModel = createViewModel()

            viewModel.processIntent(HomeIntent.SelectSort(DeckSort.LATEST))

            val state = viewModel.state.value
            assertEquals(DeckSort.LATEST, state.sort)
            assertEquals(latest, state.cards.toList())
            assertEquals(
                FakeHomeDeckRepository.DeckRequest(roomId = ROOM_ID, sort = DeckSort.LATEST, location = null),
                deckRepository.deckRequests.last(),
            )
        }

    /**
     * `가까운순`은 좌표를 받은 뒤에야 덱을 받는다(TS-020, R-009·R-013).
     *
     * 응답을 기다리지 않고 부르면 좌표 없는 요청이 되어 **빈 덱**이 돌아온다(EC-009). 그러면 사용자가 고른 덱이
     * 열리지도 않은 채 소진으로 흡수되므로, 「묻기 전에는 부르지 않는다」가 이 케이스의 절반이다.
     */
    @Test
    fun `가까운순 칩은 위치 권한을 물은 뒤 좌표와 함께 덱을 받는다`() =
        runTest {
            stage(DeckSort.GGUK_PICK, count = 3)
            val nearest = stage(DeckSort.NEAREST, count = 3)
            val viewModel = createViewModel()
            val sideEffects = recordSideEffects(viewModel)

            viewModel.processIntent(HomeIntent.SelectSort(DeckSort.NEAREST))

            assertTrue(
                "가까운순은 좌표를 요구하는 유일한 정렬이다 — 묻지 않으면 받을 방법이 없다",
                sideEffects.contains(HomeSideEffect.RequestLocationPermission),
            )
            assertTrue(
                "응답 전에 부르면 좌표 없는 요청이 되어 빈 덱이 돌아온다",
                deckRepository.deckRequests.none { it.sort == DeckSort.NEAREST },
            )

            viewModel.processIntent(HomeIntent.LocationPermissionResult(HERE))

            val state = viewModel.state.value
            assertEquals(DeckSort.NEAREST, state.sort)
            assertEquals(nearest, state.cards.toList())
            assertEquals(
                FakeHomeDeckRepository.DeckRequest(roomId = ROOM_ID, sort = DeckSort.NEAREST, location = HERE),
                deckRepository.deckRequests.last(),
            )
        }

    /**
     * 권한 거부는 오류가 아니라 **소진**이다(EC-009, R-013).
     *
     * 이 변환은 여기서만 잡힌다. `ResolveNextDeckUseCase`의 입력에서는 「권한 거부」와 「원래 후보가 0장」이
     * 구별되지 않으므로(둘 다 소진 집합의 원소다), 거부를 소진으로 **바꿔 넣는 쪽**이 판정 대상이다.
     *
     * 어느 덱으로 가는지는 UseCase가 정한 결과라 여기서 되풀이하지 않는다. 보는 것은 셋이다 — 화면이 계속
     * 넘길 수 있는 상태로 남고([HomePhase.DECK]), 빈 `가까운순`이 화면에 얹히지 않으며, 칩과 카드가 어긋나지
     * 않는다(UX-004).
     */
    @Test
    fun `위치 권한을 거부하면 가까운순이 소진으로 흡수돼 화면에 남지 않는다`() =
        runTest {
            stage(DeckSort.GGUK_PICK, count = 1)
            stage(DeckSort.LATEST, count = 3)
            // 세워 두어도 좌표가 없으면 빈 덱이다 — 이 카드들이 화면에 나오면 거부가 흡수되지 않은 것이다.
            stage(DeckSort.NEAREST, count = 3)
            val viewModel = createViewModel()

            swipe(viewModel)
            assertEquals("소진되면 같은 방의 다음 덱으로 넘어가 있어야 한다", DeckSort.LATEST, viewModel.state.value.sort)

            viewModel.processIntent(HomeIntent.SelectSort(DeckSort.NEAREST))
            viewModel.processIntent(HomeIntent.LocationPermissionResult(null))

            val state = viewModel.state.value
            assertEquals("거부는 정상 흐름이다 — 계속 넘길 수 있어야 한다", HomePhase.DECK, state.phase)
            assertNotEquals("빈 가까운순을 그대로 띄우면 넘길 카드가 없는 덱이 화면에 남는다", DeckSort.NEAREST, state.sort)
            assertDeckMatchesChip(state)
        }

    /**
     * 자동 전환에서도 칩은 실제 덱을 따라간다(UX-004).
     *
     * 사용자가 칩을 누르지 않은 전환이라 갱신을 빠뜨리기 가장 쉬운 자리다. 칩이 `꾹 Pick`에 남은 채 `최신순`
     * 카드가 뜨면 화면이 거짓말을 한다.
     */
    @Test
    fun `자동 전환으로 정렬이 바뀌면 칩과 실제 덱이 함께 옮겨진다`() =
        runTest {
            stage(DeckSort.GGUK_PICK, count = 1)
            val latest = stage(DeckSort.LATEST, count = 3)
            val viewModel = createViewModel()

            swipe(viewModel)

            val state = viewModel.state.value
            assertEquals(DeckSort.LATEST, state.sort)
            assertEquals(latest, state.cards.toList())
        }

    // ── 자동 방 전환은 정렬을 유지한다 (FR-012·FR-025, 3.0.0) ──────────────────
    //
    // T049가 세운 「방이 바뀌면 정렬을 꾹 Pick으로 초기화」가 폐기됐다(spec 4.0.0). 여기서 보는 것은
    // `ResolveNextDeckUseCase`가 고른 칸이 아니라(그건 `ResolveNextDeckUseCaseTest`가 본다),
    // `HomeViewModel`이 `NextDeck.NextRoom(roomId, sort)`의 `sort`를 **버리지 않고** 그대로 여는가다.

    /**
     * 덱을 소진하면 정렬은 그대로인 채 다음 방으로 넘어간다(TS-015, FR-012).
     *
     * 방이 둘뿐이라 `꾹 Pick`을 모두 훑으면 `최신순` 차례가 되고, 그 첫 후보가 다시 room-1이다 — 방을
     * 오가는 동안에도 정렬이 살아 있는지를 보는 자리다.
     *
     * **가짜 저장소는 [FakeHomeDeckRepository.setDeck]으로 세운 덱을 스와이프로 비워도 지우지 않는다.**
     * 그래서 방 전환이 정렬을 `꾹 Pick`으로 되감는 옛 규칙이 남아 있으면, room-1로 돌아왔을 때 이미
     * 소진한 `꾹 Pick` 카드가 재조회로 다시 나타나 이 판정을 조용히 통과시킨다 — 정렬이 실제로 유지될
     * 때만 `최신순` 덱이 열린다.
     */
    @Test
    fun `자동 방 전환은 지금 정렬을 그대로 들고 다음 방으로 간다`() =
        runTest {
            stage(DeckSort.GGUK_PICK, count = 1)
            val latest = stage(DeckSort.LATEST, count = 1)
            val viewModel = createViewModel()

            swipe(viewModel)

            val state = viewModel.state.value
            assertEquals("방이 바뀌어도 꾹 Pick으로 되감기지 않는다", DeckSort.LATEST, state.sort)
            assertEquals(latest, state.cards.toList())
            assertEquals(
                FakeHomeDeckRepository.DeckRequest(roomId = ROOM_ID, sort = DeckSort.LATEST, location = null),
                deckRepository.deckRequests.last(),
            )
        }

    /**
     * 정렬은 지금 정렬로 **모든 방**을 확인해야 다음 정렬로 넘어가고, 그다음엔 **첫 방부터** 다시
     * 훑는다(TS-016, FR-025).
     *
     * 그 방 전환에는 방 전환 툴팁 하나만 뜬다 — 정렬이 바뀐 것을 알리는 별도 툴팁은 없다(TS-018,
     * FR-016). [HomeTooltip]에는 애초에 그런 갈래가 없으므로, 이 자리가 실제로 가르는 것은 「room-1의
     * 꾹 Pick을 정렬 초기화 없이 다시 열어 버려 방금 뜬 [HomeTooltip.RoomChanged]가 [HomeTooltip.DeckAhead]로
     * 덮이는 것」과 「최신순으로 올바르게 넘어가 방 전환 툴팁이 그대로 남는 것」의 구별이다.
     */
    @Test
    fun `정렬은 방을 모두 확인한 뒤에야 넘어가고 첫 방부터 다시 훑으며 방 전환 툴팁만 뜬다`() =
        runTest {
            deckRepository.rooms = listOf(roomSummary(ROOM_ID), roomSummary(NEXT_ROOM_ID), roomSummary(THIRD_ROOM_ID))
            stage(DeckSort.GGUK_PICK, count = 1)
            deckRepository.setDeck(roomId = NEXT_ROOM_ID, sort = DeckSort.GGUK_PICK, cards = listOf(card("gguk-b")))
            deckRepository.setDeck(roomId = THIRD_ROOM_ID, sort = DeckSort.GGUK_PICK, cards = listOf(card("gguk-c")))
            val latest = stage(DeckSort.LATEST, count = 3)
            val viewModel = createViewModel()

            repeat(3) { swipe(viewModel) } // room-1 → room-2 → room-3 순으로 꾹 Pick을 모두 소진한다.

            val state = viewModel.state.value
            assertEquals("세 방의 꾹 Pick을 다 봐야 최신순으로 넘어간다", DeckSort.LATEST, state.sort)
            assertEquals("최신순은 첫 방부터 다시 훑는다", ROOM_ID, state.room?.id)
            assertEquals(latest, state.cards.toList())
            assertEquals(
                "방이 바뀐 것 하나만 알리고, 정렬 전환을 따로 알리는 툴팁은 없다",
                HomeTooltip.RoomChanged(roomSummary(ROOM_ID).name),
                state.tooltip,
            )
        }

    /**
     * 위치 권한 거부는 `가까운순`을 **모든 방에 대해** 소진 처리한다(EC-009, R-013). 권한은 방별 값이
     * 아니므로 다른 방의 `가까운순`을 다시 묻지 않는다.
     *
     * `꾹 Pick`·`최신순`을 두 방 모두 세우지 않아 시작하자마자 빈 덱으로 훑여 내려가 가까운순 권한
     * 요청까지 곧장 닿는다. 권한을 한 번 거부한 뒤 **두 번째 방**의 가까운순을 또 묻는지가 판정
     * 대상이다 — 방마다 다시 물으면 화면이 권한 다이얼로그를 기다리는 [HomePhase.LOADING]에 갇힌다.
     */
    @Test
    fun `위치 권한 거부는 가까운순을 모든 방에 대해 소진 처리해 다시 묻지 않는다`() =
        runTest {
            val viewModel = createViewModel()
            val sideEffects = recordSideEffects(viewModel)
            assertEquals(
                "초기 로드가 두 방의 꾹 Pick·최신순을 다 걷어내고 가까운순 권한을 문다",
                1,
                sideEffects.count { it == HomeSideEffect.RequestLocationPermission },
            )

            viewModel.processIntent(HomeIntent.LocationPermissionResult(null))

            assertEquals(
                "권한은 방별 값이 아니다 — 다른 방의 가까운순을 다시 물으면 안 된다",
                1,
                sideEffects.count { it == HomeSideEffect.RequestLocationPermission },
            )
            assertNotEquals(
                "다시 묻는 채로 멈추면 화면이 로딩에서 걷히지 않는다",
                HomePhase.LOADING,
                viewModel.state.value.phase,
            )
        }

    /**
     * 장소가 있는 방이 하나뿐이면 「지금 정렬로 모든 방을 확인」이 곧 그 방 하나를 보는 것이므로, 덱을
     * 소진할 때마다 정렬이 곧장 넘어간다(EC-019). 방이 바뀌지 않으므로 방 전환 툴팁은 뜨지 않는다.
     */
    @Test
    fun `장소 있는 방이 하나뿐이면 방 전환 툴팁 없이 정렬만 곧장 넘어간다`() =
        runTest {
            deckRepository.rooms = listOf(roomSummary(ROOM_ID))
            stage(DeckSort.GGUK_PICK, count = 3)
            val latest = stage(DeckSort.LATEST, count = 3)
            val viewModel = createViewModel()

            repeat(2) { swipe(viewModel) } // 잔여 1장 — 같은 덱의 예고 툴팁이 뜬다(FR-015, 이 테스트의 관심사가 아니다).
            advanceTimeBy(TOOLTIP_MILLIS + 1) // 그 예고를 걷어내야 다음 판정이 가려지지 않는다.
            assertNull(viewModel.state.value.tooltip)

            swipe(viewModel) // 꾹 Pick의 마지막 카드 — 방이 하나뿐이라 정렬만 최신순으로 넘어간다.

            val state = viewModel.state.value
            assertEquals("방이 하나뿐이면 정렬 소진마다 곧장 다음 정렬로 넘어간다", DeckSort.LATEST, state.sort)
            assertEquals(ROOM_ID, state.room?.id)
            assertEquals(latest, state.cards.toList())
            assertNull("방이 바뀌지 않았으므로 방 전환 툴팁은 뜨지 않는다", state.tooltip)
        }

    // ── 예고 툴팁 (FR-015) ────────────────────────────────────────────────

    /**
     * 잔여 2장이 되는 순간 예고가 뜨고 3초 뒤 사라진다(FR-015, TS-022).
     *
     * 임계값 **직전**도 함께 본다 — 덱을 싣자마자 띄우는 구현은 잔여 3장에서 걸린다.
     *
     * 가리키는 대상이 `최신순`인 것이 이 케이스의 핵심이다. 지금 보는 덱은 `꾹 Pick`이고, 소진 집합에 그것을
     * 넣지 않고 판정을 물으면 선언 순서상 첫 덱인 `꾹 Pick`이 그대로 돌아와 **방금 넘기던 덱을 예고하게 된다**.
     */
    @Test
    fun `잔여 두 장이 되면 다음 덱을 가리키는 예고 툴팁이 뜨고 삼 초 뒤 사라진다`() =
        runTest {
            deckRepository.rooms = listOf(roomSummary(ROOM_ID))
            stage(DeckSort.GGUK_PICK, count = 10)
            stage(DeckSort.LATEST, count = 3)
            val viewModel = createViewModel()

            repeat(7) { swipe(viewModel) }
            assertNull("잔여 3장은 아직 임계값이 아니다", viewModel.state.value.tooltip)

            swipe(viewModel)

            val expected = HomeTooltip.DeckAhead.NextSort(DeckSort.LATEST)
            assertEquals(expected, viewModel.state.value.tooltip)

            advanceTimeBy(TOOLTIP_MILLIS - 1)
            assertEquals("3초를 채우기 전에 사라지면 안 된다", expected, viewModel.state.value.tooltip)

            advanceTimeBy(2)
            assertNull("3초가 지나면 스스로 사라진다", viewModel.state.value.tooltip)
        }

    /**
     * 되돌려서 잔여가 임계값 위로 올라갔다가 다시 내려오면 예고가 **다시 뜬다**(FR-015, spec §4 가정).
     *
     * 한 덱당 1회로 굳어 있으면 되돌리기 뒤에는 잔여 2장에 아무 안내도 없어, 잔여 2장과 예고를 잇는 규칙이
     * 사용자에게만 조용히 깨진다. 사라진 것을 확인한 뒤 임계값을 다시 넘기므로 "아직 떠 있는 것"과
     * "다시 뜬 것"이 섞이지 않는다.
     */
    @Test
    fun `되돌렸다 다시 넘기면 같은 덱의 예고 툴팁이 다시 뜬다`() =
        runTest {
            stage(DeckSort.GGUK_PICK, count = 5)
            stage(DeckSort.LATEST, count = 3)
            val viewModel = createViewModel()

            repeat(3) { swipe(viewModel) }
            val expected = viewModel.state.value.tooltip
            assertNotNull("예고가 한 번은 떠야 재노출을 판정할 수 있다", expected)

            advanceTimeBy(TOOLTIP_MILLIS + 1)
            assertNull(viewModel.state.value.tooltip)

            viewModel.processIntent(HomeIntent.SwipeBackward)
            viewModel.processIntent(HomeIntent.TransitionSettled)
            swipe(viewModel)

            assertEquals("임계값을 다시 넘겨야 재노출을 볼 수 있다", 2, viewModel.state.value.cards.size)
            assertEquals("임계값을 다시 지났으므로 같은 예고가 다시 뜬다", expected, viewModel.state.value.tooltip)
        }

    /**
     * 되돌리지 않고 계속 넘기는 동안에는 예고가 **한 번뿐**이다(spec §4 가정).
     *
     * 재노출의 조건은 「덱이 임계값 위로 다시 길어졌다」이지 「임계값 아래다」가 아니다. 이 구분이 없으면
     * 잔여가 줄어드는 매 장마다 예고가 뜬다.
     */
    @Test
    fun `되돌리지 않고 더 넘기면 예고 툴팁이 다시 뜨지 않는다`() =
        runTest {
            stage(DeckSort.GGUK_PICK, count = 4)
            stage(DeckSort.LATEST, count = 3)
            val viewModel = createViewModel()

            // 4장에서 두 번 넘겨 잔여 2장 — 예고가 한 번 뜬다.
            repeat(2) { swipe(viewModel) }
            assertNotNull(viewModel.state.value.tooltip)
            advanceTimeBy(TOOLTIP_MILLIS + 1)
            assertNull(viewModel.state.value.tooltip)

            swipe(viewModel)

            assertEquals(1, viewModel.state.value.cards.size)
            assertNull("되돌리지 않았으므로 예고는 다시 뜨지 않는다", viewModel.state.value.tooltip)
        }

    /**
     * 전환 직후 잔여가 이미 2장 이하면 예고가 곧바로 뜬다(EC-012).
     *
     * 앞선 덱에서 뜬 예고(`최신순`)를 새 예고(`가까운순`)가 덮는지도 같이 드러난다 — 툴팁은 하나뿐이고
     * 마지막 것이 이긴다(R-008).
     */
    @Test
    fun `잔여가 이미 두 장인 덱으로 전환되면 예고 툴팁이 곧바로 뜬다`() =
        runTest {
            deckRepository.rooms = listOf(roomSummary(ROOM_ID))
            stage(DeckSort.GGUK_PICK, count = 1)
            stage(DeckSort.LATEST, count = 2)
            val viewModel = createViewModel()

            swipe(viewModel)

            val state = viewModel.state.value
            assertEquals(DeckSort.LATEST, state.sort)
            assertEquals(HomeTooltip.DeckAhead.NextSort(DeckSort.NEAREST), state.tooltip)
        }

    /** 방의 마지막 덱에서는 예고가 **다음 방**을 가리킨다(FR-015, TS-022). 문구가 아니라 대상만 담는다. */
    @Test
    fun `현재 방의 마지막 덱에서는 다음 방을 가리키는 예고 툴팁이 뜬다`() =
        runTest {
            val viewModel = viewModelOnLastDeck()

            swipe(viewModel)

            assertEquals(
                HomeTooltip.DeckAhead.NextRoom(roomSummary(NEXT_ROOM_ID).name),
                viewModel.state.value.tooltip,
            )
        }

    /**
     * 가리킬 대상이 없으면 뜨지 않는다(FR-015, TS-023).
     *
     * 마지막 방의 마지막 덱이라 다음이 없다. 임계값을 넘긴 것 자체는 먼저 단언한다 — 그러지 않으면 카드를
     * 아예 넘기지 못한 구현도 "툴팁 없음"으로 통과한다.
     */
    @Test
    fun `순회할 방이 더 없으면 예고 툴팁이 뜨지 않는다`() =
        runTest {
            deckRepository.rooms = listOf(roomSummary(ROOM_ID))
            val viewModel = viewModelOnLastDeck()

            swipe(viewModel)

            assertEquals("임계값을 넘겨야 예고 여부를 판정할 수 있다", 2, viewModel.state.value.cards.size)
            assertNull("가리킬 다음 덱도 다음 방도 없다", viewModel.state.value.tooltip)
        }

    // ── 도구 ──────────────────────────────────────────────────────────────

    private fun createViewModel(): HomeViewModel =
        HomeViewModel(
            homeDeckRepository = deckRepository,
            homePreferencesRepository = preferencesRepository,
            placeRepository = placeRepository,
            resolveNextDeck = ResolveNextDeckUseCase(),
            resolveRoomEntryDeck = ResolveRoomEntryDeckUseCase(),
        )

    /**
     * 현재 방의 `꾹 Pick`·`최신순`을 소진하고 3장짜리 `가까운순`(마지막 덱)에 올라선 ViewModel.
     *
     * 앞선 툴팁을 시간으로 걷어내고 돌려준다 — 남아 있으면 다음 판정에서 "아직 떠 있는 것"과 "새로 뜬 것"이
     * 구별되지 않는다.
     */
    private fun TestScope.viewModelOnLastDeck(): HomeViewModel {
        stage(DeckSort.GGUK_PICK, count = 1)
        stage(DeckSort.LATEST, count = 1)
        stage(DeckSort.NEAREST, count = 3)
        val viewModel = createViewModel()

        swipe(viewModel)
        swipe(viewModel)
        // 마지막 덱이 가까운순이라 좌표를 물어 온다(R-009).
        viewModel.processIntent(HomeIntent.LocationPermissionResult(HERE))
        advanceTimeBy(TOOLTIP_MILLIS + 1)

        val state = viewModel.state.value
        assertEquals("마지막 덱에 올라서지 못하면 이 시나리오가 성립하지 않는다", DeckSort.NEAREST, state.sort)
        assertEquals(3, state.cards.size)
        assertNull("앞선 툴팁이 남아 있으면 다음 판정을 가린다", state.tooltip)
        return viewModel
    }

    /** 넘김 하나와 그 전환의 끝. 전환 중 도착한 입력이 버려지는 것은 [HomeViewModelDeckTest]가 본다. */
    private fun swipe(viewModel: HomeViewModel) {
        viewModel.processIntent(HomeIntent.SwipeForward)
        viewModel.processIntent(HomeIntent.TransitionSettled)
    }

    /** [HomeSideEffect]는 Channel이라 흘려보낸 뒤에는 남지 않는다. 나온 순서대로 담아 두고 뒤에서 본다. */
    private fun TestScope.recordSideEffects(viewModel: HomeViewModel): List<HomeSideEffect> {
        val effects = mutableListOf<HomeSideEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.sideEffect.toList(effects) }
        return effects
    }

    /** 칩이 가리키는 정렬과 화면의 카드가 같은 덱에서 왔는가(UX-004). 카드가 없으면 대조 자체가 성립하지 않는다. */
    private fun assertDeckMatchesChip(state: HomeUiState) {
        assertTrue("대조할 카드가 없다 — 빈 덱이 화면에 남아 있다", state.cards.isNotEmpty())
        assertTrue(
            "칩은 ${state.sort}인데 카드는 ${state.cards.map { it.pinId }}",
            state.cards.all { it.pinId.startsWith(prefixOf(state.sort)) },
        )
    }

    /** [sort] 덱을 [count]장으로 세우고 그 목록을 돌려준다. pinId 접두사가 곧 출신 덱이라 칩과의 대조에 쓴다. */
    private fun stage(
        sort: DeckSort,
        count: Int,
    ): List<PlaceCard> =
        List(count) { index -> card(pinId = "${prefixOf(sort)}-$index") }
            .also { deckRepository.setDeck(roomId = ROOM_ID, sort = sort, cards = it) }

    private fun prefixOf(sort: DeckSort): String = sort.name.lowercase()

    private fun card(pinId: String): PlaceCard =
        PlaceCard(
            pinId = pinId,
            placeName = "장소 $pinId",
            address = "서울시 어딘가",
            imageUrls = emptyList(),
            label = PlaceLabel.WORTH_VISITING,
            registrant = Registrant(userId = "user-1", nickname = "민호", avatar = null),
        )

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

        const val NEXT_ROOM_ID = "room-2"

        const val THIRD_ROOM_ID = "room-3"

        /** 툴팁 노출 시간(spec FR-015·016). */
        const val TOOLTIP_MILLIS = 3_000L

        val HERE = GeoPoint(latitude = 37.5665, longitude = 126.9780)
    }
}
