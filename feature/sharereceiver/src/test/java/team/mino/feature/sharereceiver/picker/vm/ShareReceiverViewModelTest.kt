package team.mino.feature.sharereceiver.picker.vm

import androidx.lifecycle.SavedStateHandle
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
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.model.RoomType
import team.mino.core.domain.usecase.GetRoomPickerRoomsUseCase
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.sharereceiver.fake.FakeAnonymousAuthRepository
import team.mino.feature.sharereceiver.fake.FakeResources
import team.mino.feature.sharereceiver.fake.FakeRoomRepository
import team.mino.feature.sharereceiver.fake.FakeSharedPlaceRepository
import java.io.IOException

/**
 * 방 선택 시트의 선택 처리와 저장 예약을 판정한다.
 *
 * 상태의 필드·파생 값은 `data-model.md` §5.1이, 세션 확인 → 목록 조회 → 방목록/빈목록의 갈림은 §6이 소유한다.
 *
 * **로딩 상태는 판정 대상이 아니다.** 시트 표출과 `[저장하기]` 사이에 대기 표현을 두지 않기로 했으므로(UX-009)
 * [ShareReceiverUiState]에 로딩 슬롯 자체가 없다. 세션 확인과 목록 조회는 상태를 거치지 않고 결과만 남긴다.
 *
 * **선택 0개로 `Save`가 들어오는 경우도 판정하지 않는다.** 그 상태에서는 버튼이 비활성이라 인텐트가 발생하지
 * 않으며(FR-009), 여기서 억지로 흘려 보내면 spec에 없는 방어 동작을 ViewModel의 계약으로 굳히게 된다.
 * 이 테스트가 FR-009에 대해 보는 것은 버튼의 활성 조건인 [ShareReceiverUiState.isSaveEnabled] 하나다.
 *
 * **여기서 검증하지 못하는 것**과 그 자리를 메우는 것:
 *
 * | 미검증 | 메우는 것 |
 * |---|---|
 * | 요청 하나가 워커 하나로 예약된다(R-021) | `SharedPlaceRepositoryImpl`(T064·T071) |
 * | 빈 목록 시트가 안내 문구를 그리고 카드 자리를 대체한다(FR-013) | `RoomPickerEmpty`(T039) |
 * | 저장 완료 토스트가 뜬 뒤 Activity가 물러난다(FR-011) | `ShareReceiverActivity`(T041) |
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ShareReceiverViewModelTest {
    private val roomRepository = FakeRoomRepository()
    private val sharedPlaceRepository = FakeSharedPlaceRepository()
    private val anonymousAuthRepository = FakeAnonymousAuthRepository()

    @Before
    fun setUp() {
        // viewModelScope가 Main에서 돌아 세션 확인·목록 조회가 생성 직후 끝나 있도록 한다.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** 선택은 카드가 아니라 상태 한 곳에만 있다 — 같은 방을 다시 누르면 해제다(FR-007, UX-003). */
    @Test
    fun `방을 토글하면 선택에 더해지고 다시 토글하면 빠진다`() =
        runTest {
            roomRepository.rooms = threeRooms()
            val viewModel = createViewModel()

            viewModel.processIntent(ShareReceiverIntent.ToggleRoom(GROUP_ROOM_ID))
            assertEquals(setOf(GROUP_ROOM_ID), viewModel.state.value.selectedRoomIds)

            viewModel.processIntent(ShareReceiverIntent.ToggleRoom(OTHER_GROUP_ROOM_ID))
            assertEquals(setOf(GROUP_ROOM_ID, OTHER_GROUP_ROOM_ID), viewModel.state.value.selectedRoomIds)

            viewModel.processIntent(ShareReceiverIntent.ToggleRoom(GROUP_ROOM_ID))
            assertEquals(setOf(OTHER_GROUP_ROOM_ID), viewModel.state.value.selectedRoomIds)
        }

    /** 선택이 목록을 흔들지 않는다 — 카드가 다시 그려져도 선택이 흩어지지 않는 전제다(TS-015·TS-016). */
    @Test
    fun `토글은 방 목록을 다시 조회하지도 순서를 바꾸지도 않는다`() =
        runTest {
            roomRepository.rooms = threeRooms()
            val viewModel = createViewModel()
            val loaded = viewModel.state.value

            viewModel.processIntent(ShareReceiverIntent.ToggleRoom(GROUP_ROOM_ID))

            val afterToggle = viewModel.state.value
            assertEquals(loaded.rooms.map { it.id }, afterToggle.rooms.map { it.id })
            assertEquals(1, roomRepository.getRoomsCallCount)
        }

    /** 하나라도 골라야 저장할 곳이 정해진다. 해제로 0개가 되면 다시 닫힌다(FR-009 · TS-004·TS-005). */
    @Test
    fun `선택이 0개면 저장이 비활성이고 하나라도 고르면 활성된다`() =
        runTest {
            roomRepository.rooms = threeRooms()
            val viewModel = createViewModel()

            assertFalse(viewModel.state.value.isSaveEnabled)

            viewModel.processIntent(ShareReceiverIntent.ToggleRoom(GROUP_ROOM_ID))
            assertTrue(viewModel.state.value.isSaveEnabled)

            viewModel.processIntent(ShareReceiverIntent.ToggleRoom(GROUP_ROOM_ID))
            assertFalse(viewModel.state.value.isSaveEnabled)
        }

    /**
     * 복원할 세션이 없으면 목록 조회로 넘어가지 않는다(FR-019 · `data-model.md` §6).
     *
     * 조회를 세지 않으면 이 케이스는 "방이 없어서 비었다"와 구별되지 않는다. 세션 없이 나간 요청은 신원 증명이 없어
     * 프로그래머 버그로 터지므로(R-016·R-020), 조회가 **아예 일어나지 않는 것**이 검증 대상이다.
     */
    @Test
    fun `세션이 없으면 방 목록을 조회하지 않고 빈 목록이 된다`() =
        runTest {
            anonymousAuthRepository.session = null
            roomRepository.rooms = threeRooms()

            val state = createViewModel().state.value

            assertTrue(state.isEmpty)
            assertFalse(state.isSaveEnabled)
            assertEquals(0, roomRepository.getRoomsCallCount)
            assertEquals(1, anonymousAuthRepository.currentSessionCallCount)
        }

    /**
     * 조회 실패와 세션 없음이 **같은 상태**로 수렴한다(R-006 · FR-013).
     *
     * 두 상태를 같다고 단언하는 것이 이 케이스의 전부다 — 오류 슬롯이든 재시도 플래그든 둘을 가르는 필드가 하나라도
     * 생기면 이 단언이 깨진다. 실패가 실제로 일어났는지는 조회 횟수로 따로 확인한다. 그러지 않으면 조회를 건너뛴
     * 구현도 같은 상태를 내놓아 통과한다.
     */
    @Test
    fun `조회 실패는 세션 없음과 같은 빈 목록 상태로 수렴한다`() =
        runTest {
            val failingRoomRepository =
                FakeRoomRepository().apply {
                    rooms = threeRooms()
                    getRoomsFailure = MinoDomainException.Network(IOException("offline"))
                }
            val onFailure = createViewModel(roomRepository = failingRoomRepository).state.value

            val absentSession = FakeAnonymousAuthRepository().apply { session = null }
            val onAbsentSession = createViewModel(anonymousAuthRepository = absentSession).state.value

            assertEquals(1, failingRoomRepository.getRoomsCallCount)
            assertTrue(onFailure.isEmpty)
            assertFalse(onFailure.isSaveEnabled)
            assertEquals(onAbsentSession, onFailure)
        }

    /** 세션이 있으면 목록이 그대로 카드가 된다 — 개인방 최상단 고정은 UseCase가 이미 했다(FR-005·FR-006). */
    @Test
    fun `세션이 있으면 조회한 방이 모두 카드가 된다`() =
        runTest {
            roomRepository.rooms = threeRooms()

            val state = createViewModel().state.value

            assertFalse(state.isEmpty)
            assertEquals(
                listOf(PERSONAL_ROOM_ID, GROUP_ROOM_ID, OTHER_GROUP_ROOM_ID),
                state.rooms.map { it.id },
            )
        }

    /**
     * 저장은 고른 방 전부를 한 번에 확정한다(FR-010 · R-021 · TS-003).
     *
     * 방 단위로 쪼개는 것은 데이터 계층의 몫이므로(`data-model.md` §1.3) 여기서 보는 것은 **요청 한 건에 고른 방이
     * 빠짐없이 실려 나갔는지**다. `roomIds`의 순서는 어디에도 정의돼 있지 않아 집합으로 본다.
     */
    @Test
    fun `저장하면 고른 방이 모두 실린 예약을 요청하고 SavedAndFinish를 낸다`() =
        runTest {
            roomRepository.rooms = threeRooms()
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)

            viewModel.processIntent(ShareReceiverIntent.ToggleRoom(PERSONAL_ROOM_ID))
            viewModel.processIntent(ShareReceiverIntent.ToggleRoom(OTHER_GROUP_ROOM_ID))
            viewModel.processIntent(ShareReceiverIntent.Save)

            val request = sharedPlaceRepository.scheduled.single()
            assertEquals(SHARED_URL, request.url)
            assertEquals(2, request.roomIds.size)
            assertEquals(setOf(PERSONAL_ROOM_ID, OTHER_GROUP_ROOM_ID), request.roomIds.toSet())
            assertEquals(listOf(ShareReceiverSideEffect.SavedAndFinish), sideEffects)
        }

    /**
     * 시트가 떠 있는 동안 새 공유가 도착하면 저장 대상 링크만 갈리고 방 목록은 그대로다(EC-013 · R-024).
     *
     * 선택을 비우는 것은 앞선 공유를 위해 고른 방에 새 링크가 저장되는 것을 막기 위해서다.
     */
    @Test
    fun `새 공유가 도착하면 링크가 갈리고 선택이 비워지되 방 목록은 유지된다`() =
        runTest {
            roomRepository.rooms = threeRooms()
            val viewModel = createViewModel()
            viewModel.processIntent(ShareReceiverIntent.ToggleRoom(PERSONAL_ROOM_ID))
            val roomsBefore = viewModel.state.value.rooms

            viewModel.processIntent(ShareReceiverIntent.SharedUrlReplaced(OTHER_SHARED_URL))

            val state = viewModel.state.value
            assertTrue("앞선 공유의 선택이 남으면 새 링크가 고르지 않은 방에 저장된다", state.selectedRoomIds.isEmpty())
            assertFalse(state.isSaveEnabled)
            assertEquals("목록을 다시 받으면 두 번째 공유에서만 카드가 늦게 찬다", roomsBefore, state.rooms)
            assertEquals(1, roomRepository.getRoomsCallCount)
        }

    /** 갈아 끼운 뒤 저장하면 나중에 도착한 링크가 실린다 — 시트에 보이는 것과 저장되는 것이 갈리지 않는다. */
    @Test
    fun `새 공유가 도착한 뒤 저장하면 나중 링크가 실린다`() =
        runTest {
            roomRepository.rooms = threeRooms()
            val viewModel = createViewModel()

            viewModel.processIntent(ShareReceiverIntent.SharedUrlReplaced(OTHER_SHARED_URL))
            viewModel.processIntent(ShareReceiverIntent.ToggleRoom(PERSONAL_ROOM_ID))
            viewModel.processIntent(ShareReceiverIntent.Save)

            assertEquals(OTHER_SHARED_URL, sharedPlaceRepository.scheduled.single().url)
        }

    /** 저장할 방이 없는 시트에서도 저장 예약은 일어나지 않는다 — 이탈은 아무것도 남기지 않는다(TS-025). */
    @Test
    fun `방이 없으면 아무것도 예약되지 않는다`() =
        runTest {
            roomRepository.rooms = emptyList()

            createViewModel()

            assertTrue(sharedPlaceRepository.scheduled.isEmpty())
        }

    /**
     * 진입점이 넘긴 URL은 [SavedStateHandle]에 실려 온다고 **가정한다.**
     *
     * `EXTRA_TEXT`에서 URL을 뽑는 것은 Activity의 몫이고(`contracts/share-intent.md` §2.1), ViewModel은 이미 뽑힌
     * URL 하나를 받는다. 키를 문자열 리터럴로 두지 않고 [ShareReceiverViewModel]의 상수로 참조하는 것은,
     * 전달 수단이 달라지면 조용히 `null`을 읽는 대신 컴파일이 깨지게 하기 위해서다.
     *
     * [FakeResources]를 넘기는 이유는 목록 변환이 문구 포맷을 소유하기 때문이다 — 그 문구는 판정 대상이 아니다.
     */
    private fun createViewModel(
        roomRepository: FakeRoomRepository = this.roomRepository,
        anonymousAuthRepository: FakeAnonymousAuthRepository = this.anonymousAuthRepository,
        sharedPlaceRepository: FakeSharedPlaceRepository = this.sharedPlaceRepository,
    ): ShareReceiverViewModel =
        ShareReceiverViewModel(
            savedStateHandle = SavedStateHandle(mapOf(ShareReceiverViewModel.KEY_SHARED_URL to SHARED_URL)),
            getRoomPickerRooms = GetRoomPickerRoomsUseCase(roomRepository = roomRepository),
            sharedPlaceRepository = sharedPlaceRepository,
            anonymousAuthRepository = anonymousAuthRepository,
            resources = FakeResources(),
        )

    /** 수집을 인텐트보다 먼저 걸어 둔다 — 채널로 나가는 일회성 신호는 놓치면 되돌릴 수 없다. */
    private fun TestScope.collectSideEffects(viewModel: ShareReceiverViewModel): List<ShareReceiverSideEffect> {
        val collected = mutableListOf<ShareReceiverSideEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.sideEffect.toList(collected) }
        return collected
    }

    /** 개인방 하나와 공동방 둘. 개인방이 이미 최상단이라 정렬이 순서를 흔들지 않는다. */
    private fun threeRooms(): List<RoomSummary> =
        listOf(
            roomSummary(id = PERSONAL_ROOM_ID, type = RoomType.PERSONAL, name = "내 장소"),
            roomSummary(id = GROUP_ROOM_ID, type = RoomType.GROUP, name = "민호야 잘하자"),
            roomSummary(id = OTHER_GROUP_ROOM_ID, type = RoomType.GROUP, name = "성수 카페"),
        )

    private fun roomSummary(
        id: String,
        type: RoomType,
        name: String,
    ): RoomSummary =
        RoomSummary(
            id = id,
            name = name,
            description = "",
            type = type,
            color = RoomColor.GRAY,
            placeCount = 0,
            thumbnailImageUrls = emptyList(),
        )

    private companion object {
        /** Activity가 `EXTRA_TEXT`에서 뽑아 넘긴 URL. 도메인 검사는 서버가 하므로 형식을 좁히지 않는다. */
        const val SHARED_URL = "https://www.instagram.com/p/ABC123/"

        /** 시트가 떠 있는 동안 도착하는 두 번째 공유의 링크(EC-013). */
        const val OTHER_SHARED_URL = "https://www.instagram.com/p/ZZZ999/"

        const val PERSONAL_ROOM_ID = "room-personal"

        const val GROUP_ROOM_ID = "room-1"

        const val OTHER_GROUP_ROOM_ID = "room-2"
    }
}
