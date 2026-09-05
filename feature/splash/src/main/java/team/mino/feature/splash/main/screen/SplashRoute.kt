package team.mino.feature.splash.main.screen

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
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
 * 오류 안내는 일회성 신호로 오므로 통로 둘로 받는다 — 도메인 예외는 [CollectDomainError], 시간
 * 임계로 대기를 접은 것은 [SplashSideEffect.EntryTimedOut]이다(에러 처리 규약 §5). 어느 쪽이든
 * 지금 띄울 문구 하나로 합쳐 화면에 내려보내고, 표출 시간이 지나면 여기서 내린다.
 *
 * **`SnackbarHostState`를 쓰지 않는다.** 셸이 내려준
 * [team.mino.core.common.ui.scaffold.LocalSnackbarHostState]는 Scaffold 슬롯에 M3 기본 모습으로
 * 그려져 브랜드 레이어 위 정해진 자리에 놓을 수 없고(UX-003), 그렇다고 상태만 따로 들면 표출
 * 시간을 재는 주체가 `SnackbarHost` 컴포저블이라 안내가 화면에 영구히 남는다. 첫 안내가 해제되지
 * 않으면 뒤이은 `showSnackbar`도 호스트의 뮤텍스에 갇혀 반복 표출(UX-006)까지 함께 무너진다.
 * 액션도 닫기 버튼도 없는 안내라(UX-001) 호스트가 대신 줄 것이 표출 시간뿐이므로 그 하나만 둔다.
 */
@Composable
internal fun SplashRoute(
    inviteCode: String?,
    onNavigateToMain: () -> Unit,
    onNavigateToInvitedRoom: (String) -> Unit,
    onNavigateToOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // 진입 신호는 화면이 보이는 순간 한 번. 중복 진입 방어는 ViewModel이 갖는다.
    LaunchedEffect(viewModel) {
        viewModel.processIntent(SplashIntent.Start(inviteCode))
    }

    CollectSideEffect(viewModel.sideEffect) { effect ->
        when (effect) {
            is SplashSideEffect.NavigateTo ->
                when (val entry = effect.entry) {
                    SplashEntry.Main -> onNavigateToMain()
                    is SplashEntry.InvitedRoom -> onNavigateToInvitedRoom(entry.roomId)
                    SplashEntry.Onboarding -> onNavigateToOnboarding()
                }

            SplashSideEffect.EntryTimedOut ->
                toastMessage = resources.getString(R.string.splash_error_temporary)
        }
    }

    // 에러 수집기가 받는 것은 ViewModel이 아니라 에러 방출자다. 그 자리에 ViewModel을 그대로 놓으면
    // 하위 Composable로 ViewModel을 흘려보내는 것과 구분되지 않으므로 넘길 능력만 남겨 타입을 좁힌다.
    val errorEmitter: DomainErrorEmitter = viewModel
    CollectDomainError(errorEmitter) { error ->
        toastMessage = resources.getString(messageResOf(error))
    }

    // 표출 시간이 지나면 스스로 내린다. 다음 안내는 문구가 같아도 사이에 null을 거쳐 오므로
    // (ViewModel이 10초를 비운다) 이 효과가 다시 시작된다.
    val accessibilityManager = LocalAccessibilityManager.current
    LaunchedEffect(toastMessage) {
        if (toastMessage == null) return@LaunchedEffect

        val duration = accessibilityManager?.calculateRecommendedTimeoutMillis(
            originalTimeoutMillis = TOAST_DURATION_MILLIS,
            containsIcons = true,
            containsText = true,
        ) ?: TOAST_DURATION_MILLIS

        delay(duration)
        toastMessage = null
    }

    SplashScreen(
        isSpinnerVisible = uiState.isSpinnerVisible,
        modifier = modifier,
        toastMessage = toastMessage,
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

/** M3 `SnackbarDuration.Short`와 같은 값. 억제 간격(10초)보다 짧아 다음 안내를 막지 않는다. */
private const val TOAST_DURATION_MILLIS = 4_000L
