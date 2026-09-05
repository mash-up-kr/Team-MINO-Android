package team.mino.feature.room.detail.screen

import android.content.ClipData
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.toClipEntry
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import team.mino.core.common.ui.architecture.CollectSideEffect
import team.mino.core.common.ui.scaffold.LocalSnackbarHostState
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_OUTCOME
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_ROOM_ID
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_ROOM_ID
import team.mino.core.navigation.activity.launcher.ROOM_FORM_OUTCOME_UPDATED
import team.mino.feature.room.detail.vm.RoomDetailIntent
import team.mino.feature.room.detail.vm.RoomDetailSideEffect
import team.mino.feature.room.detail.vm.RoomDetailViewModel
import team.mino.feature.room.showShareCompleted

/**
 * 방 상세의 유일한 Route — `RoomListRoute`가 `selectedRoomId != null`일 때 [RoomListScreen]의
 * `detailContent` 슬롯으로 직접 호출한다(별도 Navigation 목적지가 아니다, `RoomNavigation.kt` KDoc
 * 참고). [roomId]는 `RoomDetailViewModel.Factory`에 `@AssistedInject`로 직접 넘긴다.
 *
 * [BoxScope] 확장인 이유: 호출부([RoomListScreen])가 지도([RoomListMap])를 담은 같은 `Box` 안에서
 * 이 Route를 그려야 지도가 리스트↔상세 전환에도 살아남는다 — 그 `Box`의 [BoxScope]를 그대로 받아
 * [RoomDetailScreen]의 컨트롤·바텀시트가 `Modifier.align`으로 위치를 잡을 수 있게 한다.
 *
 * @param onCurrentLocationClick 현재 위치 버튼 클릭 — 지도를 실제로 그리는 `RoomListViewModel`에게
 *   전달해야 하므로 호출부(`RoomListRoute`)가 넘겨준다([RoomDetailScreen] KDoc 참고).
 * @param onOpenPlaceDetail 장소 카드·리스트 탭 → [SCR-006] 장소 상세. 인자는 `Pin.id`다. 장소 상세도
 *   방 상세와 같은 목적지 안의 로컬 상태(`selectedPinId`)라 이 Route가 직접 열 수 없다 — 그 상태를 가진
 *   `RoomListViewModel`에게 넘겨야 하므로 `onCurrentLocationClick`과 같은 이유로 호출부가 넘겨준다
 *   (docs/specs/place-detail/contracts/place-detail-entry.md §2).
 * @param onLeaveRoom [SYS-007] 나가기 완료 — 방 상세를 닫고 [message] 토스트("방을 나갔어요"/"방장을
 *   넘기고 나갔어요")를 띄운다. 이 Route는 `onBack()` 호출과 동시에 컴포지션에서 사라지므로, 여기서
 *   `rememberCoroutineScope()`로 토스트를 띄우면 스낵바가 미처 뜨기 전에 스코프가 취소된다 — 컴포지션에
 *   계속 남아있는 `RoomListRoute`가 대신 띄워야 하므로 닫기와 토스트를 함께 호출부에 위임한다.
 */
