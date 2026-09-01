package team.mino.feature.room

import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.Route
import team.mino.core.navigation.screen.graph
import team.mino.core.navigation.screen.screen
import team.mino.feature.room.main.screen.RoomListRoute

/** 방 리스트 탭 그래프의 진입 Route. 셸의 탭 목록이 참조하므로 이 모듈이 밖으로 여는 유일한 Route다. */
@Serializable
data object RoomGraph : Route

/**
 * 방 리스트·방 상세 그래프의 유일한 목적지. 방 리스트↔방 상세 전환은 별도 목적지가 아니라
 * `RoomListViewModel.selectedRoomId` 로컬 상태로 표현한다 — 지도(`RoomListMap`)를 하나의 컴포지션에서
 * 계속 살려 두어야 리스트↔상세 전환에서 카메라가 리셋되지 않는다.
 */
@Serializable
internal data object RoomMain : Route

/**
 * 방 리스트 탭 그래프를 셸의 [NavGraphBuilder]에 등록한다. 이 모듈의 화면 표면은 이 함수 하나다.
 */
fun NavGraphBuilder.roomGraph() {
    graph<RoomGraph>(startDestination = RoomMain) {
        screen<RoomMain> { RoomListRoute() }
    }
}
