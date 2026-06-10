package team.mino.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import team.mino.core.navigation.screen.MinoNavHost
import team.mino.core.navigation.screen.screen
import team.mino.feature.home.main.screen.HomeScreen

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
