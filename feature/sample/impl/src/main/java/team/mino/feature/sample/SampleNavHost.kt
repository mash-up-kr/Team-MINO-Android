package team.mino.feature.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import team.mino.core.analytics.screen.TrackScreenViews
import team.mino.core.navigation.screen.MinoNavHost
import team.mino.core.navigation.screen.popBackStackIfResumed
import team.mino.core.navigation.screen.screen
import team.mino.feature.sample.detail.model.SampleQuery
import team.mino.feature.sample.detail.screen.SampleDetailRoute
import team.mino.feature.sample.main.screen.SampleRoute
import team.mino.feature.sample.map.screen.SampleMapRoute

@Composable
internal fun SampleNavHost(
    onNavigateToHome: () -> Unit,
    onRequestHomeResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    TrackScreenViews(navController)

    MinoNavHost(
        navController = navController,
        startDestination = SampleMain,
        modifier = modifier,
    ) {
        screen<SampleMain> {
            SampleRoute(
                onNavigateToHome = onNavigateToHome,
                onRequestHomeResult = onRequestHomeResult,
                onNavigateToMap = { navController.navigate(SampleMap) },
                onNavigateToDetail = {
                    navController.navigate(SampleDetail(SampleQuery(keyword = "민호", page = 1)))
                },
            )
        }
        screen<SampleMap> {
            SampleMapRoute()
        }
        screen<SampleDetail>(typeMap = SampleDetail.typeMap) { entry ->
            SampleDetailRoute(
                onBack = { navController.popBackStackIfResumed(entry) },
            )
        }
    }
}
