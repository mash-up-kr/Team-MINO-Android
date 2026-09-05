package team.mino.feature.room.main.vm

import androidx.lifecycle.SavedStateHandle
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.Place
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.model.PlaceDetail
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomListSortOption
import team.mino.core.domain.model.RoomMemberSummary
import team.mino.core.domain.model.RoomThumbnail
import team.mino.core.domain.usecase.EnsureAnonymousSessionUseCase
import team.mino.core.navigation.entry.PlaceDetailEntryOrigin
import team.mino.core.navigation.entry.PlaceDetailRequestHolder
import team.mino.core.navigation.entry.RoomDetailRequestHolder
import team.mino.feature.room.fake.FakeAnonymousAuthRepository
import team.mino.feature.room.fake.FakeLocationContext
import team.mino.feature.room.fake.FakePlaceRepository
import team.mino.feature.room.fake.FakeRoomFormLauncher
import team.mino.feature.room.fake.FakeRoomPlacesRepository
import team.mino.feature.room.fake.FakeRoomPreferencesRepository
import team.mino.feature.room.fake.FakeRoomRepository
import team.mino.feature.room.main.component.DefaultMapCenter
import team.mino.feature.room.main.model.BottomSheetLevel
import kotlin.time.Clock
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
    private val roomPlacesRepository = FakeRoomPlacesRepository()
    private val placeRepository = FakePlaceRepository()

    /** 홀더는 요청을 담는 그릇일 뿐이라 테스트 더블을 두지 않고 실물을 쓴다. */
    private val placeDetailRequestHolder = PlaceDetailRequestHolder()
    private val roomDetailRequestHolder = RoomDetailRequestHolder()
    private val roomPreferencesRepository = FakeRoomPreferencesRepository()

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
    fun `방 카드 선택은 selectedRoomId를 설정한다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(RoomListIntent.OnRoomCardClick("room-1"))

            assertEquals("room-1", viewModel.state.value.selectedRoomId)
        }

    @Test
    fun `방 상세 닫기는 selectedRoomId를 null로 되돌린다`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.processIntent(RoomListIntent.OnRoomCardClick("room-1"))

            viewModel.processIntent(RoomListIntent.OnCloseRoomDetailClick)

            assertEquals(null, viewModel.state.value.selectedRoomId)
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

            viewModel.processIntent(RoomListIntent.OnRoomFormResult("created-room"))

            assertEquals("created-room", viewModel.state.value.selectedRoomId)
        }

    @Test
    fun `방 생성 폼 결과가 취소(null)면 아무 것도 하지 않는다`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.processIntent(RoomListIntent.OnRoomFormResult(null))

            assertEquals(null, viewModel.state.value.selectedRoomId)
        }

    @Test
    fun `장소를 선택하면 그 핀만 선택 표시되고 카메라가 그 장소로 옮겨간다`() =
        runTest {
            roomRepository.givenRooms(room(id = "personal", isPersonal = true))
            roomPlacesRepository.givenPlaces(place(id = "pin-1", location = PIN_1_LOCATION), place(id = "pin-2"))
            val viewModel = createViewModel(permissionGranted = false)
            advanceUntilIdle()
            val requestIdBefore = viewModel.state.value.mapCenterRequestId

            viewModel.processIntent(RoomListIntent.OnPlaceSelected("pin-1"))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals("pin-1", state.selectedPinId)
            assertEquals(PIN_1_LOCATION, state.mapCenter)
            assertEquals(requestIdBefore + 1, state.mapCenterRequestId)
            assertEquals(listOf("pin-1"), state.mapPins.filter { it.selected }.map { it.place.id })
        }

    /** 아직 목록에 없는 핀을 지목하면 좌표를 모르는 채로 카메라를 엉뚱한 곳으로 옮기지 않는다. */
    @Test
    fun `좌표를 모르는 핀을 선택하면 카메라는 그대로 둔다`() =
        runTest {
            val viewModel = createViewModel(permissionGranted = false)

            viewModel.processIntent(RoomListIntent.OnPlaceSelected("unknown-pin"))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals("unknown-pin", state.selectedPinId)
            assertNull(state.mapCenter)
            assertEquals(0, state.mapCenterRequestId)
        }

    /**
     * [TS-056] 탭 간 진입은 방·핀·카메라를 **함께** 세운다.
     *
     * 좌표는 핀 상세 응답이 준다 — 홈에서 콜드 진입하면 `placesByRoomId`가 아직 비어 있어
     * `OnPlaceSelected`처럼 목록에서 찾을 수 없고, 못 찾아 카메라가 안 움직이면 선택 핀이 화면 밖에 남는다.
     */
    @Test
    fun `탭 간 요청으로 장소 상세를 열면 방과 핀과 카메라를 함께 세운다`() =
        runTest {
            placeRepository.givenPlaceDetail(placeDetail(pinId = "pin-1", roomId = "room-1"))
            val viewModel = createViewModel(permissionGranted = false)
            advanceUntilIdle()
            val requestIdBefore = viewModel.state.value.mapCenterRequestId

            placeDetailRequestHolder.request("pin-1", PlaceDetailEntryOrigin.NOTIFICATION)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals("pin-1", state.selectedPinId)
            assertEquals("room-1", state.selectedRoomId)
            assertEquals(PIN_1_LOCATION, state.mapCenter)
            assertEquals(requestIdBefore + 1, state.mapCenterRequestId)
            assertNull(placeDetailRequestHolder.pending.value)
        }

    /**
     * [EC-030] 진입에 딸린 자동 위치 이동은 장소 상세가 열려 있는 동안 카메라를 가져가지 않는다.
     *
     * 홈·알림 진입은 탭 전환(→ `OnScreenEntered`)과 장소 상세 열기가 같은 순간이라 둘이 겹친다.
     */
    @Test
    fun `장소 상세가 열려 있으면 진입 시 자동 위치 이동이 카메라를 덮지 않는다`() =
        runTest {
            placeRepository.givenPlaceDetail(placeDetail(pinId = "pin-1", roomId = "room-1"))
            val viewModel = createViewModel(permissionGranted = true)
            placeDetailRequestHolder.request("pin-1", PlaceDetailEntryOrigin.NOTIFICATION)
            advanceUntilIdle()
            val requestIdBefore = viewModel.state.value.mapCenterRequestId

            viewModel.processIntent(RoomListIntent.OnScreenEntered)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(PIN_1_LOCATION, state.mapCenter)
            assertEquals(requestIdBefore, state.mapCenterRequestId)
        }

    /** [EC-030] 막는 것은 진입에 딸린 자동 이동뿐이다 — 사용자가 직접 누른 [현재 위치]는 그대로 움직인다. */
    @Test
    fun `장소 상세가 열려 있어도 현재 위치 버튼은 카메라를 옮긴다`() =
        runTest {
            placeRepository.givenPlaceDetail(placeDetail(pinId = "pin-1", roomId = "room-1"))
            val viewModel = createViewModel(permissionGranted = true)
            placeDetailRequestHolder.request("pin-1", PlaceDetailEntryOrigin.NOTIFICATION)
            advanceUntilIdle()
            val requestIdBefore = viewModel.state.value.mapCenterRequestId

            viewModel.processIntent(RoomListIntent.OnCurrentLocationClick)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(DefaultMapCenter, state.mapCenter)
            assertEquals(requestIdBefore + 1, state.mapCenterRequestId)
        }

    /** [TS-006] 장소 상세를 닫으면 `selectedRoomId`가 남아 그 방의 방 상세가 다시 드러난다. */
    @Test
    fun `장소 상세 닫기는 selectedPinId만 비우고 selectedRoomId는 남긴다`() =
        runTest {
            roomRepository.givenRooms(room(id = "personal", isPersonal = true))
            roomPlacesRepository.givenPlaces(place(id = "pin-1", location = PIN_1_LOCATION))
            val viewModel = createViewModel(permissionGranted = false)
            advanceUntilIdle()
            viewModel.processIntent(RoomListIntent.OnRoomCardClick("room-1"))
            viewModel.processIntent(RoomListIntent.OnPlaceSelected("pin-1"))
            advanceUntilIdle()

            viewModel.processIntent(RoomListIntent.OnClosePlaceDetailClick)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertNull(state.selectedPinId)
            assertEquals("room-1", state.selectedRoomId)
            assertTrue(state.mapPins.none { it.selected })
        }

    /**
     * [TS-037] 홈에서 들어온 [나가기]는 방 상세가 아니라 홈 탭으로 되돌린다.
     *
     * [EC-031] 이때 `selectedRoomId`도 함께 비운다 — 홈 진입이 방과 핀을 함께 세우므로, 핀만 비우면
     * 사용자가 연 적 없는 방 상세가 저장 탭에 남는다. **[나가기] 직후 화면으로는 드러나지 않는
     * 결함이라** 상태로만 잡을 수 있다.
     */
    @Test
    fun `홈에서 들어온 장소 상세를 닫으면 방까지 비우고 홈 복귀를 올린다`() =
        runTest {
            placeRepository.givenPlaceDetail(placeDetail(pinId = "pin-1", roomId = "room-1"))
            val viewModel = createViewModel(permissionGranted = false)
            val sideEffects = collectSideEffects(viewModel)
            placeDetailRequestHolder.request("pin-1", PlaceDetailEntryOrigin.HOME)
            advanceUntilIdle()
            assertTrue(viewModel.state.value.returnsToHomeOnClose)

            viewModel.processIntent(RoomListIntent.OnClosePlaceDetailClick)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertNull(state.selectedPinId)
            assertNull(state.selectedRoomId)
            assertFalse(state.returnsToHomeOnClose)
            assertEquals(listOf(RoomListSideEffect.NavigateToHome), sideEffects)
        }

    /** [TS-007] 홈이 아닌 탭 간 진입(알림)은 예외가 아니다 — 그 방의 방 상세로 나간다. */
    @Test
    fun `알림에서 들어온 장소 상세를 닫으면 방 상세가 드러나고 홈 복귀를 올리지 않는다`() =
        runTest {
            placeRepository.givenPlaceDetail(placeDetail(pinId = "pin-1", roomId = "room-1"))
            val viewModel = createViewModel(permissionGranted = false)
            val sideEffects = collectSideEffects(viewModel)
            placeDetailRequestHolder.request("pin-1", PlaceDetailEntryOrigin.NOTIFICATION)
            advanceUntilIdle()
            assertFalse(viewModel.state.value.returnsToHomeOnClose)

            viewModel.processIntent(RoomListIntent.OnClosePlaceDetailClick)
            advanceUntilIdle()

            assertNull(viewModel.state.value.selectedPinId)
            assertEquals("room-1", viewModel.state.value.selectedRoomId)
            assertTrue(sideEffects.isEmpty())
        }

    /** [TS-057] [저장된 방]으로 방을 바꾸면 홈 복귀 예외가 소멸하고 바뀐 방의 방 상세로 나간다. */
    @Test
    fun `홈에서 들어와도 방을 바꾸면 홈이 아니라 바뀐 방으로 나간다`() =
        runTest {
            placeRepository.givenPlaceDetail(placeDetail(pinId = "pin-1", roomId = "room-1"))
            val viewModel = createViewModel(permissionGranted = false)
            val sideEffects = collectSideEffects(viewModel)
            placeDetailRequestHolder.request("pin-1", PlaceDetailEntryOrigin.HOME)
            advanceUntilIdle()

            viewModel.processIntent(RoomListIntent.OnPlaceDetailRoomSwitched(pinId = "pin-b", roomId = "room-b"))
            viewModel.processIntent(RoomListIntent.OnClosePlaceDetailClick)
            advanceUntilIdle()

            assertEquals("room-b", viewModel.state.value.selectedRoomId)
            assertTrue(sideEffects.isEmpty())
        }

    /**
     * [EC-032] 예외는 되살아나지 않는다 — 판정 기준이 "지금 어느 방인가"가 아니라 "바꾼 적이 있는가"다.
     *
     * 원래 방으로 되돌리는 것도 「바꾼 적」에 들어, 진입 시점의 방으로 돌아와도 홈으로 나가지 않는다.
     */
    @Test
    fun `방을 바꿨다가 원래 방으로 되돌려도 홈 복귀는 살아나지 않는다`() =
        runTest {
            placeRepository.givenPlaceDetail(placeDetail(pinId = "pin-1", roomId = "room-1"))
            val viewModel = createViewModel(permissionGranted = false)
            val sideEffects = collectSideEffects(viewModel)
            placeDetailRequestHolder.request("pin-1", PlaceDetailEntryOrigin.HOME)
            advanceUntilIdle()

            viewModel.processIntent(RoomListIntent.OnPlaceDetailRoomSwitched(pinId = "pin-b", roomId = "room-b"))
            viewModel.processIntent(RoomListIntent.OnPlaceDetailRoomSwitched(pinId = "pin-1", roomId = "room-1"))
            viewModel.processIntent(RoomListIntent.OnClosePlaceDetailClick)
            advanceUntilIdle()

            assertEquals("room-1", viewModel.state.value.selectedRoomId)
            assertTrue(sideEffects.isEmpty())
        }

    /**
     * [FR-009] 홈에서 연 상세가 떠 있는 채로 지도 마커를 눌러 다른 장소로 옮겨가면 예외가 따라붙지
     * 않는다 — 사용자가 이미 저장 탭 안에서 장소를 직접 고른 것이다.
     */
    @Test
    fun `홈 진입 뒤 지도 마커로 다른 장소를 고르면 홈 복귀가 풀린다`() =
        runTest {
            roomPlacesRepository.givenPlaces(place(id = "pin-2", location = PIN_1_LOCATION))
            placeRepository.givenPlaceDetail(placeDetail(pinId = "pin-1", roomId = "room-1"))
            val viewModel = createViewModel(permissionGranted = false)
            val sideEffects = collectSideEffects(viewModel)
            placeDetailRequestHolder.request("pin-1", PlaceDetailEntryOrigin.HOME)
            advanceUntilIdle()

            viewModel.processIntent(RoomListIntent.OnPlaceSelected("pin-2"))
            viewModel.processIntent(RoomListIntent.OnClosePlaceDetailClick)
            advanceUntilIdle()

            assertEquals("room-1", viewModel.state.value.selectedRoomId)
            assertTrue(sideEffects.isEmpty())
        }

    /** [FR-025] [저장된 방] 전환은 보고 있는 방과 선택 핀을 함께 갈아 끼운다(TS-045·TS-046). */
    @Test
    fun `저장된 방 전환은 selectedPinId와 selectedRoomId를 함께 바꾼다`() =
        runTest {
            val viewModel = createViewModel(permissionGranted = false)
            viewModel.processIntent(RoomListIntent.OnRoomCardClick("room-a"))
            viewModel.processIntent(RoomListIntent.OnPlaceSelected("pin-a"))
            advanceUntilIdle()

            viewModel.processIntent(RoomListIntent.OnPlaceDetailRoomSwitched(pinId = "pin-b", roomId = "room-b"))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals("pin-b", state.selectedPinId)
            assertEquals("room-b", state.selectedRoomId)
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

    @Test
    fun `Nudge 팝업을 나중에 만들래요로 닫으면 지금 시각을 저장한다`() =
        runTest {
            roomRepository.givenRooms(room(id = "personal", isPersonal = true))
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.processIntent(RoomListIntent.OnNudgeDismissClick)
            advanceUntilIdle()

            assertEquals(1, roomPreferencesRepository.recordedDismissedAts.size)
        }

    @Test
    fun `2주 이내에 닫았으면 재진입해도 Nudge 팝업이 억제된다`() =
        runTest {
            roomPreferencesRepository.dismissedAt = Clock.System.now() - 1.days
            roomRepository.givenRooms(room(id = "personal", isPersonal = true))
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.processIntent(RoomListIntent.OnScreenEntered)
            advanceUntilIdle()

            assertTrue(viewModel.state.value.nudgeSheetDismissed)
            assertFalse(viewModel.state.value.isNudgeSheetVisible)
        }

    @Test
    fun `2주가 지나면 재진입 시 Nudge 팝업이 다시 표출된다`() =
        runTest {
            roomPreferencesRepository.dismissedAt = Clock.System.now() - 15.days
            roomRepository.givenRooms(room(id = "personal", isPersonal = true))
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.processIntent(RoomListIntent.OnScreenEntered)
            advanceUntilIdle()

            assertFalse(viewModel.state.value.nudgeSheetDismissed)
            assertTrue(viewModel.state.value.isNudgeSheetVisible)
        }

    private fun createViewModel(permissionGranted: Boolean = true): RoomListViewModel =
        RoomListViewModel(
            // 빈 SavedStateHandle은 RoomMain.initialRoomId를 기본값(null)으로 디코딩한다 — 딥링크
            // 진입(초기 selectedRoomId)은 RoomNavigationTest류가 아니라 여기 관심사가 아니다.
            savedStateHandle = SavedStateHandle(),
            context = FakeLocationContext(permissionGranted = permissionGranted),
            roomRepository = roomRepository,
            roomPlacesRepository = roomPlacesRepository,
            roomPreferencesRepository = roomPreferencesRepository,
            ensureAnonymousSessionUseCase = EnsureAnonymousSessionUseCase(FakeAnonymousAuthRepository()),
            placeRepository = placeRepository,
            placeDetailRequestHolder = placeDetailRequestHolder,
            roomDetailRequestHolder = roomDetailRequestHolder,
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
            memberSummary = RoomMemberSummary(visibleAvatars = emptyList(), overflowCount = 0),
            lastPlaceSavedAt = lastPlaceSavedAt,
            commentCount = 0,
        )

    @OptIn(ExperimentalTime::class)
    private fun place(
        id: String,
        location: GeoPoint = GeoPoint(latitude = 0.0, longitude = 0.0),
    ): Place =
        Place(
            id = id,
            // 핀 id와 장소 id를 갈라 두어야 「어느 방에 이미 담겼는지」를 묻는 쪽이 잘못된 키를 써도 드러난다.
            placeId = "place-of-$id",
            name = id,
            address = "",
            category = PlaceCategoryFilter.CAFE,
            thumbnailUrls = emptyList(),
            savedAt = Instant.DISTANT_PAST,
            commentCount = 0,
            isGgukPick = false,
            distanceMeters = null,
            location = location,
        )

    private fun placeDetail(
        pinId: String,
        roomId: String,
        location: GeoPoint = PIN_1_LOCATION,
    ): PlaceDetail =
        PlaceDetail(
            pinId = pinId,
            roomId = roomId,
            placeId = "place-$pinId",
            name = pinId,
            address = "",
            location = location,
            imageUrls = emptyList(),
            registrant = null,
            sourceUrl = null,
            mapUrl = null,
        )

    @OptIn(ExperimentalTime::class)
    private companion object {
        val DAY_1 = Instant.DISTANT_PAST + 1.days
        val DAY_10 = Instant.DISTANT_PAST + 10.days
        val PIN_1_LOCATION = GeoPoint(latitude = 37.5665, longitude = 126.9780)
    }
}
