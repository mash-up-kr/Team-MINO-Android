package team.mino.feature.main

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import team.mino.core.analytics.screen.TrackScreenViews
import team.mino.core.common.ui.scaffold.LocalBottomNavVisibility
import team.mino.core.common.ui.scaffold.MinoScaffold
import team.mino.core.navigation.entry.PlaceDetailEntryOrigin
import team.mino.feature.main.component.MainBottomBar
import team.mino.feature.main.placeholder.RoomFormEntryPoint

@Composable
internal fun MainShell(
    onRequestPlaceDetail: (pinId: String, origin: PlaceDetailEntryOrigin) -> Unit,
    onRequestRoomDetail: (roomId: String) -> Unit,
    onOpenExternalMap: (mapUrl: String?, query: String) -> Unit,
    onOpenSourceLink: (url: String) -> Unit,
    onNavigateToRoomForm: () -> Unit,
    onNavigateToProfileEdit: () -> Unit,
    roomFormEntryPoint: RoomFormEntryPoint,
    initialRoomId: String? = null,
    modifier: Modifier = Modifier,
    // 콜드 진입은 시작 목적지로, 웜 진입(onNewIntent)은 이미 떠 있는 NavHost에 명령형으로 탭을 옮긴다
    // (→ docs/specs/push-notification/contracts/push-deeplink-contract.md §5).
    startTab: MainTab = MainTab.HOME,
    pendingTab: MainTab? = null,
    onPendingTabConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    TrackScreenViews(navController)
    // 소비를 컴포지션 생명주기에 묶어 두면 시간 지연 가정 없이 NavHost가 준비된 뒤에만 전환된다.
    LaunchedEffect(pendingTab) {
        pendingTab?.let {
            navController.navigateToTab(it)
            onPendingTabConsumed()
        }
    }
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
                startTab = startTab,
                onRequestPlaceDetail = onRequestPlaceDetail,
                onRequestRoomDetail = onRequestRoomDetail,
                onOpenExternalMap = onOpenExternalMap,
                onOpenSourceLink = onOpenSourceLink,
                onNavigateToRoomForm = onNavigateToRoomForm,
                onNavigateToProfileEdit = onNavigateToProfileEdit,
                roomFormEntryPoint = roomFormEntryPoint,
                initialRoomId = initialRoomId,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
