package team.mino.feature.room.main.vm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomListSortOption
import team.mino.core.domain.model.RoomMemberSummary
import team.mino.core.domain.model.RoomThumbnail
import team.mino.core.domain.usecase.EnsureAnonymousSessionUseCase
import team.mino.feature.room.fake.FakeAnonymousAuthRepository
import team.mino.feature.room.fake.FakeLocationContext
import team.mino.feature.room.fake.FakeRoomDetailLauncher
import team.mino.feature.room.fake.FakeRoomFormLauncher
import team.mino.feature.room.fake.FakeRoomRepository
import team.mino.feature.room.main.component.DefaultMapCenter
import team.mino.feature.room.main.model.BottomSheetLevel
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 방 리스트 탭의 인텐트 처리를 판정한다.
 *
 * 계약은 `contracts/room-list-main-contract.md`가 소유한다.
 *
 * **위치 조회 관련 제약**: [RoomListViewModel]은 `Context`를 직접 받아 `LocationManager`·권한을
 * 조회한다. Robolectric이 이 저장소에 없어 실물 `LocationManager`를 흉내 낼 수 없으므로,
 * [FakeLocationContext]의 `LocationManager`는 항상 `null`이다 — "권한은 허용됐지만 실제 GPS 픽스를
 * 구하는" 경로(`RoomListViewModel.requestSingleLocationUpdate`)는 이 테스트가 검증하지 못하고,
 * "권한 허용 + 위치를 못 구해 기본 좌표로 떨어지는" 경로까지만 검증한다.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class RoomListViewModelTest {
    private val roomRepository = FakeRoomRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `위치 권한이 없으면 진입 시 권한 요청 SideEffect를 발행한다`() =
        runTest {
            val viewModel = createViewModel(permissionGranted = false)
            val sideEffects = collectSideEffects(viewModel)

            viewModel.processIntent(RoomListIntent.OnScreenEntered)
            advanceUntilIdle()

            assertEquals(listOf(RoomListSideEffect.RequestLocationPermission), sideEffects)
        }

    @Test
    fun `위치 권한이 있으면 진입 시 권한을 다시 묻지 않고 mapCenter를 설정한다`() =
        runTest {
            val viewModel = createViewModel(permissionGranted = true)
            val sideEffects = collectSideEffects(viewModel)

            viewModel.processIntent(RoomListIntent.OnScreenEntered)
            advanceUntilIdle()

            assertTrue(sideEffects.isEmpty())
            // FakeLocationContext는 LocationManager가 없어 실제 GPS 픽스를 구하지 못하므로
            // EC-002의 기본 디폴트 좌표로 떨어진다(클래스 KDoc 「위치 조회 관련 제약」 참고).
            assertEquals(DefaultMapCenter, viewModel.state.value.mapCenter)
        }

    @Test
    fun `권한 거부 결과를 받으면 EC-002에 따라 기본 디폴트 좌표로 설정한다`() =
        runTest {
            val viewModel = createViewModel(permissionGranted = false)

            viewModel.processIntent(RoomListIntent.OnLocationPermissionResult(granted = false))
            advanceUntilIdle()

            assertEquals(DefaultMapCenter, viewModel.state.value.mapCenter)
        }

    @Test
    fun `권한 허용 결과를 받으면 mapCenter를 갱신한다`() =
        runTest {
            val viewModel = createViewModel(permissionGranted = false)

            viewModel.processIntent(RoomListIntent.OnLocationPermissionResult(granted = true))
            advanceUntilIdle()

            assertEquals(DefaultMapCenter, viewModel.state.value.mapCenter)
        }

    @Test
    fun `권한이 없으면 현재 위치 버튼 클릭은 아무 것도 하지 않는다`() =
        runTest {
            val viewModel = createViewModel(permissionGranted = false)

            viewModel.processIntent(RoomListIntent.OnCurrentLocationClick)
            advanceUntilIdle()

            assertEquals(null, viewModel.state.value.mapCenter)
        }

    @Test
    fun `권한이 있으면 현재 위치 버튼 클릭이 mapCenter를 갱신한다`() =
        runTest {
            val viewModel = createViewModel(permissionGranted = true)

            viewModel.processIntent(RoomListIntent.OnCurrentLocationClick)
            advanceUntilIdle()

            assertEquals(DefaultMapCenter, viewModel.state.value.mapCenter)
        }

    /**
     * 실기기에서 재현된 버그: 사용자가 지도를 수동으로 옮긴 뒤 같은 위치로 돌아가려고 현재 위치
     * 버튼을 다시 누르면, `currentDeviceLocation()`이 이전과 같은 좌표를 돌려줘 `mapCenter` 값
     * 자체는 안 바뀐다. `RoomListMap`의 `LaunchedEffect`가 `mapCenter` 값으로만 키를 잡으면
     * 재실행되지 않아 카메라가 안 움직인다 — `mapCenterRequestId`가 매 클릭마다 증가해야
     * `LaunchedEffect(mapCenterRequestId)`가 값이 같아도 다시 실행된다.
     */
    @Test
    fun `현재 위치 버튼을 연속으로 눌러도 mapCenter 값이 같으면 mapCenterRequestId는 매번 증가한다`() =
        runTest {
            val viewModel = createViewModel(permissionGranted = true)

            viewModel.processIntent(RoomListIntent.OnCurrentLocationClick)
            advanceUntilIdle()
            val firstRequestId = viewModel.state.value.mapCenterRequestId

            viewModel.processIntent(RoomListIntent.OnCurrentLocationClick)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(DefaultMapCenter, state.mapCenter)
            assertEquals(firstRequestId + 1, state.mapCenterRequestId)
        }

    @Test
    fun `시트를 위로 드래그하면 Half에서 Full로 승격된다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(RoomListIntent.OnSheetDraggedUp)

            assertEquals(BottomSheetLevel.FULL, viewModel.state.value.sheetLevel)
        }

    @Test
    fun `Full 상태에서 위로 드래그해도 그대로 유지된다`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.processIntent(RoomListIntent.OnSheetDraggedUp)

            viewModel.processIntent(RoomListIntent.OnSheetDraggedUp)

            assertEquals(BottomSheetLevel.FULL, viewModel.state.value.sheetLevel)
        }

    @Test
    fun `Half 상태에서 아래로 드래그하면 Peek으로 축소된다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(RoomListIntent.OnSheetDraggedDown)

            assertEquals(BottomSheetLevel.PEEK, viewModel.state.value.sheetLevel)
        }

    @Test
    fun `Peek 상태에서 아래로 드래그해도 그대로 유지된다`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.processIntent(RoomListIntent.OnSheetDraggedDown)

            viewModel.processIntent(RoomListIntent.OnSheetDraggedDown)

            assertEquals(BottomSheetLevel.PEEK, viewModel.state.value.sheetLevel)
        }

    @Test
    fun `정렬 드롭다운 선택이 mapMarkerSort 상태를 갱신한다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(RoomListIntent.OnMapSortSelected(MapMarkerSortOption.LATEST))

            assertEquals(MapMarkerSortOption.LATEST, viewModel.state.value.mapMarkerSort)
        }

    @Test
    fun `카테고리 칩 선택이 categoryFilter 상태를 갱신한다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(RoomListIntent.OnCategoryFilterSelected(PlaceCategoryFilter.CAFE))

            assertEquals(PlaceCategoryFilter.CAFE, viewModel.state.value.categoryFilter)
        }

    @Test
    fun `공동방 목록을 정렬해도 개인방 고정은 유지된다`() =
        runTest {
            val personal = room(id = "personal", isPersonal = true)
            val old = room(id = "old", lastPlaceSavedAt = DAY_1)
            val recent = room(id = "recent", lastPlaceSavedAt = DAY_10)
            roomRepository.givenRooms(personal, old, recent)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.processIntent(RoomListIntent.OnRoomListSortSelected(RoomListSortOption.RECENTLY_SAVED))

            val state = viewModel.state.value
            assertEquals(personal, state.personalRoom)
            assertEquals(listOf(recent, old), state.groupRooms)
        }

    @Test
    fun `방 카드 선택은 방 상세 이동 SideEffect를 발행한다`() =
        runTest {
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)

            viewModel.processIntent(RoomListIntent.OnRoomCardClick("room-1"))
            advanceUntilIdle()

            assertEquals(listOf(RoomListSideEffect.NavigateToRoomDetail("room-1")), sideEffects)
        }

    @Test
    fun `공동방 생성 버튼은 방 생성 폼 이동 SideEffect를 발행한다`() =
        runTest {
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)

            viewModel.processIntent(RoomListIntent.OnAddRoomClick)
            advanceUntilIdle()

            assertEquals(listOf(RoomListSideEffect.NavigateToRoomForm), sideEffects)
        }

    @Test
    fun `Ghost Card·Nudge의 방 만들기도 같은 SideEffect를 발행한다`() =
        runTest {
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)

            viewModel.processIntent(RoomListIntent.OnGhostCardClick)
            viewModel.processIntent(RoomListIntent.OnNudgeCreateClick)
            advanceUntilIdle()

            assertEquals(
                listOf(RoomListSideEffect.NavigateToRoomForm, RoomListSideEffect.NavigateToRoomForm),
                sideEffects,
            )
        }

    @Test
    fun `방 생성 폼 결과에 생성된 방 ID가 있으면 방 상세로 체이닝한다`() =
        runTest {
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)

            viewModel.processIntent(RoomListIntent.OnRoomFormResult("created-room"))
            advanceUntilIdle()

            assertEquals(listOf(RoomListSideEffect.NavigateToRoomDetail("created-room")), sideEffects)
        }

    @Test
    fun `방 생성 폼 결과가 취소(null)면 아무 것도 하지 않는다`() =
        runTest {
            val viewModel = createViewModel()
            val sideEffects = collectSideEffects(viewModel)

            viewModel.processIntent(RoomListIntent.OnRoomFormResult(null))
            advanceUntilIdle()

            assertTrue(sideEffects.isEmpty())
        }

    @Test
    fun `공동방이 없으면 Nudge와 Ghost Card를 노출한다`() =
        runTest {
            roomRepository.givenRooms(room(id = "personal", isPersonal = true))
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertTrue(state.showNudge)
            assertTrue(state.showGhostCard)
        }

    @Test
    fun `공동방이 1개 이상이면 Nudge와 Ghost Card를 노출하지 않는다`() =
        runTest {
            roomRepository.givenRooms(room(id = "personal", isPersonal = true), room(id = "group-1"))
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.showNudge)
            assertFalse(state.showGhostCard)
        }

    private fun createViewModel(permissionGranted: Boolean = true): RoomListViewModel =
        RoomListViewModel(
            context = FakeLocationContext(permissionGranted = permissionGranted),
            roomRepository = roomRepository,
            ensureAnonymousSessionUseCase = EnsureAnonymousSessionUseCase(FakeAnonymousAuthRepository()),
            roomDetailLauncher = FakeRoomDetailLauncher(),
            roomFormLauncher = FakeRoomFormLauncher(),
        )

    /** 수집을 인텐트보다 먼저 걸어 둔다 — 채널로 나가는 일회성 신호는 놓치면 되돌릴 수 없다. */
    private fun TestScope.collectSideEffects(viewModel: RoomListViewModel): List<RoomListSideEffect> {
        val collected = mutableListOf<RoomListSideEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.sideEffect.toList(collected) }
        return collected
    }

    @OptIn(ExperimentalTime::class)
    private fun room(
        id: String,
        isPersonal: Boolean = false,
        lastPlaceSavedAt: Instant? = null,
    ): Room =
        Room(
            id = id,
            name = id,
            description = "",
            color = RoomColor.GRAY,
            ownerId = "owner",
            isPersonal = isPersonal,
            placeCount = 0,
            thumbnail = RoomThumbnail.ColorAndCharacter(color = null),
            memberSummary = RoomMemberSummary(visibleAvatarUrls = emptyList(), overflowCount = 0),
            lastPlaceSavedAt = lastPlaceSavedAt,
            commentCount = 0,
        )

    @OptIn(ExperimentalTime::class)
    private companion object {
        val DAY_1 = Instant.DISTANT_PAST + 1.days
        val DAY_10 = Instant.DISTANT_PAST + 10.days
    }
}
