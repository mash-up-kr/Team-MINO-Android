package team.mino.feature.room.main.screen

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import team.mino.core.common.ui.architecture.CollectSideEffect
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_DETAIL_ROOM_ID
import team.mino.feature.room.main.model.BottomSheetLevel
import team.mino.feature.room.main.vm.RoomListIntent
import team.mino.feature.room.main.vm.RoomListSideEffect
import team.mino.feature.room.main.vm.RoomListViewModel

/**
 * `RoomFormLauncher` 결과에서 생성된 방 id를 담는 extra 키.
 *
 * `:feature:roomform`이 아직 없어 결과 계약(extra 키)이 확정 전이다
 * (`docs/specs/room-list/contracts/navigation-launchers.md`) — room-list는 "결과를 받는 호출자"
 * 역할만 못박혀 있으므로, roomform 쪽 계약이 정해질 때까지 쓸 임시 키를 이 파일 안에서만
 * 정의한다. `:core:navigation`의 `ExtraTag.kt`는 roomform이 실제로 존재해야 정식 계약을
 * 확정할 수 있어 지금 손대지 않는다.
 */
private const val EXTRA_ROOM_FORM_CREATED_ROOM_ID = "room_form_created_room_id"

/**
 * 방 리스트 탭 그래프의 진입 Route — 유일한 화면.
 *
 * @param sheetLevelOverride 시작 인자. `null`이면 FR-001 기본값(HALF), 값이 있으면 EC-007(방 상세 [X] 복귀) 케이스다.
 */
@Composable
internal fun RoomListRoute(
    sheetLevelOverride: BottomSheetLevel?,
    modifier: Modifier = Modifier,
    viewModel: RoomListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity

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
        val createdRoomId = result.data?.getStringExtra(EXTRA_ROOM_FORM_CREATED_ROOM_ID)
        viewModel.processIntent(RoomListIntent.OnRoomFormResult(createdRoomId))
    }

    CollectSideEffect(sideEffect = viewModel.sideEffect) { effect ->
        when (effect) {
            RoomListSideEffect.RequestLocationPermission ->
                locationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                )
            is RoomListSideEffect.NavigateToRoomDetail ->
                viewModel.roomDetailLauncher.launch(activity) {
                    putExtra(EXTRA_ROOM_DETAIL_ROOM_ID, effect.roomId)
                }

            RoomListSideEffect.NavigateToRoomForm ->
                viewModel.roomFormLauncher.launch(activity, resultLauncher = roomFormResultLauncher)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.resolveInitialSheetLevel(sheetLevelOverride)
        viewModel.processIntent(RoomListIntent.OnScreenEntered)
    }

    RoomListScreen(
        state = state,
        onIntent = viewModel::processIntent,
        modifier = modifier,
    )
}
