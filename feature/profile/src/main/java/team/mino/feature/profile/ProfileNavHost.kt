package team.mino.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import team.mino.core.navigation.screen.MinoNavHost
import team.mino.core.navigation.screen.Route
import team.mino.core.navigation.screen.screen
import team.mino.feature.profile.main.screen.ProfileRoute

@Composable
internal fun ProfileNavHost(
    navController: NavHostController,
    startDestination: Route,
    onBackClick: () -> Unit,
    onSaveCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MinoNavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        screen<ProfileMain> {
            ProfileRoute(
                onBackClick = onBackClick,
                onSaveCompleted = onSaveCompleted,
            )
        }
    }
}
