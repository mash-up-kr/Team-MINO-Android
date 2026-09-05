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
 *
 * @param initialRoomId 이 탭이 리스트가 아니라 특정 방의 상세로 바로 시작해야 할 때(초대 딥링크
 *  SYS-010 진입). `RoomListViewModel`이 `selectedRoomId`의 초기값으로 복원한다 — 카드를 눌러 여는
 *  것과 같은 로컬 상태 전환이라 별도 목적지를 두지 않는다. 기본값은 프리미티브 인자의 방식을 따른
 *  `RoomFormViewModel`과 같은 이유(JVM 단위 테스트에서 `SavedStateHandle` 디코딩)로 둔다.
 */
@Serializable
internal data class RoomMain(val initialRoomId: String? = null) : Route

/**
 * 방 리스트 탭 그래프를 셸의 [NavGraphBuilder]에 등록한다. 이 모듈의 화면 표면은 이 함수 하나다.
 *
 * `navController`를 받지 않는다 — 이 탭의 화면 전환은 **거의** 전부 [RoomMain] 하나 안의 로컬 상태다.
 * 밖으로 나가는 셋만 콜백으로 받는다: 앱 밖으로 나가는 둘(외부 지도·원문 링크)은 Activity가 실행해야
 * 하고, 다른 탭으로 가는 하나(홈 복귀)는 탭 목록을 셸만 알기 때문이다
 * (`docs/architecture/feature-navigation.md` 3장).
 *
 * @param initialRoomId [RoomMain.initialRoomId] 참고 — 셸이 콜드 스타트 진입 인자(초대 딥링크)를 그대로
 *   전달하는 자리다. 이 탭 안에서 새로 생기는 값이 아니라 밖에서 들어오는 값이라 `navController`처럼
 *   그래프가 소유하지 않고 파라미터로 받는다.
 * @param onOpenExternalMap 장소 상세의 [지도보기] — 외부 지도 앱으로 연다. 링크가 없으면 검색어로 대신 연다
 *   (장소 상세 spec FR-016).
 * @param onOpenSourceLink 장소 상세의 [원문보기] — 장소의 원문 링크를 연다(장소 상세 spec FR-017).
 * @param onNavigateToHome 장소 상세 [나가기]가 홈 탭으로 되돌아가야 할 때(장소 상세 spec FR-009). 이 탭
 *   **안에서** 끝나지 않는 유일한 화면 전환이라 콜백으로 받는다 — 탭 목록을 아는 것은 셸뿐이다.
 */
fun NavGraphBuilder.roomGraph(
    onOpenExternalMap: (mapUrl: String?, query: String) -> Unit,
    onOpenSourceLink: (url: String) -> Unit,
    onNavigateToHome: () -> Unit,
    initialRoomId: String? = null,
) {
    graph<RoomGraph>(startDestination = RoomMain(initialRoomId)) {
        screen<RoomMain> {
            RoomListRoute(
                onOpenExternalMap = onOpenExternalMap,
                onOpenSourceLink = onOpenSourceLink,
                onNavigateToHome = onNavigateToHome,
            )
        }
    }
}
