package team.mino.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import team.mino.core.navigation.screen.MinoNavHost
import team.mino.core.navigation.screen.screen
import team.mino.feature.onboarding.invite.screen.InviteRoute
import team.mino.feature.onboarding.relay.screen.OnboardingRelayScreen
import team.mino.feature.onboarding.tutorial.screen.TutorialRoute

/**
 * `:feature:onboarding`의 화면 그래프.
 *
 * 시작 목적지는 [OnboardingRelay]로 고정이다 — 첫 스텝이 다른 Activity로 위임되므로 온보딩이
 * 열리는 시점에 이 그래프가 그릴 것은 위임 화면뿐이고, 재개 진입도 여기서 목적지로 이동한다.
 * [OnboardingInvite]의 인자는 `String`이라 `typeMap`이 필요 없다.
 *
 * **그래프 안에 `navigate` 호출이 없다.** 스텝 전이는 전부 플로우 ViewModel이 정하고 셸이
 * 실행하므로, 화면이 올리는 것은 조작뿐이다([onInviteClosed]·[onTutorialFinished] ·
 * `contracts/onboarding-flow-ui.md` §2.3·§2.4).
 *
 * [onShareInviteLink]도 같은 이유로 여기서 처리하지 않고 지나가기만 한다 — 외부 앱 전환이라
 * 시작 지점이 Activity다(`feature-navigation.md` 1장).
 */
@Composable
internal fun OnboardingNavHost(
    navController: NavHostController,
    onShareInviteLink: (String) -> Unit,
    onInviteClosed: () -> Unit,
    onTutorialFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MinoNavHost(
        navController = navController,
        startDestination = OnboardingRelay,
        modifier = modifier,
    ) {
        screen<OnboardingRelay> {
            OnboardingRelayScreen()
        }
        // roomId는 화면으로 드릴링하지 않는다 — InviteViewModel이 savedStateHandle로 복원한다
        // (feature-navigation.md 2장 · contracts/onboarding-flow-ui.md §3.2).
        screen<OnboardingInvite> {
            InviteRoute(
                onShareInviteLink = onShareInviteLink,
                onClose = onInviteClosed,
            )
        }
        screen<OnboardingTutorial> {
            TutorialRoute(onFinish = onTutorialFinished)
        }
    }
}
