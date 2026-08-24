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

@Serializable
internal data object RoomMain : Route

/**
 * 방 리스트 탭 그래프를 셸의 [NavGraphBuilder]에 등록한다. 이 모듈의 화면 표면은 이 함수 하나다.
 *
 * `sheetLevelOverride`는 항상 `null`로 진입한다 — EC-007([SCR-005] 방 상세 `[X]` 복귀 시 시트 상태
 * 유지)은 `room-detail`이 미구현이라 아직 이 그래프로 값을 되돌려줄 경로가 없다([contracts/navigation-launchers.md](../../../../docs/specs/room-list/contracts/navigation-launchers.md)).
 */
fun NavGraphBuilder.roomGraph() {
    graph<RoomGraph>(startDestination = RoomMain) {
        screen<RoomMain> {
            RoomListRoute(sheetLevelOverride = null)
        }
    }
}
