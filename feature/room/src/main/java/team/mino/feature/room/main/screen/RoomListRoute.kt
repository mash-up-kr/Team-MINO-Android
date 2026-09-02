package team.mino.feature.room.main.screen

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import team.mino.core.common.ui.architecture.CollectSideEffect
import team.mino.core.common.ui.scaffold.LocalBottomNavVisibility
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_ROOM_ID
import team.mino.feature.room.detail.screen.RoomDetailRoute
import team.mino.feature.room.detail.vm.RoomDetailViewModel
import team.mino.feature.room.main.model.BottomSheetLevel
import team.mino.feature.room.main.vm.RoomListIntent
import team.mino.feature.room.main.vm.RoomListSideEffect
import team.mino.feature.room.main.vm.RoomListViewModel
import team.mino.feature.room.placedetail.model.PlaceSheetLevel
import team.mino.feature.room.placedetail.screen.PlaceDetailRoute
import team.mino.feature.room.placedetail.vm.PlaceDetailViewModel

/**
 * 방 리스트 탭 그래프의 진입 Route — 유일한 화면.
 *
 * 방 상세는 별도 목적지가 아니라 [RoomListUiState.selectedRoomId] 로컬 상태로 전환한다
 * (`RoomNavigation.kt` KDoc 참고) — 그래서 [RoomDetailRoute]를 `navController.navigate()`가
 * 아니라 이 Route 안에서 직접 그린다. [RoomListScreen]의 `detailContent` 슬롯으로 넘겨 지도
 * (`RoomListMap`)를 감싼 같은 [BoxScope] 안에 얹는다 — 그래야 지도가 리스트↔상세 전환에도 같은
 * 컴포지션에 남아 카메라가 리셋되지 않는다.
 *
 * **장소 상세도 같은 자리에 같은 방식으로 얹는다**([RoomListUiState.selectedPinId], `placeDetailContent`
 * 슬롯). 그래서 이 Route가 세 갈래의 시스템 뒤로가기·바텀 네비게이션 노출을 한곳에서 판정한다
 * (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §2.5·§2.6).
 *
 * @param onOpenExternalMap 장소 상세의 [지도보기] — 외부 지도 앱으로 연다(FR-016). 실행 주체가 `MainActivity`라
 *   `roomGraph`가 내려준 콜백을 그대로 흘린다.
 * @param onOpenSourceLink 장소 상세의 [원문보기] — 장소의 원문 링크를 연다(FR-017). 같은 이유로 흘리기만 한다.
 */
