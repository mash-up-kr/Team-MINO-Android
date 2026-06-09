package team.mino.feature.sample.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.MinoNavHost
import team.mino.core.navigation.screen.Route
import team.mino.core.navigation.screen.screen
import team.mino.feature.sample.screen.SampleRoute

@Serializable
internal data object SampleMain : Route

@Composable
internal fun SampleNavHost(
    onNavigateToHome: () -> Unit,
    onRequestHomeResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    MinoNavHost(
        navController = navController,
        startDestination = SampleMain,
        modifier = modifier,
    ) {
        screen<SampleMain> {
            SampleRoute(
                onNavigateToHome = onNavigateToHome,
                onRequestHomeResult = onRequestHomeResult,
            )
        }
    }
}
