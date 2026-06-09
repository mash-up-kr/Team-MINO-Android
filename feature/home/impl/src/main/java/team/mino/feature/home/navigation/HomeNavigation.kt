package team.mino.feature.home.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.MinoNavHost
import team.mino.core.navigation.screen.Route
import team.mino.core.navigation.screen.screen
import team.mino.feature.home.screen.HomeScreen

@Serializable
internal data object HomeMain : Route

@Composable
internal fun HomeNavHost(
    greeting: String,
    onReturnResult: () -> Unit,
    onNavigateToSample: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    MinoNavHost(
        navController = navController,
        startDestination = HomeMain,
        modifier = modifier,
    ) {
        screen<HomeMain> {
            HomeScreen(
                greeting = greeting,
                onReturnResult = onReturnResult,
                onNavigateToSample = onNavigateToSample,
            )
        }
    }
}
