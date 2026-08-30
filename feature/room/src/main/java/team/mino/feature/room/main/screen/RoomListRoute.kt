package team.mino.feature.room.main.screen

import android.Manifest
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import team.mino.core.common.ui.architecture.CollectSideEffect
import team.mino.core.common.ui.scaffold.LocalBottomNavVisibility
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_ROOM_ID
import team.mino.feature.room.detail.screen.RoomDetailRoute
import team.mino.feature.room.detail.vm.RoomDetailViewModel
import team.mino.feature.room.main.model.BottomSheetLevel
import team.mino.feature.room.main.vm.RoomListIntent
import team.mino.feature.room.main.vm.RoomListSideEffect
import team.mino.feature.room.main.vm.RoomListViewModel

/**
 * 방 리스트 탭 그래프의 진입 Route — 유일한 화면.
 *
 * 방 상세는 별도 목적지가 아니라 [RoomListUiState.selectedRoomId] 로컬 상태로 전환한다
 * (`RoomNavigation.kt` KDoc 참고) — 그래서 [RoomDetailRoute]를 `navController.navigate()`가
 * 아니라 이 Route 안에서 직접 그린다. [RoomListScreen]의 `detailContent` 슬롯으로 넘겨 지도
 * (`RoomListMap`)를 감싼 같은 [BoxScope] 안에 얹는다 — 그래야 지도가 리스트↔상세 전환에도 같은
 * 컴포지션에 남아 카메라가 리셋되지 않는다.
 */
@Composable
internal fun RoomListRoute(
    modifier: Modifier = Modifier,
    viewModel: RoomListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity
    val selectedRoomId = state.selectedRoomId

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

    // 방 상세를 보는 중엔 시스템 뒤로가기/제스처가 곧장 탭·앱을 벗어나지 않고 먼저 리스트로 복귀한다.
    BackHandler(enabled = selectedRoomId != null) {
        viewModel.processIntent(RoomListIntent.OnCloseRoomDetailClick)
    }

    // ImmersiveRoute는 목적지 단위 마커라 이 화면(같은 목적지 안에서 로컬 상태로 전환)엔 못 쓴다 —
    // LocalBottomNavVisibility로 대체(core/common/ui/scaffold/LocalBottomNavVisibility.kt KDoc 참고).
    val bottomNavVisibility = LocalBottomNavVisibility.current
    val isDetailMode = selectedRoomId != null
    DisposableEffect(isDetailMode) {
        bottomNavVisibility.value = !isDetailMode
        onDispose { bottomNavVisibility.value = true }
    }

    // 상세 시트가 Full인지(=지도가 안 새어 나와야 하는지)는 상세 자신의 sheetLevel로만 판정할 수 있다.
    // rememberDetailSheetLevel이 같은 key(roomId)로 hiltViewModel을 다시 조회해 RoomDetailRoute가 쓰는
    // 것과 같은 ViewModel 인스턴스를 그대로 얻는다(ViewModelStore가 key로 캐시) — 런처·SideEffect 배선을
    // 중복시키지 않고 이 화면(RoomListScreen)이 필요로 하는 sheetLevel 하나만 읽는다.
    val detailSheetLevel = selectedRoomId?.let { rememberDetailSheetLevel(it) }

    RoomListScreen(
        state = state,
        onIntent = viewModel::processIntent,
        modifier = modifier,
        detailSheetLevel = detailSheetLevel,
        detailContent = if (selectedRoomId != null) {
            {
                RoomDetailRoute(
                    roomId = selectedRoomId,
                    onBack = { viewModel.processIntent(RoomListIntent.OnCloseRoomDetailClick) },
                    onCurrentLocationClick = { viewModel.processIntent(RoomListIntent.OnCurrentLocationClick) },
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
