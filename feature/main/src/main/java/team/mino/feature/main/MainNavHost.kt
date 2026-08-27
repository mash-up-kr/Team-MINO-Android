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
import team.mino.feature.main.placeholder.screen.RoomFormEntryPlaceholderScreen

@Composable
internal fun MainNavHost(
    navController: NavHostController,
    onNavigateToSample: () -> Unit,
    onRequestSampleResult: () -> Unit,
    roomFormEntryPoint: RoomFormEntryPoint,
    modifier: Modifier = Modifier,
) {
    MinoNavHost(
        navController = navController,
        startDestination = MainTab.HOME.route,
        modifier = modifier,
    ) {
        homeGraph(
            onNavigateToSample = onNavigateToSample,
            onRequestSampleResult = onRequestSampleResult,
        )
        // 저장 탭 자리는 방 폼을 눌러 볼 임시 검증 진입점이 빌려 쓴다. 폼을 여는 경로가 아직 저장소 어디에도
        // 없기 때문이다. 진입점 feature가 생기면 아래 placeholder들과 같은 모양으로 되돌린다
        // (→ docs/specs/group-room-form/plan.md §범위 경계).
        screen<Saved> { RoomFormEntryPlaceholderScreen(entryPoint = roomFormEntryPoint) }
        // 아직 탭 feature 모듈이 없는 탭은 전환 검증용 placeholder다. 모듈이 생기면 홈처럼 그 모듈의
        // 등록 함수 호출로 교체하고 Route 소유도 그쪽으로 옮긴다(→ docs/architecture/feature-navigation.md 3장).
        screen<Notification> { MainTabPlaceholderScreen(label = stringResource(MainTab.NOTIFICATION.labelRes)) }
        screen<MyPage> { MainTabPlaceholderScreen(label = stringResource(MainTab.MY_PAGE.labelRes)) }
    }
}
