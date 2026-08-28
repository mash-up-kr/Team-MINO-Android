package team.mino.feature.splash.main.screen

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import team.mino.core.common.ui.architecture.CollectSideEffect
import team.mino.core.common.ui.error.CollectDomainError
import team.mino.core.domain.model.SplashEntry
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.splash.R
import team.mino.feature.splash.main.vm.SplashIntent
import team.mino.feature.splash.main.vm.SplashSideEffect
import team.mino.feature.splash.main.vm.SplashViewModel

/**
 * 스플래시 화면의 연결부.
 *
 * 전환 신호만 콜백으로 올려보내고 `Launcher`를 직접 부르지 않는다 — Activity 전환은 Activity가
 * 시작한다(feature-navigation.md 1장). 어떤 사용자 조작도 받지 않아 Screen으로 내려보낼 콜백이 없다.
 *
 * 오류 안내는 일회성 신호로 오므로 상태가 아니라 통로 둘로 받는다 — 도메인 예외는
 * [CollectDomainError], 시간 임계로 대기를 접은 것은 [SplashSideEffect.EntryTimedOut]이다
 * (에러 처리 규약 §5). 어느 쪽이든 표출 시간은 [SnackbarHostState]에 맡기고, 지금 떠 있는 문구만
 * 화면으로 내려보낸다 — 셸이 내려준 [team.mino.core.common.ui.scaffold.LocalSnackbarHostState]를
 * 쓰지 않는 것은 그 호스트가 Scaffold 슬롯에 M3 기본 모습으로 그려지는 반면 이 화면의 안내는
 * 브랜드 레이어 위 정해진 자리에 놓여야 하기 때문이다(UX-003).
 */
@Composable
internal fun SplashRoute(
    onNavigateToMain: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current

    // 진입 신호는 화면이 보이는 순간 한 번. 중복 진입 방어는 ViewModel이 갖는다.
    LaunchedEffect(viewModel) {
        viewModel.processIntent(SplashIntent.Start)
    }

    CollectSideEffect(viewModel.sideEffect) { effect ->
        when (effect) {
            is SplashSideEffect.NavigateTo ->
                when (effect.entry) {
                    SplashEntry.Main -> onNavigateToMain()
                    SplashEntry.Onboarding -> onNavigateToOnboarding()
                }

            SplashSideEffect.EntryTimedOut ->
                scope.launch {
                    snackbarHostState.showSnackbar(resources.getString(R.string.splash_error_temporary))
                }
        }
    }

    // 에러 수집기가 받는 것은 ViewModel이 아니라 에러 방출자다. 그 자리에 ViewModel을 그대로 놓으면
    // 하위 Composable로 ViewModel을 흘려보내는 것과 구분되지 않으므로 넘길 능력만 남겨 타입을 좁힌다.
    val errorEmitter: DomainErrorEmitter = viewModel
    CollectDomainError(errorEmitter) { error ->
        scope.launch { snackbarHostState.showSnackbar(resources.getString(messageResOf(error))) }
    }

    SplashScreen(
        isSpinnerVisible = uiState.isSpinnerVisible,
        modifier = modifier,
        toastMessage = snackbarHostState.currentSnackbarData?.visuals?.message,
    )
}

/**
 * 리프별 사용자 문구. 공통 매퍼를 두지 않고 화면마다 자기 문맥에 맞춰 적는다
 * (에러 처리 규약 §8 — 리프별 문구 정책이 미정이다).
 *
 * 연결 자체가 안 된 것과 그 밖의 사유는 사용자가 취할 행동이 달라 문구를 가른다(FR-008·FR-009).
 * 세션 발급 실패(`Auth`)도 서버 조회 실패(`Http`)도 사용자가 할 수 있는 일은 같으므로 한 갈래다.
 */
@StringRes
private fun messageResOf(error: MinoDomainException): Int =
    when (error) {
        is MinoDomainException.Network -> R.string.splash_error_network
        is MinoDomainException.Http, is MinoDomainException.Auth -> R.string.splash_error_temporary
    }
