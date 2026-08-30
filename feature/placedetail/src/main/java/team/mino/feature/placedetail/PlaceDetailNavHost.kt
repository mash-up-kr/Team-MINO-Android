package team.mino.feature.placedetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import team.mino.core.navigation.screen.MinoNavHost
import team.mino.core.navigation.screen.Route
import team.mino.core.navigation.screen.screen
import team.mino.feature.placedetail.main.screen.PlaceDetailRoute

/**
 * `:feature:placedetail`의 화면 그래프. 목적지는 [PlaceDetailMain] 한 장뿐이지만, 진입 인자 복원
 * (`toRoute<PlaceDetailMain>`)과 화면 조회 로깅이 NavHost에 딸려 오므로 그래프를 둔다
 * (`docs/architecture/feature-module.md` 4장).
 *
 * 내부 전환이 없어 `navController`로 `navigate`하는 자리가 없다. 화면 밖으로 나가는 셋은 모두 feature
 * **간** 전환이거나 화면 종료라 그래프가 처리하지 않고 셸을 거쳐 Activity로 올라간다
 * (`docs/architecture/feature-navigation.md` 1장).
 *
 * [PlaceDetailMain]의 인자는 `String` 하나라 `typeMap`이 필요 없다.
 */
@Composable
internal fun PlaceDetailNavHost(
    navController: NavHostController,
    startDestination: Route,
    onExit: () -> Unit,
    onOpenExternalMap: (mapUrl: String?, query: String) -> Unit,
    onOpenSourceLink: (url: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    MinoNavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        screen<PlaceDetailMain> {
            PlaceDetailRoute(
                onExit = onExit,
                onOpenExternalMap = onOpenExternalMap,
                onOpenSourceLink = onOpenSourceLink,
            )
        }
    }
}
