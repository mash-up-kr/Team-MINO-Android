package team.mino.feature.mypage.profile.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import team.mino.core.common.ui.architecture.CollectSideEffect
import team.mino.core.common.ui.error.CollectDomainError
import team.mino.core.common.ui.scaffold.LocalSnackbarHostState
import team.mino.feature.mypage.R
import team.mino.feature.mypage.profile.vm.ProfileSideEffect
import team.mino.feature.mypage.profile.vm.ProfileViewModel

// CollectDomainError는 DomainErrorEmitter를 위임한 ViewModel을 그대로 받는 것이 의도된 사용법이다
// (core/common/ui/README.md §"에러 소비") — ComposeViewModelForwarding이 잡는 일반적 안티패턴과 다르다.
@Suppress("ComposeViewModelForwarding")
@Composable
internal fun ProfileRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CollectSideEffect(sideEffect = viewModel.sideEffect) { effect ->
        when (effect) {
            ProfileSideEffect.NavigateBack -> onBack()
        }
    }

    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val errorMessage = stringResource(R.string.mypage_error_domain)
    CollectDomainError(emitter = viewModel) {
        scope.launch { snackbarHostState.showSnackbar(errorMessage) }
    }

    ProfileScreen(
        state = state,
        onIntent = viewModel::processIntent,
        onBack = onBack,
        modifier = modifier,
    )
}
