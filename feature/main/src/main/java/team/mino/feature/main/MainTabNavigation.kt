package team.mino.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import team.mino.core.navigation.screen.ImmersiveRouteRegistry

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
    val destination = backStackEntry?.destination
    // hasRoute는 Route마다 직렬화 서술자를 조회한다. Scaffold가 bottomBar를 다시 서브컴포즈할 때마다
    // 반복되지 않도록 목적지가 바뀔 때만 훑는다.
    return remember(destination) {
        destination?.let {
            MainTab.entries.firstOrNull { tab ->
                it.hierarchy.any { parent -> parent.hasRoute(tab.route::class) }
            }
        }
    }
}

/**
 * 현재 목적지가 바텀 네비게이션을 숨겨야 하는 몰입 화면([team.mino.core.navigation.screen.ImmersiveRoute])인지.
 *
 * 셸은 구체 Route 타입을 몰라도 되도록 [ImmersiveRouteRegistry]로 위임한다.
 */
@Composable
internal fun NavHostController.isCurrentDestinationImmersive(): Boolean {
    val backStackEntry by currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    return remember(destination) {
        ImmersiveRouteRegistry.isImmersive(destination)
    }
}