@Composable
internal fun BoxScope.RoomDetailRoute(
    roomId: String,
    onBack: () -> Unit,
    onCurrentLocationClick: () -> Unit,
    onOpenPlaceDetail: (pinId: String) -> Unit,
    onLeaveRoom: (message: String) -> Unit,
    modifier: Modifier = Modifier,
    // compose-lints `ViewModels` 규칙 — hiltViewModel() 호출은 컴포저블 시그니처에서 명시적으로 드러나야
    // 하므로 본문이 아니라 기본 파라미터 자리에 둔다(`RoomListRoute.rememberDetailSheetLevel`과 같은 이유).
    viewModel: RoomDetailViewModel = hiltViewModel<RoomDetailViewModel, RoomDetailViewModel.Factory>(
        key = roomId,
        creationCallback = { factory -> factory.create(roomId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = checkNotNull(LocalActivity.current) { "RoomDetailRoute는 Activity 컨텍스트 안에서만 그려진다." }
    val snackbarHostState = LocalSnackbarHostState.current
    val clipboard = LocalClipboard.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()

    // [FR-012] 방 편집 — RoomFormLauncher 결과 계약은
    // docs/specs/group-room-form/contracts/room-form-launcher.md §3 참조.
    val editRoomResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val updated = result.data?.getStringExtra(EXTRA_ROOM_FORM_RESULT_OUTCOME) == ROOM_FORM_OUTCOME_UPDATED
        viewModel.processIntent(RoomDetailIntent.OnRoomFormResult(updated))
    }

    // [FR-009] 공유 시트 [+ 새 방 만들기].
    val shareCreateRoomResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val createdRoomId = result.data?.getStringExtra(EXTRA_ROOM_FORM_RESULT_ROOM_ID)
        viewModel.processIntent(RoomDetailIntent.OnShareRoomFormResult(createdRoomId))
    }

    CollectSideEffect(sideEffect = viewModel.sideEffect) { effect ->
        when (effect) {
            RoomDetailSideEffect.NavigateBack -> onBack()

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

            // [FR-009] 공유 완료 — 문구와 3초 노출은 장소 상세와 한 곳에서 나온다
            // (`ShareCompletedToast.kt`). 같은 [SYS-003] 시트라 진입점에 따라 갈리지 않는다.
            RoomDetailSideEffect.ShowShareCompleteToast ->
                scope.launch { snackbarHostState.showShareCompleted(resources) }

            // [SYS-007] 나가기 완료 — 방 상세는 room-list 백스택 위에 쌓인 nested Route라
            // NavigateBack과 같은 popBackStackIfResumed(entry) 메커니즘으로 SCR-004에 복귀한다
            // (research.md D12, T032와 동일한 onBack 콜백을 재사용). 토스트는 이 Route가 사라진 뒤에도
            // 남아있어야 해서 [onLeaveRoom] KDoc대로 호출부(`RoomListRoute`)에 함께 위임한다.
            is RoomDetailSideEffect.LeaveRoomCompleted -> onLeaveRoom(effect.message)

            // [FR-011] "초대하기" — OS 공유 시트. `OnboardingActivity.shareInviteLink`와 같은 패턴
            // (createChooser로 감싸는 이유도 같다 — 기본 앱이 정해져 있어도 매번 시트를 띄운다).
            is RoomDetailSideEffect.ShareInviteLink -> {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, effect.link)
                }
                activity.startActivity(Intent.createChooser(sendIntent, null))
            }

            // [FR-011] "링크 복사하기" — 클립보드에 쓰고 완료를 알린다(`InviteRoute`의 CopyInviteLink와 같은 패턴).
            is RoomDetailSideEffect.CopyInviteLink ->
                scope.launch {
                    clipboard.setClipEntry(ClipData.newPlainText(INVITE_LINK_CLIP_LABEL, effect.link).toClipEntry())
                    snackbarHostState.showSnackbar("클립 보드에 초대링크가 복사되었어요")
                }

            is RoomDetailSideEffect.NavigateToPlaceDetail -> onOpenPlaceDetail(effect.pinId)
        }
    }

    // ON_RESUME마다 다시 보낸다 — 인스타그램 공유 시트 등 외부 앱에 다녀온 뒤 이 화면으로 돌아왔을 때도
    // 방·장소가 새로고침돼야 한다(`RoomDetailViewModel.onScreenEntered` KDoc 참고). 최초 진입도
    // 이 이벤트로 커버된다 — 컴포지션 시점에 이미 RESUMED면 즉시 한 번 발행된다.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.processIntent(RoomDetailIntent.OnScreenEntered)
    }

    // 이 Route가 별도 목적지가 아니라 RoomListRoute의 selectedRoomId로 여닫히는 로컬 상태라
    // (RoomListRoute.kt KDoc 참고), 시스템 뒤로가기가 RoomListRoute의 BackHandler에서 바로 처리돼
    // 위 NavigateBack 경로를 거치지 않고 이 Composable이 사라질 수 있다 — 그 경로까지 덮도록
    // onDispose에서 화면 이탈을 알린다.
    DisposableEffect(Unit) {
        onDispose { viewModel.processIntent(RoomDetailIntent.OnScreenExited) }
    }

    // [state.dismissibleOverlayIntent]가 있는 동안엔 이 BackHandler가 부모(RoomListRoute)의
    // BackHandler보다 먼저 뒤로가기를 가로챈다(#290 QA로 발견 — 예전엔 이 화면 전용 오버레이가 열려
    // 있어도 뒤로가기를 누르면 그 오버레이가 아니라 방 상세 전체가 닫혔다). 어떤 오버레이를 닫을지·
    // 우선순위는 [RoomDetailUiState.dismissibleOverlayIntent] 하나가 판정한다 — 여기서 조건을 다시
    // 나열하지 않아야, 오버레이 종류가 늘어도 그 값 하나만 고치면 된다.
    BackHandler(enabled = state.dismissibleOverlayIntent != null) {
        state.dismissibleOverlayIntent?.let(viewModel::processIntent)
    }

    RoomDetailScreen(
        state = state,
        onIntent = viewModel::processIntent,
        onCurrentLocationClick = onCurrentLocationClick,
        modifier = modifier,
    )
}

/** 클립 항목의 식별자 — 화면에 나오는 문구가 아니라 고정 문자열이다(`InviteRoute.CLIP_LABEL`과 같은 이유). */
private const val INVITE_LINK_CLIP_LABEL = "invite_link"
