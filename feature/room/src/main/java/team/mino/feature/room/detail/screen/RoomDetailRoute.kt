package team.mino.feature.room.detail.screen

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import team.mino.core.common.ui.architecture.CollectSideEffect
import team.mino.core.common.ui.scaffold.LocalSnackbarHostState
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_OUTCOME
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_ROOM_ID
import team.mino.core.navigation.activity.launcher.ROOM_FORM_OUTCOME_UPDATED
import team.mino.feature.room.detail.vm.RoomDetailIntent
import team.mino.feature.room.detail.vm.RoomDetailSideEffect
import team.mino.feature.room.detail.vm.RoomDetailViewModel

/**
 * 방 상세의 유일한 Route — `RoomListRoute`가 `selectedRoomId != null`일 때 [RoomListScreen]의
 * `detailContent` 슬롯으로 직접 호출한다(별도 Navigation 목적지가 아니다, `RoomNavigation.kt` KDoc
 * 참고). [roomId]는 `RoomDetailViewModel.Factory`에 `@AssistedInject`로 직접 넘긴다.
 *
 * [BoxScope] 확장인 이유: 호출부([RoomListScreen])가 지도([RoomListMap])를 담은 같은 `Box` 안에서
 * 이 Route를 그려야 지도가 리스트↔상세 전환에도 살아남는다 — 그 `Box`의 [BoxScope]를 그대로 받아
 * [RoomDetailScreen]의 컨트롤·바텀시트가 `Modifier.align`으로 위치를 잡을 수 있게 한다.
 */
@Composable
internal fun BoxScope.RoomDetailRoute(
    roomId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: RoomDetailViewModel = hiltViewModel<RoomDetailViewModel, RoomDetailViewModel.Factory>(
        key = roomId,
        creationCallback = { factory -> factory.create(roomId) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    // [FR-012] 방 편집 — RoomFormLauncher 결과 계약은
    // docs/specs/group-room-form/contracts/room-form-launcher.md §3 참조.
    val editRoomResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val updated = result.data?.getStringExtra(EXTRA_ROOM_FORM_RESULT_OUTCOME) == ROOM_FORM_OUTCOME_UPDATED
        viewModel.processIntent(RoomDetailIntent.OnRoomFormResult(updated))
    }

    // [FR-009] 공유 시트 [+ 새 방 만들기] — RoomListRoute의 EXTRA_ROOM_FORM_CREATED_ROOM_ID와 같은 이유로
    // 이 파일 안에서만 정의하는 임시 키다(roomform 결과 계약이 아직 확정 전).
    val shareCreateRoomResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val createdRoomId = result.data?.getStringExtra(EXTRA_ROOM_FORM_CREATED_ROOM_ID)
        viewModel.processIntent(RoomDetailIntent.OnShareRoomFormResult(createdRoomId))
    }

    // [research.md D10] room-list와 같은 현재 위치 권한 요청 플로우.
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.processIntent(RoomDetailIntent.OnLocationPermissionResult(granted))
    }

    CollectSideEffect(sideEffect = viewModel.sideEffect) { effect ->
        when (effect) {
            RoomDetailSideEffect.NavigateBack -> onBack()

            RoomDetailSideEffect.RequestLocationPermission ->
                locationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                )

            RoomDetailSideEffect.NavigateToRoomForm -> {
                val roomId = state.room?.id
                viewModel.roomFormLauncher.launch(activity, resultLauncher = editRoomResultLauncher) {
                    roomId?.let { putExtra(EXTRA_ROOM_FORM_ROOM_ID, it) } ?: this
                }
            }

            RoomDetailSideEffect.NavigateToCreateRoomForm ->
                viewModel.roomFormLauncher.launch(activity, resultLauncher = shareCreateRoomResultLauncher)

            RoomDetailSideEffect.ShowEditCompleteSnackbar ->
                scope.launch { snackbarHostState.showSnackbar("방 편집이 완료되었어요") }

            // [FR-009] 공유 완료 — 3초 노출(UX-002). SnackbarDuration.Short는 4초라 duration을
            // 직접 지정하지 않고, 3초 뒤 스스로 dismiss하는 launch로 표현한다.
            RoomDetailSideEffect.ShowShareCompleteToast ->
                scope.launch {
                    // Figma `2542-125820`("004-2-3 방 상세 half_공유 피드백 토스트 표출") 실측 문구.
                    val job = launch { snackbarHostState.showSnackbar("공유가 완료됐습니다.") }
                    delay(SHARE_COMPLETE_TOAST_DURATION_MS)
                    job.cancel()
                }

            // [SYS-007] 나가기 완료 — 방 상세는 room-list 백스택 위에 쌓인 nested Route라
            // NavigateBack과 같은 popBackStackIfResumed(entry) 메커니즘으로 SCR-004에 복귀한다
            // (research.md D12, T032와 동일한 onBack 콜백을 재사용).
            RoomDetailSideEffect.NavigateToRoomList -> onBack()

            // 나머지 SideEffect는 각 사용자 스토리 구현 태스크에서 연결한다.
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        viewModel.processIntent(RoomDetailIntent.OnScreenEntered)
    }

    // 이 Route가 별도 목적지가 아니라 RoomListRoute의 selectedRoomId로 여닫히는 로컬 상태라
    // (RoomListRoute.kt KDoc 참고), 시스템 뒤로가기가 RoomListRoute의 BackHandler에서 바로 처리돼
    // 위 NavigateBack 경로를 거치지 않고 이 Composable이 사라질 수 있다 — 그 경로까지 덮도록
    // onDispose에서 화면 이탈을 알린다.
    DisposableEffect(Unit) {
        onDispose { viewModel.processIntent(RoomDetailIntent.OnScreenExited) }
    }

    RoomDetailScreen(
        state = state,
        onIntent = viewModel::processIntent,
        modifier = modifier,
    )
}

/** [UX-002] 공유 완료 토스트 노출 시간. */
private const val SHARE_COMPLETE_TOAST_DURATION_MS = 3000L

/** `RoomListRoute.EXTRA_ROOM_FORM_CREATED_ROOM_ID`와 같은 이유·같은 값의 임시 키. */
private const val EXTRA_ROOM_FORM_CREATED_ROOM_ID = "room_form_created_room_id"
