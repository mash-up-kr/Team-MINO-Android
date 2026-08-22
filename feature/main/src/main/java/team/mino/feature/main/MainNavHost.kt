package team.mino.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import team.mino.core.navigation.screen.MinoNavHost
import team.mino.core.navigation.screen.screen
import team.mino.feature.home.homeGraph
import team.mino.feature.main.placeholder.RoomFormEntryPoint
import team.mino.feature.main.placeholder.screen.MainTabPlaceholderScreen
import team.mino.feature.mypage.mypageGraph
import team.mino.feature.room.roomGraph

@Composable
internal fun MainNavHost(
    navController: NavHostController,
    onNavigateToPlaceDetail: (pinId: String) -> Unit,
    onNavigateToRoomForm: () -> Unit,
    roomFormEntryPoint: RoomFormEntryPoint,
    modifier: Modifier = Modifier,
) {
    MinoNavHost(
        navController = navController,
        startDestination = MainTab.HOME.route,
        modifier = modifier,
    ) {
        homeGraph(
            onNavigateToPlaceDetail = onNavigateToPlaceDetail,
            onNavigateToRoomForm = onNavigateToRoomForm,
            // 빈 상태 CTA도 지금은 같은 폼으로 보낸다. [SYS-009] 공동방 생성 유도 화면이 생기면 여기서만
            // 갈라 주면 된다 — 홈은 두 갈래를 따로 내보낸다
            // (→ docs/specs/home-deck-exploration/contracts/home-ui.md §1).
            onCreateRoomFromEmpty = onNavigateToRoomForm,
        )
        roomGraph()
        // 아직 탭 feature 모듈이 없는 탭은 전환 검증용 placeholder다. 모듈이 생기면 홈처럼 그 모듈의
        // 등록 함수 호출로 교체하고 Route 소유도 그쪽으로 옮긴다(→ docs/architecture/feature-navigation.md 3장).
        screen<Notification> { MainTabPlaceholderScreen(label = stringResource(MainTab.NOTIFICATION.labelRes)) }
        mypageGraph(navController = navController)
    }
}