@Composable
internal fun RoomListRoute(
    onOpenExternalMap: (mapUrl: String?, query: String) -> Unit,
    onOpenSourceLink: (url: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoomListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = checkNotNull(LocalActivity.current) { "RoomListRoute는 Activity 컨텍스트 안에서만 그려진다." }
    val selectedRoomId = state.selectedRoomId
    val selectedPinId = state.selectedPinId

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.processIntent(RoomListIntent.OnLocationPermissionResult(granted))
    }

    val roomFormResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val createdRoomId = result.data?.getStringExtra(EXTRA_ROOM_FORM_RESULT_ROOM_ID)
        viewModel.processIntent(RoomListIntent.OnRoomFormResult(createdRoomId))
    }

    CollectSideEffect(sideEffect = viewModel.sideEffect) { effect ->
        when (effect) {
            RoomListSideEffect.RequestLocationPermission ->
                locationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                )

            RoomListSideEffect.NavigateToRoomForm ->
                viewModel.roomFormLauncher.launch(activity, resultLauncher = roomFormResultLauncher)
        }
    }

    // ON_RESUME마다 다시 보낸다 — 인스타그램 공유 시트 등 외부 앱에 다녀온 뒤 이 화면으로 돌아왔을 때도
    // 방 목록·장소·멤버가 새로고침돼야 한다(RoomListViewModel.observeMyRooms KDoc 참고). 최초 진입도
    // 이 이벤트로 커버된다 — 컴포지션 시점에 이미 RESUMED면 즉시 한 번 발행된다.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.processIntent(RoomListIntent.OnScreenEntered)
    }

    // 방 상세·장소 상세를 보는 중엔 시스템 뒤로가기/제스처가 곧장 탭·앱을 벗어나지 않고 먼저 한 겹 위로
    // 올라온다 — 우선순위는 장소 상세 → 방 상세 → 리스트 → 탭·앱 이탈이다
    // (`docs/specs/place-detail/contracts/place-detail-entry.md` §4). 장소 상세를 닫는 경로는 [나가기]와
    // 이 뒤로가기 둘뿐이며 같은 인텐트로 모인다 — 시트 드래그는 닫지 못한다(EC-003).
    BackHandler(enabled = selectedPinId != null || selectedRoomId != null) {
        val intent = if (selectedPinId != null) {
            RoomListIntent.OnClosePlaceDetailClick
        } else {
            RoomListIntent.OnCloseRoomDetailClick
        }
        viewModel.processIntent(intent)
    }

    // ImmersiveRoute는 목적지 단위 마커라 이 화면(같은 목적지 안에서 로컬 상태로 전환)엔 못 쓴다 —
    // LocalBottomNavVisibility로 대체(core/common/ui/scaffold/LocalBottomNavVisibility.kt KDoc 참고).
    val bottomNavVisibility = LocalBottomNavVisibility.current
    val isDetailMode = selectedRoomId != null
    // [state.isNudgeSheetVisible]이 화면 전체(바텀 네비게이션 자리까지)를 덮는 딤 팝업([RoomNudgeAutoSheet])의
    // 표출 여부다 — 팝업이 떠 있는 동안엔 셸의 바텀 네비게이션도 함께 숨겨야 실기기에서 팝업 액션 영역
    // 아래로 네비게이션 바가 비쳐 보이지 않는다(실기기 확인된 결함).
    //
    // 장소 상세(FR-020)도 같은 판정식에 항을 하나 더하는 것으로 끝난다 — 이 값을 다투는 곳이 둘이 되지
    // 않도록 새 `DisposableEffect`를 만들지 않는다(`docs/specs/place-detail/research.md` D19).
    DisposableEffect(selectedPinId, isDetailMode, state.isNudgeSheetVisible) {
        bottomNavVisibility.value = selectedPinId == null && !isDetailMode && !state.isNudgeSheetVisible
        onDispose { bottomNavVisibility.value = true }
    }

    // 상세 시트가 Full인지(=지도가 안 새어 나와야 하는지)는 상세 자신의 sheetLevel로만 판정할 수 있다.
    // rememberDetailSheetLevel이 같은 key(roomId)로 hiltViewModel을 다시 조회해 RoomDetailRoute가 쓰는
    // 것과 같은 ViewModel 인스턴스를 그대로 얻는다(ViewModelStore가 key로 캐시) — 런처·SideEffect 배선을
    // 중복시키지 않고 이 화면(RoomListScreen)이 필요로 하는 sheetLevel 하나만 읽는다.
    val detailSheetLevel = selectedRoomId?.let { rememberDetailSheetLevel(it) }
    val placeDetailSheetLevel = selectedPinId?.let { rememberPlaceDetailSheetLevel(it) }

    RoomListScreen(
        state = state,
        onIntent = viewModel::processIntent,
        modifier = modifier,
        detailSheetLevel = detailSheetLevel,
        placeDetailSheetLevel = placeDetailSheetLevel,
        detailContent = if (selectedRoomId != null) {
            {
                RoomDetailRoute(
                    roomId = selectedRoomId,
                    onBack = { viewModel.processIntent(RoomListIntent.OnCloseRoomDetailClick) },
                    onCurrentLocationClick = { viewModel.processIntent(RoomListIntent.OnCurrentLocationClick) },
                    onOpenPlaceDetail = { pinId -> viewModel.processIntent(RoomListIntent.OnPlaceSelected(pinId)) },
                )
            }
        } else {
            null
        },
        placeDetailContent = if (selectedPinId != null) {
            {
                PlaceDetailRoute(
                    pinId = selectedPinId,
                    // [나가기]는 화면을 끝내는 것이 아니라 selectedPinId를 비우는 것이다 — 그러면 그 아래
                    // 「지금 보고 있는 방」의 방 상세가 그대로 드러난다(FR-009). 시스템 뒤로가기도 같은
                    // 인텐트로 온다.
                    onExit = { viewModel.processIntent(RoomListIntent.OnClosePlaceDetailClick) },
                    // 앱 밖으로 나가는 둘은 Activity가 실행한다 — 이 Route는 셸에서 내려온 콜백을 그대로
                    // 잇기만 한다(FR-016·FR-017, `docs/architecture/feature-navigation.md` 3장).
                    onOpenExternalMap = onOpenExternalMap,
                    onOpenSourceLink = onOpenSourceLink,
                    // 장소 상세는 자기 카메라 상태를 갖지 않는다 — 방 상세와 같은 인텐트로 직결한다
                    // (`docs/specs/place-detail/research.md` D25).
                    onCurrentLocationClick = { viewModel.processIntent(RoomListIntent.OnCurrentLocationClick) },
                    // 「지금 보고 있는 방」을 쥔 쪽이 여기다 — 두 값을 인텐트 하나로 넘겨 selectedPinId·
                    // selectedRoomId가 한 번에 갱신되게 한다. 둘을 나눠 보내면 마커 양식·코멘트·[나가기]
                    // 목적지가 서로 다른 방을 가리키는 중간 상태가 생긴다(FR-025 · SC-009).
                    onSwitchRoom = { pinId, roomId ->
                        viewModel.processIntent(RoomListIntent.OnPlaceDetailRoomSwitched(pinId, roomId))
                    },
                )
            }
        } else {
            null
        },
    )
}

