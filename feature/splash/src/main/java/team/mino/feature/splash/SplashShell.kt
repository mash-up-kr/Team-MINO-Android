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
 * **셸이 받은 인셋 패딩을 화면에 걸지 않는다.** 디자인이 위아래 인셋을 다르게 다루기 때문이다 —
 * 상태바는 화면 위에 겹쳐 그려져 위 여백의 기준이 화면 최상단이고, 마스코트는 내비게이션 바
 * 위에서 잘린다. 여기서 한꺼번에 소비하면 위 요소가 상태바 높이만큼 밀린다. 그래서 화면은 전체
 * 영역에 그리고, 어느 쪽을 소비할지는
 * [team.mino.feature.splash.main.screen.SplashScreen]이 정한다.
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
