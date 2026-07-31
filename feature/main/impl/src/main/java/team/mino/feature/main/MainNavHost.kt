package team.mino.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import team.mino.core.navigation.screen.MinoNavHost
import team.mino.core.navigation.screen.screen
import team.mino.feature.main.placeholder.screen.MainTabPlaceholderScreen

@Composable
internal fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    MinoNavHost(
        navController = navController,
        startDestination = MainTab.HOME.route,
        modifier = modifier,
    ) {
        // 각 탭 화면은 추후 탭 feature 모듈이 노출하는 등록 함수로 교체한다. 지금 등록된 것은 탭 전환 검증용
        // placeholder다. 모듈 구성은 docs/adr/2026-07-30-single-feature-module.md 참조.
        screen<Home> { MainTabPlaceholderScreen(label = stringResource(MainTab.HOME.labelRes)) }
        screen<Saved> { MainTabPlaceholderScreen(label = stringResource(MainTab.SAVED.labelRes)) }
        screen<Notification> { MainTabPlaceholderScreen(label = stringResource(MainTab.NOTIFICATION.labelRes)) }
        screen<MyPage> { MainTabPlaceholderScreen(label = stringResource(MainTab.MY_PAGE.labelRes)) }
    }
}
