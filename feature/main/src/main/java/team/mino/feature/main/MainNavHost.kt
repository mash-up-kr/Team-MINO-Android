package team.mino.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import team.mino.core.navigation.entry.PlaceDetailEntryOrigin
import team.mino.core.navigation.screen.MinoNavHost
import team.mino.feature.home.homeGraph
import team.mino.feature.main.placeholder.RoomFormEntryPoint
import team.mino.feature.mypage.mypageGraph
import team.mino.feature.notifications.notificationGraph
import team.mino.feature.room.roomGraph

/**
 * @param initialRoomId 초대 딥링크(SYS-010)로 특정 방 상세부터 시작해야 할 때(콜드 스타트 전용).
 *   값이 있으면 시작 탭 자체를 방 리스트 탭(`RoomGraph`)으로 바꾸고, 그 탭 그래프도 리스트가 아니라
 *   이 방의 상세에서 시작한다 — 앱이 뜨자마자 그 방으로 바로 들어가야 하기 때문이다.
 */
@Composable
internal fun MainNavHost(
    navController: NavHostController,
    onRequestPlaceDetail: (pinId: String, origin: PlaceDetailEntryOrigin) -> Unit,
    onRequestRoomDetail: (roomId: String) -> Unit,
    onOpenExternalMap: (mapUrl: String?, query: String) -> Unit,
    onOpenSourceLink: (url: String) -> Unit,
    onNavigateToRoomForm: () -> Unit,
    onNavigateToProfileEdit: () -> Unit,
    roomFormEntryPoint: RoomFormEntryPoint,
    initialRoomId: String? = null,
    modifier: Modifier = Modifier,
    startTab: MainTab = MainTab.HOME,
) {
    MinoNavHost(
        navController = navController,
        // 콜드 스타트 시작 탭을 정하는 딥링크는 둘이다 — 초대([initialRoomId])와 푸시([startTab]).
        // 초대는 방 상세까지 지목하므로 탭만 정하는 푸시보다 앞선다.
        startDestination = if (initialRoomId != null) MainTab.SAVED.route else startTab.route,
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
            // 초대 딥링크(SYS-010) 콜드 스타트 진입. 방 탭 안에서 새로 생기는 값이 아니라 Activity의
            // 진입 인자를 그대로 흘려보낸다.
            initialRoomId = initialRoomId,
        )
        // 알림 탭 밖으로 나가는 두 전환. 장소 상세도 방 상세도 저장 탭 안의 화면이라 홈과 같은 모양으로
        // 홀더에 요청을 남기고 탭만 옮긴다 — 알림 모듈은 pinId·roomId를 올리는 것까지만 안다
        // (→ docs/specs/notifications/contracts/notification-ui.md §1, research.md D10·D14).
        notificationGraph(
            navController = navController,
            // origin이 NOTIFICATION이라고 도착 화면의 소속이나 [나가기] 규칙이 달라지지는 않는다.
            // 그 갈림은 장소 상세가 홀더에서 읽어 집행한다(notifications spec UX-013·UX-016).
            onNavigateToPlaceDetail = { pinId ->
                onRequestPlaceDetail(pinId, PlaceDetailEntryOrigin.NOTIFICATION)
                navController.navigateToTab(MainTab.SAVED)
            },
            onNavigateToRoomDetail = { roomId ->
                onRequestRoomDetail(roomId)
                navController.navigateToTab(MainTab.SAVED)
            },
        )
        mypageGraph(onNavigateToProfileEdit = onNavigateToProfileEdit)
    }
}
