package team.mino.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * 탭 전환. 탭 이동 이력이 백스택에 쌓이지 않도록 시작 목적지까지 되감되, 떠난 탭의 상태는 저장했다가 복원한다.
 */
internal fun NavHostController.navigateToTab(tab: MainTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * 현재 선택된 탭. 탭 하위에 중첩 그래프가 생겨도 상위 탭이 선택 상태를 유지하도록 [hierarchy]를 훑는다.
 */
@Composable
internal fun NavHostController.currentTab(): MainTab? {
    val backStackEntry by currentBackStackEntryAsState()
    return backStackEntry?.destination?.let { destination ->
        MainTab.entries.firstOrNull { tab ->
            destination.hierarchy.any { it.hasRoute(tab.route::class) }
        }
    }
}
