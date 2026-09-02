package team.mino.feature.splash

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import team.mino.core.analytics.screen.TrackScreenViews
import team.mino.core.common.ui.scaffold.MinoScaffold

/**
 * `:feature:splash`의 진입 컴포저블. Activity가 여는 것은 이 셸이다.
 *
 * 화면이 하나뿐이고 내부 전환·진입 인자가 없어도 `navController`와 그래프를 둔다 — 화면 조회
 * 로깅이 거기 딸려 온다(feature-module.md 4장). 시작 목적지는 하나뿐이라 Activity에서 받지 않고
 * 그래프가 갖는다.
 *
 * **셸이 받은 인셋 패딩을 화면에 걸지 않는다.** 디자인은 상태바를 화면 안에 겹쳐 그린 채로 각
 * 요소의 자리를 재므로, 위아래 모두 기준이 시스템 바가 아니라 화면 끝이다. 인셋을 여기서
 * 소비하면 아래에 배경색 띠가 남아 배경 아트가 바닥에 닿지 못하고, 위 요소는 상태바 높이만큼
 * 통째로 밀린다. 그래서 화면은 전체 영역에 그리고,
 * [team.mino.feature.splash.main.screen.SplashScreen]도 인셋을 소비하지 않는다.
 */
@Composable
internal fun SplashShell(
    onNavigateToMain: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    TrackScreenViews(navController)

    MinoScaffold(modifier = modifier) { _ ->
        SplashNavHost(
            navController = navController,
            onNavigateToMain = onNavigateToMain,
            onNavigateToOnboarding = onNavigateToOnboarding,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
