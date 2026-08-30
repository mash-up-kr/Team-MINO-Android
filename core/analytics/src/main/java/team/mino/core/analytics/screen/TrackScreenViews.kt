package team.mino.core.analytics.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * [navController]의 목적지 전환을 감지해 화면 조회 이벤트를 자동으로 기록한다.
 *
 * [navController]를 소유하는 feature 셸(`XShell`)에서 한 번 호출해두면,
 * 이후 화면이 늘어나도 각 화면에서 별도로 로깅을 호출할 필요가 없다.
 */
@Composable
fun TrackScreenViews(navController: NavHostController) {
    val viewModel: ScreenViewTrackerViewModel = hiltViewModel()
    val backStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(backStackEntry) {
        val route = backStackEntry?.destination?.route ?: return@LaunchedEffect
        viewModel.trackScreenView(route)
    }
}
