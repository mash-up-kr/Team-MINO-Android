package team.mino.feature.sample

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import team.mino.core.analytics.screen.TrackScreenViews
import team.mino.core.common.ui.scaffold.MinoScaffold
import team.mino.core.navigation.screen.Route

@Composable
internal fun SampleShell(
    startDestination: Route,
    onReturnResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    TrackScreenViews(navController)

    MinoScaffold(modifier = modifier) { innerPadding ->
        SampleNavHost(
            navController = navController,
            startDestination = startDestination,
            onReturnResult = onReturnResult,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
