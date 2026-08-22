package team.mino.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import team.mino.core.navigation.entry.PlaceDetailEntryOrigin
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
    onRequestPlaceDetail: (pinId: String, origin: PlaceDetailEntryOrigin) -> Unit,
    onOpenExternalMap: (mapUrl: String?, query: String) -> Unit,
    onOpenSourceLink: (url: String) -> Unit,
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
            // 장소 상세는 저장 탭 안의 화면이다. 홈이 지목한 핀을 홀더에 남기고 탭만 옮기면, 저장 탭이
            // 그 요청을 받아 상세를 연다(→ docs/specs/place-detail/contracts/place-detail-entry.md §3.2).
            // 탭 전환이 백스택을 저장·복원하는 탓에 Route 인자로는 새 핀이 전달되지 않아 홀더를 쓴다(같은 계약 §3.3).
            onNavigateToPlaceDetail = { pinId ->
                onRequestPlaceDetail(pinId, PlaceDetailEntryOrigin.HOME)
                navController.navigateToTab(MainTab.SAVED)
            },
            onNavigateToRoomForm = onNavigateToRoomForm,
            // 빈 상태 CTA도 지금은 같은 폼으로 보낸다. [SYS-009] 공동방 생성 유도 화면이 생기면 여기서만
            // 갈라 주면 된다 — 홈은 두 갈래를 따로 내보낸다
            // (→ docs/specs/home-deck-exploration/contracts/home-ui.md §1).
            onCreateRoomFromEmpty = onNavigateToRoomForm,
        )
        // 앱 밖으로 나가는 둘(외부 지도·원문 링크)만 셸이 받아 Activity에 넘긴다 — 저장 탭 안에서 끝나는
        // 전환은 그 모듈이 스스로 한다(→ docs/architecture/feature-navigation.md 3장).
        roomGraph(
            onOpenExternalMap = onOpenExternalMap,
            onOpenSourceLink = onOpenSourceLink,
            // 장소 상세 [나가기]의 홈 복귀 — 홈에서 들어와 방을 바꾸지 않았을 때만 저장 탭이 올린다
            // (→ docs/specs/place-detail/contracts/place-detail-entry.md §4.2). 저장 탭은 자기가
            // 어느 탭인지도, 홈이 몇 번째 탭인지도 모르므로 판정만 하고 이동은 셸이 한다.
            // 홈의 덱 위치는 탭 전환의 saveState/restoreState가 되살린다.
            onNavigateToHome = { navController.navigateToTab(MainTab.HOME) },
        )
        // 아직 탭 feature 모듈이 없는 탭은 전환 검증용 placeholder다. 모듈이 생기면 홈처럼 그 모듈의
        // 등록 함수 호출로 교체하고 Route 소유도 그쪽으로 옮긴다(→ docs/architecture/feature-navigation.md 3장).
        screen<Notification> { MainTabPlaceholderScreen(label = stringResource(MainTab.NOTIFICATION.labelRes)) }
        mypageGraph(navController = navController)
    }
}
