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
 *
 * `navController`를 받지 않는다 — 이 탭의 화면 전환은 전부 [RoomMain] 하나 안의 로컬 상태다. 반대로 앱 밖으로
 * 나가는 둘은 Activity가 실행해야 해서 콜백으로 받는다(`docs/architecture/feature-navigation.md` 3장).
 *
 * @param onOpenExternalMap 장소 상세의 [지도보기] — 외부 지도 앱으로 연다. 링크가 없으면 검색어로 대신 연다
 *   (장소 상세 spec FR-016).
 * @param onOpenSourceLink 장소 상세의 [원문보기] — 장소의 원문 링크를 연다(장소 상세 spec FR-017).
 */
fun NavGraphBuilder.roomGraph(
    onOpenExternalMap: (mapUrl: String?, query: String) -> Unit,
    onOpenSourceLink: (url: String) -> Unit,
) {
    graph<RoomGraph>(startDestination = RoomMain) {
        screen<RoomMain> {
            RoomListRoute(
                onOpenExternalMap = onOpenExternalMap,
                onOpenSourceLink = onOpenSourceLink,
            )
        }
    }
}
