package team.mino.feature.sample

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import team.mino.core.analytics.screen.TrackScreenViews
import team.mino.core.common.ui.scaffold.MinoScaffold

@Composable
internal fun SampleShell(
    onNavigateToHome: () -> Unit,
    onRequestHomeResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    TrackScreenViews(navController)

    MinoScaffold(modifier = modifier) { innerPadding ->
        SampleNavHost(
            navController = navController,
            onNavigateToHome = onNavigateToHome,
            onRequestHomeResult = onRequestHomeResult,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
