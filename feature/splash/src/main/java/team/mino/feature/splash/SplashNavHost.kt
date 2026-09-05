package team.mino.feature.splash

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import team.mino.core.navigation.screen.MinoNavHost
import team.mino.core.navigation.screen.screen
import team.mino.feature.splash.main.screen.SplashRoute

/**
 * 화면이 하나이고 내부 전환도 진입 인자도 없지만 그래프는 둔다 — 화면 조회 로깅이
 * `navController`의 백스택 엔트리에 붙어 있어, 그래프가 없으면 [SplashMain] 조회가 기록되지
 * 않는다(feature-module.md 4장 · core/analytics README §7).
 */
@Composable
internal fun SplashNavHost(
    navController: NavHostController,
    inviteCode: String?,
    onNavigateToMain: () -> Unit,
    onNavigateToInvitedRoom: (String) -> Unit,
    onNavigateToOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MinoNavHost(
        navController = navController,
        startDestination = SplashMain,
        modifier = modifier,
    ) {
        screen<SplashMain> {
            SplashRoute(
                inviteCode = inviteCode,
                onNavigateToMain = onNavigateToMain,
                onNavigateToInvitedRoom = onNavigateToInvitedRoom,
                onNavigateToOnboarding = onNavigateToOnboarding,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
