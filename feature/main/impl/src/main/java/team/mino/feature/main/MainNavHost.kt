package team.mino.feature.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import team.mino.core.analytics.screen.TrackScreenViews
import team.mino.core.navigation.screen.MinoNavHost
import team.mino.core.navigation.screen.screen
import team.mino.feature.main.component.MainBottomBar
import team.mino.feature.main.placeholder.screen.MainTabPlaceholderScreen

@Composable
internal fun MainNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    TrackScreenViews(navController)

    Scaffold(
        modifier = modifier,
        // 현재 탭은 bottomBar 슬롯 안에서 읽는다. 바깥에서 읽으면 화면 전환마다 Scaffold와 NavHost까지 리컴포지션된다.
        bottomBar = {
            MainBottomBar(
                currentTab = navController.currentTab(),
                onTabSelected = navController::navigateToTab,
            )
        },
    ) { innerPadding ->
        MinoNavHost(
            navController = navController,
            startDestination = MainTab.HOME.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            // 각 탭 화면은 추후 탭 feature 모듈이 노출하는 등록 함수로 교체한다. 지금 등록된 것은 탭 전환 검증용
            // placeholder다. 모듈 구성은 docs/adr/2026-07-30-single-feature-module.md 참조.
            screen<Home> { MainTabPlaceholderScreen(label = stringResource(MainTab.HOME.labelRes)) }
            screen<Saved> { MainTabPlaceholderScreen(label = stringResource(MainTab.SAVED.labelRes)) }
            screen<Notification> { MainTabPlaceholderScreen(label = stringResource(MainTab.NOTIFICATION.labelRes)) }
            screen<MyPage> { MainTabPlaceholderScreen(label = stringResource(MainTab.MY_PAGE.labelRes)) }
        }
    }
}
