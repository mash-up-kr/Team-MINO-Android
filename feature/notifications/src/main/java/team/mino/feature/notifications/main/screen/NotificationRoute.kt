package team.mino.feature.notifications.main.screen

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import team.mino.core.common.ui.architecture.CollectSideEffect
import team.mino.core.common.ui.error.CollectDomainError
import team.mino.core.common.ui.scaffold.LocalSnackbarHostState
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.notifications.R
import team.mino.feature.notifications.main.vm.NotificationIntent
import team.mino.feature.notifications.main.vm.NotificationSideEffect
import team.mino.feature.notifications.main.vm.NotificationViewModel

/**
 * [NotificationScreen]의 연결부. ViewModel을 얻어 상태를 구독하고 의도를 넘긴다.
 *
 * **첫 조회를 여기서 건다.** 화면에 들어온 것이 곧 [NotificationIntent.Load]이며(계약 §2), 탭을 떠났다
 * 돌아와 이 컴포저블이 다시 서더라도 ViewModel이 두 번째 [NotificationIntent.Load]를 버려 목록이 처음부터
 * 다시 그려지지 않는다(spec FR-015) — 그래서 여기에 별도의 가드를 두지 않는다.
 *
 * **목록 조회 실패는 두 곳으로 나뉜다.** 화면을 덮는 오류 얼굴은 `NotificationUiState.phase`가 들고, 무엇
 * 때문에 못 받았는지는 `DomainErrorEmitter`로 와 여기서 문구가 된다(계약 §4.1,
 * `docs/conventions/error_handling.md` §5). 상태가 예외 리프를 담지 않으므로 이 수집이 없으면 실패의 종류가
 * 사용자에게 닿지 않는다.
 *
 * **추가 로드 실패는 이 통로로 오지 않는다.** 이미 그린 목록을 남긴 채 목록 끝에서만 알리므로
 * `NotificationUiState.appendError` 한 자리로 끝난다(spec UX-012·EC-016).
 *
 * **같은 그래프 안의 전환은 여기서 끝내지 않는다.** 저장 오류 안내로 가는 신호는 `NavController`를 쥔
 * `notificationGraph`가 준 [onNavigateToSaveErrorGuide]로 올린다 — 화면이 `NavController`를 들면 어느
 * 그래프에 놓였는지까지 알게 된다(`docs/architecture/feature-navigation.md` 3장).
 *
 * **대상이 아직 있는지 되묻지 않는다**(계약 §4.1). 이미 지워진 장소·나간 방이어도 그대로 보내고, 그 사실은
 * 도착한 화면이 자기 규칙으로 알린다(spec UX-006·EC-009·EC-010).
 */
@Composable
internal fun NotificationRoute(
    onNavigateToPlaceDetail: (pinId: String) -> Unit,
    onNavigateToRoomDetail: (roomId: String) -> Unit,
    onNavigateToSaveErrorGuide: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current

    LaunchedEffect(Unit) { viewModel.processIntent(NotificationIntent.Load) }

    CollectSideEffect(viewModel.sideEffect) { effect ->
        when (effect) {
            // 밖으로 나가는 둘은 `notificationGraph`가 받은 콜백으로 그대로 올린다 — 어느 홀더에 실어 어느
            // 탭으로 옮길지는 셸이 정한다(계약 §1, `research.md` D10·D14).
            is NotificationSideEffect.NavigateToPlaceDetail -> onNavigateToPlaceDetail(effect.pinId)
            is NotificationSideEffect.NavigateToRoomDetail -> onNavigateToRoomDetail(effect.roomId)
            // 저장 오류 안내는 같은 그래프 안의 목적지라 `notificationGraph`가 `NavController`로 옮긴다
            // (계약 §1·§3). `else`를 두지 않아 갈래가 늘면 컴파일이 멈추고 여기서 다시 판단하게 된다.
            NotificationSideEffect.NavigateToSaveErrorGuide -> onNavigateToSaveErrorGuide()
            // 되돌림은 안내 화면이 자기 자리에서 팝한다 — 시스템 뒤로가기와 같은 경로여야 하기 때문이다
            // (계약 §4.4, spec EC-014). 이 신호는 목록이 다시 설 때까지 채널에 머무니, 도착했을 때는 이미
            // 되돌아온 뒤다. 여기서 또 팝하면 알림 탭 자체가 백스택에서 빠진다.
            NotificationSideEffect.NavigateBack -> Unit
        }
    }

    // 수집기가 받는 것은 ViewModel이 아니라 에러 방출자다. 그 자리에 ViewModel을 그대로 놓으면 하위로
    // ViewModel을 흘려보내는 것과 구분되지 않으므로 넘길 능력만 남겨 타입을 좁힌다.
    val errorEmitter: DomainErrorEmitter = viewModel
    CollectDomainError(errorEmitter) { error ->
        scope.launch { snackbarHostState.showSnackbar(resources.getString(loadErrorMessageResOf(error))) }
    }

    NotificationScreen(
        state = state,
        onIntent = viewModel::processIntent,
        modifier = modifier,
    )
}

/**
 * 리프별 사용자 문구. 공통 매퍼를 두지 않고 화면마다 자기 문맥에 맞춰 적는다
 * (`docs/conventions/error_handling.md` §8).
 *
 * 연결 자체가 안 된 것만 갈라 낸다 — 그때는 사용자가 할 수 있는 일이 있고, 나머지는 재시도 말고 없어
 * 오류 화면에 이미 서 있는 안내와 같은 말이 된다.
 */
@StringRes
private fun loadErrorMessageResOf(error: MinoDomainException): Int =
    when (error) {
        is MinoDomainException.Network -> R.string.notification_error_network
        is MinoDomainException.Http, is MinoDomainException.Auth -> R.string.notification_error_load_failed
    }
