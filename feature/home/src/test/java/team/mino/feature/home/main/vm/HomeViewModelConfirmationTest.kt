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
import org.junit.Assert.assertNull
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
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.home.fake.FakeHomeDeckRepository
import team.mino.feature.home.fake.FakeHomePreferencesRepository
import team.mino.feature.home.fake.FakePlaceRepository
import java.io.IOException

/**
 * 두 「확인 이벤트」가 서로를 건드리지 않는다는 것만 판정한다(FR-023, `data-model.md` §2.3).
 *
 * | | 무엇이 일으키나 | 무엇을 바꾸나 | 홈이 서버를 부르나 |
 * |---|---|---|---|
 * | ① 경과일 초기화 확인 | 카드 본문 탭([HomeIntent.OpenPlaceDetail]) | 이동해 간 [SCR-006]이 기록한다 | **아니다**(spec 4.0.0) |
 * | ② 카드 열람 확인 | 좌→우 스와이프([HomeIntent.SwipeForward]) | 화면 상태뿐 | 아니다 |
 *
 * 다루는 범위는 TS-012(탭이 상세로 보낸다)·TS-034(홈이 ①을 중복 기록하지 않는다)·TS-013(탭이 덱을 안
 * 건드린다)·TS-035(넘김이 서버를 안 부른다)·EC-017(되돌려도 ①은 취소되지 않는다)이다. **FR-023을 지키는
 * 그물은 이 파일 하나뿐이다** — 둘이 뒤섞이면 사용자가 그냥 넘긴 장소의 `꾹 Pick` 순위가 조용히 바뀌거나,
 * 눌러 본 장소가 덱에서 사라진다.
 *
 * **spec 4.0.0에서 ①의 기록이 [SCR-006]으로 넘어가 홈은 그것을 부르지 않는다.** 그래서 판정은 「기록이
 * 나갔는가」가 아니라 **「나가지 않았는가」**다 — [FakePlaceRepository.recordedAccessPinIds]가 내내 비어 있는
 * 것을 본다. `다른 방 저장` 때문에 [PlaceRepository][team.mino.core.domain.repository.PlaceRepository]가
 * 주입돼 있어(R-019) 홈이 마음만 먹으면 부를 수 있으므로, 부르지 않는다는 것을 이 더블로 직접 확인해야 한다.
 * 상세 이동이 실제로 일어난 것은 SideEffect 목록으로 함께 본다.
 *
 * **`다른 방 저장`(FR-005)도 여기서 본다.** 그것 역시 「덱을 건드리지 않고 서버에만 나가는 일」이라 위 표의
 * ①과 같은 성질이고, 판정 근거도 같은 종류의 더블 — [FakePlaceRepository.duplicatePinCalls]다. 대상은
 * TS-011a(복수 선택이 그대로 나감)·TS-011b(이미 저장된 방도 막히지 않음)·EC-018(선택 없으면 확정이 없다)이다.
 * 시트를 여는 것 자체(TS-011)는 이 파일이 새로 세우지 않는다 — 수동 검증으로 넘어갔다(`tasks.md` TS 표).
 *
 * **여기서 보지 않는 것**: 넘김·되돌리기 자체의 덱 조작 규칙(TS-001·002, EC-001·003)은 `HomeViewModelDeckTest`가
 * 소유한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelConfirmationTest {
    private val deckRepository = FakeHomeDeckRepository()
    private val preferencesRepository = FakeHomePreferencesRepository()
    private val placeRepository = FakePlaceRepository()

    @Before
    fun setUp() {
        // viewModelScope가 Main에서 돌아 첫 덱 적재가 생성 직후 끝나 있도록 한다.
        Dispatchers.setMain(UnconfinedTestDispatcher())

        deckRepository.rooms = listOf(roomSummary(ROOM_ID))

        // 시작 방과 가이드는 이 파일의 판정 대상이 아니라 전제다. 가이드가 떠 있으면 탭도 넘김도 버려진다(FR-019).
        preferencesRepository.lastRoomId = ROOM_ID
        preferencesRepository.guideDismissed = true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * 카드 본문 탭이 하는 일은 **그 장소의 상세로 보내는 것 하나뿐**이다(FR-007, TS-012·034).
     *
     * 인자까지 본다 — 탭한 카드가 아닌 다른 pinId로 보내면 엉뚱한 장소가 열리고, [SCR-006]이 그 장소의
     * 경과일을 초기화한다.
     *
     * **SideEffect 목록 전체를 비교하는 것이 TS-034의 판정이다.** 홈이 「경과일 초기화 확인」을 따로 보내려면
     * 서버로 나가는 무엇이 하나 더 있어야 하는데, 나간 것이 상세 이동 하나뿐임을 여기서 본다
     * (덱 요청은 아래 [deckRequests] 단언이 함께 막는다).
     */
    @Test
    fun `카드 본문을 탭하면 그 장소의 상세로 보내는 것이 전부다`() =
        runTest {
            val cards = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            val viewModel = createViewModel()
            val effects = recordSideEffects(viewModel)
            val deckRequestsBefore = deckRepository.deckRequests.size

            viewModel.processIntent(HomeIntent.OpenPlaceDetail(cards.first().pinId))

            assertEquals(listOf(HomeSideEffect.NavigateToPlaceDetail(cards.first().pinId)), effects)
            assertEquals(
                "「경과일 초기화 확인」은 이동해 간 [SCR-006]이 기록한다 — 홈이 부르면 두 건이 된다",
                emptyList<String>(),
                placeRepository.recordedAccessPinIds,
            )
            assertEquals(
                "탭이 덱을 다시 받아 오면 요청이 늘어난다",
                deckRequestsBefore,
                deckRepository.deckRequests.size,
            )
        }

    /**
     * 탭은 덱의 진행 상태를 **어느 것도** 바꾸지 않는다(FR-023, TS-013).
     *
     * 잔여 카드뿐 아니라 되돌리기 이력과 덱 요청 횟수까지 함께 본다. 카드 목록만 보면 "덱을 다시 받아 같은
     * 목록이 된" 구현이 통과하고, 그 구현은 상세를 열 때마다 서버 요청을 한 번씩 더 흘린다.
     *
     * **전제부터 단언한다** — 카드가 실려 있지 않으면 "그대로다"는 아무것도 하지 않는 구현에서도 성립한다.
     */
    @Test
    fun `카드 본문을 탭해도 덱은 그대로다`() =
        runTest {
            val cards = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            val viewModel = createViewModel()
            val deckRequestsBefore = deckRepository.deckRequests.size

            assertEquals("탭을 넣기 전에 덱이 실려 있어야 판정할 것이 생긴다", cards, viewModel.state.value.cards)

            viewModel.processIntent(HomeIntent.OpenPlaceDetail(cards.first().pinId))

            val state = viewModel.state.value
            assertEquals("탭한 카드가 최상단에 그대로 남아야 한다", cards, state.cards.toList())
            assertTrue("탭은 넘김이 아니므로 되돌릴 카드가 생기지 않는다", state.undoStack.isEmpty())
            assertEquals("탭이 덱을 다시 받아 오면 요청이 늘어난다", deckRequestsBefore, deckRepository.deckRequests.size)
        }

    /**
     * 넘김은 서버를 부르지 않는다(FR-001·023, TS-035). **이 파일에서 가장 중요한 케이스다.**
     *
     * 여기가 깨지면 사용자가 보지도 않고 넘긴 장소의 경과일이 초기화되어 `꾹 Pick` 순위가 조용히 바뀐다.
     *
     * 호출이 없다는 것만 보면 넘김을 아예 처리하지 않는 구현도 통과하므로, **카드가 실제로 빠졌는지를 함께
     * 단언한다.** 둘을 같이 봐야 "넘어갔지만 서버는 조용했다"가 판정된다.
     */
    @Test
    fun `좌에서 우로 넘겨도 서버로 나가는 것이 없다`() =
        runTest {
            val cards = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            val viewModel = createViewModel()
            val deckRequestsBefore = deckRepository.deckRequests.size

            viewModel.processIntent(HomeIntent.SwipeForward)

            assertEquals("넘김이 덱을 건드리지 않으면 판정할 넘김 자체가 없다", cards.drop(1), viewModel.state.value.cards)
            assertEquals(
                "넘김은 「카드 열람 확인」이고, 그것은 화면 안에서 끝난다",
                emptyList<String>(),
                placeRepository.recordedAccessPinIds,
            )
        }

    /**
     * 상세를 열어 본 카드를 넘겼다가 되돌려도 **[SCR-006]이 이미 기록한 초기화는 취소되지 않는다**
     * (EC-017, `data-model.md` §2.2).
     *
     * 되돌리기가 취소하는 것은 「카드 열람 확인」뿐이다. 카드는 최상단으로 돌아오고, 홈은 그동안 서버로
     * 아무것도 보내지 않는다 — 되돌릴 때 보상 요청을 흘리는 구현은 여기서 걸린다. 상세 이동 SideEffect
     * 하나만 나간 것까지 함께 본다.
     */
    @Test
    fun `상세를 본 카드를 넘겼다가 되돌려도 홈은 보상 요청을 흘리지 않는다`() =
        runTest {
            val cards = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            val viewModel = createViewModel()
            val topPinId = cards.first().pinId
            val effects = recordSideEffects(viewModel)
            val deckRequestsBefore = deckRepository.deckRequests.size

            viewModel.processIntent(HomeIntent.OpenPlaceDetail(topPinId))
            viewModel.processIntent(HomeIntent.SwipeForward)
            viewModel.processIntent(HomeIntent.TransitionSettled)
            viewModel.processIntent(HomeIntent.SwipeBackward)

            assertEquals("되돌리기는 카드만 되돌린다", cards, viewModel.state.value.cards)
            assertEquals(listOf(HomeSideEffect.NavigateToPlaceDetail(topPinId)), effects)
            assertEquals(
                "되돌리기가 보상으로 기록을 흘리면 안 된다",
                emptyList<String>(),
                placeRepository.recordedAccessPinIds,
            )
            assertEquals("되돌리기가 덱을 다시 받아 오면 안 된다", deckRequestsBefore, deckRepository.deckRequests.size)
        }

    /**
     * `다른 방 저장`으로 방을 여러 개 고르면 **고른 방 전부**가 그대로 `duplicatePin`에 실려 나간다
     * (FR-005, TS-011a).
     *
     * 목록 순서와 개수를 함께 본다 — 하나만 넘기거나 순서를 다시 섞는 구현은 사용자가 고른 방과 다른 조합으로
     * 담기거나 방을 하나 빠뜨린다. 메뉴가 닫히고 시트가 열리는 것(TS-011)은 이 케이스가 성립하기 위한 전제일
     * 뿐이라 별도 단언은 최소로 둔다 — 시트를 거치지 않고는 [HomeIntent.ToggleSaveTargetRoom]에 닿을 수 없다.
     *
     * 확정이 **저장 대상 지정일 뿐**임을 덱·현재 방·되돌리기 이력이 그대로임으로 함께 확인한다(spec §3.2).
     */
    @Test
    fun `방을 여러 개 고르고 저장하기를 누르면 고른 방 전부가 그대로 duplicatePin에 나간다`() =
        runTest {
            deckRepository.rooms =
                listOf(roomSummary(ROOM_ID), roomSummary(OTHER_ROOM_ID), roomSummary(THIRD_ROOM_ID))
            val cards = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            val viewModel = createViewModel()
            val effects = recordSideEffects(viewModel)
            val pinId = cards.first().pinId

            viewModel.processIntent(HomeIntent.OpenActionMenu(pinId))
            viewModel.processIntent(HomeIntent.SaveToAnotherRoom(pinId))
            assertNull("메뉴가 남아 있으면 시트 뒤에 겹쳐 뜬다", viewModel.state.value.actionMenuTarget)

            viewModel.processIntent(HomeIntent.ToggleSaveTargetRoom(OTHER_ROOM_ID))
            viewModel.processIntent(HomeIntent.ToggleSaveTargetRoom(THIRD_ROOM_ID))
            viewModel.processIntent(HomeIntent.ConfirmSaveTargets)

            assertEquals(
                listOf(
                    FakePlaceRepository.DuplicatePinRequest(
                        pinId = pinId,
                        roomIds = listOf(OTHER_ROOM_ID, THIRD_ROOM_ID),
                    ),
                ),
                placeRepository.duplicatePinCalls,
            )
            assertEquals(listOf(HomeSideEffect.ShowSaveResult), effects)
            assertNull("확정되면 시트는 닫힌다", viewModel.state.value.savePicker)
            assertEquals(
                "저장은 방 전환이 아니다",
                ROOM_ID,
                viewModel.state.value.room
                    ?.id,
            )
            assertEquals(
                "카드가 그대로 남아야 한다",
                cards,
                viewModel.state.value.cards
                    .toList(),
            )
            assertTrue(
                "저장은 넘김이 아니므로 되돌릴 카드가 생기지 않는다",
                viewModel.state.value.undoStack
                    .isEmpty(),
            )
        }

    /**
     * 이미 저장된 방도 체크·비활성으로 막히지 않고 그대로 고를 수 있다(FR-005, TS-011b).
     *
     * ViewModel은 애초에 "이 방에 이미 저장돼 있는가"를 알지 못한다 — `RoomPickerItem`이 그 정보를 담지 않는
     * 것과 같은 이유다(`core/common/ui`). 그래서 판정은 **막는 코드가 없다**는 것이고, 가장 직접적인 증거는
     * 카드가 나온 방 자체([ROOM_ID], 이미 그 장소를 담고 있는 방)를 저장 대상으로 다시 골라도 걸러지지 않고
     * `duplicatePin`까지 그대로 실려 나가는 것이다.
     */
    @Test
    fun `이미 저장된 방을 다시 골라도 막히지 않고 duplicatePin에 실려 나간다`() =
        runTest {
            deckRepository.rooms = listOf(roomSummary(ROOM_ID), roomSummary(OTHER_ROOM_ID))
            val cards = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            val viewModel = createViewModel()
            val pinId = cards.first().pinId

            viewModel.processIntent(HomeIntent.SaveToAnotherRoom(pinId))
            viewModel.processIntent(HomeIntent.ToggleSaveTargetRoom(ROOM_ID))
            viewModel.processIntent(HomeIntent.ConfirmSaveTargets)

            assertEquals(
                listOf(FakePlaceRepository.DuplicatePinRequest(pinId = pinId, roomIds = listOf(ROOM_ID))),
                placeRepository.duplicatePinCalls,
            )
        }

    /**
     * 선택된 방이 없으면 `ConfirmSaveTargets`가 확정으로 이어지지 않는다(FR-005, EC-018).
     *
     * 화면은 `저장하기`를 비활성으로 그려 애초에 이 Intent가 나가지 않게 막지만, 그 비활성은 그리기의 몫일
     * 뿐이다. ViewModel 쪽에서도 막아야 그리기 실수 하나로 "선택 없이 확정"이 뚫리지 않는다 — 그래서 비활성을
     * 뚫고 Intent가 들어온 것처럼 직접 보낸다.
     */
    @Test
    fun `방을 하나도 고르지 않고 확정해도 저장은 나가지 않는다`() =
        runTest {
            deckRepository.rooms = listOf(roomSummary(ROOM_ID), roomSummary(OTHER_ROOM_ID))
            val cards = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            val viewModel = createViewModel()
            val effects = recordSideEffects(viewModel)

            viewModel.processIntent(HomeIntent.SaveToAnotherRoom(cards.first().pinId))
            assertTrue(
                "선택 없음을 판정하려면 시트가 선택 없이 열려 있어야 한다",
                viewModel.state.value.savePicker
                    ?.selectedRoomIds
                    .orEmpty()
                    .isEmpty(),
            )

            viewModel.processIntent(HomeIntent.ConfirmSaveTargets)

            assertEquals(
                "선택이 비었으면 duplicatePin은 애초에 불리지 않는다",
                emptyList<FakePlaceRepository.DuplicatePinRequest>(),
                placeRepository.duplicatePinCalls,
            )
            assertEquals("확정되지 않았으니 성공 알림도 없다", emptyList<HomeSideEffect>(), effects)
        }

    /**
     * 저장이 실패하면 **실패로** 알린다(FR-005). 통로는 SideEffect가 아니라 `DomainErrorEmitter`다
     * (`docs/conventions/error_handling.md` §5 2행 — 사용자 액션의 일회성 실패).
     *
     * 성패를 가리지 않고 늘 성공을 흘리는 구현은 사용자가 담기지 않은 장소를 담긴 것으로 알고 떠난다.
     * 성공 케이스와 짝이라 둘이 함께 있어야 「성패가 실제로 갈린다」가 판정된다.
     *
     * **둘을 함께 본다** — 에러가 나갔는지만 보면 성공 알림까지 겹쳐 보내 스낵바를 두 번 띄우는 구현이 통과한다.
     * 리프를 그대로 확인하는 것은 ViewModel이 문구로 바꾸지 않는다는 계약이기도 하다(§5, 문구 매핑은 Route).
     */
    @Test
    fun `저장이 실패하면 실패가 알려진다`() =
        runTest {
            deckRepository.rooms = listOf(roomSummary(ROOM_ID), roomSummary(OTHER_ROOM_ID))
            val failure = MinoDomainException.Network(IOException("offline"))
            placeRepository.duplicatePinFailure = failure
            val cards = cards(count = 3)
            deckRepository.setDeck(ROOM_ID, DeckSort.GGUK_PICK, cards)
            val viewModel = createViewModel()
            val effects = recordSideEffects(viewModel)
            val domainErrors = recordDomainErrors(viewModel)
            val pinId = cards.first().pinId

            viewModel.processIntent(HomeIntent.SaveToAnotherRoom(pinId))
            viewModel.processIntent(HomeIntent.ToggleSaveTargetRoom(OTHER_ROOM_ID))
            viewModel.processIntent(HomeIntent.ConfirmSaveTargets)

            assertEquals("실패는 도메인 에러 채널로 나간다", listOf(failure), domainErrors)
            assertEquals("실패에 성공 알림이 겹치면 스낵바가 두 번 뜬다", emptyList<HomeSideEffect>(), effects)
            assertEquals(
                "실패한 저장은 나가지 않은 것이다",
                emptyList<FakePlaceRepository.DuplicatePinRequest>(),
                placeRepository.duplicatePinCalls,
            )
            assertNull("실패해도 시트는 닫힌다", viewModel.state.value.savePicker)
        }

    private fun createViewModel(): HomeViewModel =
        HomeViewModel(
            homeDeckRepository = deckRepository,
            homePreferencesRepository = preferencesRepository,
            placeRepository = placeRepository,
            resolveNextDeck = ResolveNextDeckUseCase(),
            resolveRoomEntryDeck = ResolveRoomEntryDeckUseCase(),
        )

    /** pinId로 카드를 구별할 수 있게 번호를 매긴다. 나머지 필드는 판정에 쓰이지 않는다. */
    private fun cards(count: Int): List<PlaceCard> =
        List(count) { index ->
            PlaceCard(
                pinId = "pin-$index",
                placeName = "장소 $index",
                address = "서울시 어딘가 $index",
                imageUrls = emptyList(),
                label = PlaceLabel.WORTH_VISITING,
                registrant = Registrant(userId = "user-1", nickname = "민호", avatar = null),
            )
        }

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

    /** [HomeSideEffect]는 Channel이라 흘려보낸 뒤에는 남지 않는다. 나온 순서대로 담아 두고 뒤에서 본다. */
    private fun TestScope.recordSideEffects(viewModel: HomeViewModel): List<HomeSideEffect> {
        val effects = mutableListOf<HomeSideEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.sideEffect.toList(effects) }
        return effects
    }

    /** 도메인 에러도 Channel이라 같은 방식으로 받아 둔다 — 화면 없이 볼 수 있는 자리는 여기뿐이다. */
    private fun TestScope.recordDomainErrors(viewModel: HomeViewModel): List<MinoDomainException> {
        val errors = mutableListOf<MinoDomainException>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.domainErrors.toList(errors) }
        return errors
    }

    private companion object {
        const val ROOM_ID = "room-1"

        /** 저장 대상이 되는 다른 방. 시작 방이 되지 않도록 [FakeHomePreferencesRepository.lastRoomId]는 [ROOM_ID]다. */
        const val OTHER_ROOM_ID = "room-2"

        /** TS-011a에서 [OTHER_ROOM_ID]와 함께 복수 선택되는 세 번째 방. */
        const val THIRD_ROOM_ID = "room-3"
    }
}
