package team.mino.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import team.mino.core.navigation.screen.MinoNavHost
import team.mino.core.navigation.screen.Route
import team.mino.core.navigation.screen.screen
import team.mino.feature.home.main.screen.HomeRoute

@Composable
internal fun HomeNavHost(
    navController: NavHostController,
    startDestination: Route,
    onReturnResult: () -> Unit,
    onNavigateToSample: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MinoNavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        screen<HomeMain> {
            HomeRoute(
                onReturnResult = onReturnResult,
                onNavigateToSample = onNavigateToSample,
            )
        }
    }
}
