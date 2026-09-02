package team.mino.feature.main

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import team.mino.core.analytics.screen.TrackScreenViews
import team.mino.core.common.ui.scaffold.LocalBottomNavVisibility
import team.mino.core.common.ui.scaffold.MinoScaffold
import team.mino.feature.main.component.MainBottomBar
import team.mino.feature.main.placeholder.RoomFormEntryPoint

@Composable
internal fun MainShell(
    onNavigateToPlaceDetail: (pinId: String) -> Unit,
    onNavigateToRoomForm: () -> Unit,
    onNavigateToProfileEdit: () -> Unit,
    roomFormEntryPoint: RoomFormEntryPoint,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    TrackScreenViews(navController)
    val bottomNavVisibility = remember { mutableStateOf(true) }

    CompositionLocalProvider(LocalBottomNavVisibility provides bottomNavVisibility) {
        MinoScaffold(
            modifier = modifier,
            // 현재 탭·몰입 여부는 bottomBar 슬롯 안에서 읽는다. 바깥에서 읽으면 탭 전환마다 셸과 그래프까지 리컴포지션된다.
            bottomBar = {
                if (!navController.isCurrentDestinationImmersive() && bottomNavVisibility.value) {
                    MainBottomBar(
                        currentTab = navController.currentTab(),
                        onTabSelected = navController::navigateToTab,
                    )
                }
            },
        ) { innerPadding ->
            MainNavHost(
                navController = navController,
                onNavigateToPlaceDetail = onNavigateToPlaceDetail,
                onNavigateToRoomForm = onNavigateToRoomForm,
                onNavigateToProfileEdit = onNavigateToProfileEdit,
                roomFormEntryPoint = roomFormEntryPoint,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
