package team.mino.feature.home

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import team.mino.core.analytics.screen.TrackScreenViews
import team.mino.core.common.ui.scaffold.MinoScaffold
import team.mino.core.navigation.screen.Route

@Composable
internal fun HomeShell(
    startDestination: Route,
    onReturnResult: () -> Unit,
    onNavigateToSample: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    TrackScreenViews(navController)

    MinoScaffold(modifier = modifier) { innerPadding ->
        HomeNavHost(
            navController = navController,
            startDestination = startDestination,
            onReturnResult = onReturnResult,
            onNavigateToSample = onNavigateToSample,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
