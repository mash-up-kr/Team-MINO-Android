package team.mino.feature.placedetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import team.mino.core.analytics.screen.TrackScreenViews
import team.mino.core.common.ui.scaffold.MinoScaffold
import team.mino.core.navigation.screen.Route

/**
 * `:feature:placedetail`의 진입 컴포저블. Activity가 여는 것은 이 셸이다.
 *
 * 진입 인자(`pinId`)는 시작 라우트([PlaceDetailMain])에 실려 들어오므로 셸은 [startDestination]을 그대로
 * 흘려보낸다. 화면 밖으로 나가는 콜백 셋도 해석하지 않고 그대로 올려보낸다 — 그것을 실행하는 것은
 * `PlaceDetailActivity`다(`docs/architecture/feature-navigation.md` 1장).
 *
 * **셸이 받은 인셋 패딩을 화면에 걸지 않는다.** 이 화면은 지도가 시스템 바 뒤까지 꽉 차야 하고, 그 위에 얹히는
 * 시트는 화면 바닥에 붙은 채 자기 윗변만 상태바 아래로 맞춘다(`PlaceDetailScreen`). 인셋을 여기서 소비하면
 * 지도 위아래에 배경색 띠가 남고 시트 앵커(369dp)도 시스템 바 높이만큼 어긋난다. 시스템 바를 피해야 하는
 * 요소는 화면이 직접 인셋을 소비한다 — `SplashShell`과 같은 이유다.
 *
 * 스낵바 호스트와 미처리 예외 안내는 [MinoScaffold]가 소유하므로 이 셸이 따로 배선하지 않는다
 * (`docs/conventions/error_handling.md` §6).
 */
@Composable
internal fun PlaceDetailShell(
    startDestination: Route,
    onExit: () -> Unit,
    onOpenExternalMap: (mapUrl: String?, query: String) -> Unit,
    onOpenSourceLink: (url: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    TrackScreenViews(navController)

    MinoScaffold(modifier = modifier) { _ ->
        PlaceDetailNavHost(
            navController = navController,
            startDestination = startDestination,
            onExit = onExit,
            onOpenExternalMap = onOpenExternalMap,
            onOpenSourceLink = onOpenSourceLink,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