/**
 * [selectedRoomId]로 열린 방 상세의 현재 [BottomSheetLevel]. [RoomDetailRoute]와 같은 `roomId` key로
 * [RoomDetailViewModel]을 다시 조회해 같은 인스턴스를 얻는다(ViewModelStore가 key로 캐시) — `viewModel`을
 * 기본 파라미터로 둬야 [hiltViewModel] 호출이 컴포저블 시그니처에서 명시적으로 드러난다
 * (compose-lints `ViewModels` 규칙).
 */
@Composable
private fun rememberDetailSheetLevel(
    roomId: String,
    viewModel: RoomDetailViewModel = hiltViewModel<RoomDetailViewModel, RoomDetailViewModel.Factory>(
        key = roomId,
        creationCallback = { factory -> factory.create(roomId) },
    ),
): BottomSheetLevel {
    val detailState by viewModel.state.collectAsStateWithLifecycle()
    return detailState.sheetLevel
}

/**
 * [selectedPinId]로 열린 장소 상세의 현재 [PlaceSheetLevel]. [rememberDetailSheetLevel]과 같은 수법이다 —
 * [PlaceDetailRoute]와 같은 `pinId` key로 [PlaceDetailViewModel]을 다시 조회해 같은 인스턴스에서
 * `sheetLevel` 하나만 읽는다(ViewModelStore가 key로 캐시).
 *
 * 상태 전체가 아니라 `sheetLevel`만 흘려 구독한다 — 여기가 다시 그려지면 아래로 내려가는 오버레이 람다가
 * 새로 만들어져 장소 상세 트리 전체가 함께 다시 그려지므로, 코멘트 한 글자마다 그 값이 오게 두지 않는다.
 */
@Composable
private fun rememberPlaceDetailSheetLevel(
    selectedPinId: String,
    viewModel: PlaceDetailViewModel = hiltViewModel<PlaceDetailViewModel, PlaceDetailViewModel.Factory>(
        key = selectedPinId,
        creationCallback = { factory -> factory.create(selectedPinId) },
    ),
): PlaceSheetLevel {
    val sheetLevelFlow = remember(viewModel) { viewModel.state.map { it.sheetLevel }.distinctUntilChanged() }
    val sheetLevel by sheetLevelFlow.collectAsStateWithLifecycle(viewModel.state.value.sheetLevel)
    return sheetLevel
}
