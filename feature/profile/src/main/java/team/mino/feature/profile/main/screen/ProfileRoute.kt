package team.mino.feature.profile.main.screen

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
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
import team.mino.feature.profile.R
import team.mino.feature.profile.main.vm.ProfileSideEffect
import team.mino.feature.profile.main.vm.ProfileViewModel

/**
 * [ProfileScreen]의 연결부. ViewModel을 얻어 상태를 구독하고 인텐트를 넘긴다.
 *
 * 다음 목적지는 이 화면이 정하지 않는다 — 저장이 끝났다는 신호와 뒤로가기를 콜백으로 올려보내고,
 * 어디로 갈지는 받는 쪽이 결정한다.
 *
 * 저장 실패는 상태가 아니라 ViewModel 인스턴스별 채널로 오므로 셸이 아니라 여기서 수집해
 * 셸이 내려준 스낵바 호스트에 띄운다(에러 처리 규약 §5·§6).
 */
@Composable
internal fun ProfileRoute(
    onBackClick: () -> Unit,
    onSaveCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current

    // 뒤로 갈 수 없는 진입점에서는 시스템 뒤로가기도 삼킨다. 갈 수 있는 진입점에서는 아무것도
    // 하지 않고 기본 동작에 맡겨 Activity가 저장 없이 닫히게 둔다.
    BackHandler(enabled = !state.isBackEnabled) {}

    CollectSideEffect(viewModel.sideEffect) { effect ->
        when (effect) {
            ProfileSideEffect.SaveCompleted -> onSaveCompleted()
        }
    }

    val errorEmitter: DomainErrorEmitter = viewModel
    CollectDomainError(errorEmitter) { error ->
        scope.launch { snackbarHostState.showSnackbar(resources.getString(messageResOf(error))) }
    }

    ProfileScreen(
        state = state,
        onIntent = viewModel::processIntent,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

/**
 * 리프별 사용자 문구. 공통 매퍼를 두지 않고 화면마다 자기 문맥에 맞춰 적는다
 * (에러 처리 규약 §8 — 리프별 문구 정책이 미정이다).
 */
@StringRes
private fun messageResOf(error: MinoDomainException): Int =
    when (error) {
        is MinoDomainException.Network -> R.string.profile_error_network
        is MinoDomainException.Http -> R.string.profile_error_save_failed
        is MinoDomainException.Auth -> R.string.profile_error_auth_expired
    }
