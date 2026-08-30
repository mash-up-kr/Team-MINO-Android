package team.mino.feature.home

import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable
import team.mino.core.navigation.screen.Route
import team.mino.core.navigation.screen.graph
import team.mino.core.navigation.screen.screen
import team.mino.feature.home.main.screen.HomeRoute

/** 홈 탭 그래프의 진입 Route. 셸의 탭 목록이 참조하므로 이 모듈이 밖으로 여는 유일한 Route다. */
@Serializable
data object HomeGraph : Route

@Serializable
internal data object HomeMain : Route

/**
 * 홈 탭 그래프를 셸의 [NavGraphBuilder]에 등록한다. 이 모듈의 화면 표면은 이 함수 하나다.
 *
 * feature 밖으로 나가는 전환만 콜백으로 받는다. 모듈 안에서 끝나는 전환(방 시트·액션 메뉴·가이드)은
 * 콜백으로 내보내지 않고 `HomeUiState`의 상태로 둔다.
 *
 * [onNavigateToRoomForm]과 [onCreateRoomFromEmpty]는 셸이 같은 곳으로 배선하더라도 홈이 그 판단을
 * 대신하지 않는다 — 전자는 [SYS-001] 생성 폼, 후자는 [SYS-009] 공동방 생성 유도다
 * (→ docs/specs/home-deck-exploration/contracts/home-ui.md §1).
 *
 * @param onNavigateToPlaceDetail 카드 본문 탭 → [SCR-006] 장소 상세 (FR-007)
 * @param onNavigateToRoomForm 방 시트의 `방 만들기` 칸 (EC-015)
 * @param onCreateRoomFromEmpty 빈 상태 안내의 `공동방 만들기` CTA (FR-020)
 */
fun NavGraphBuilder.homeGraph(
    onNavigateToPlaceDetail: (pinId: String) -> Unit,
    onNavigateToRoomForm: () -> Unit,
    onCreateRoomFromEmpty: () -> Unit,
) {
    graph<HomeGraph>(startDestination = HomeMain) {
        screen<HomeMain> {
            HomeRoute(
                onNavigateToPlaceDetail = onNavigateToPlaceDetail,
                onNavigateToRoomForm = onNavigateToRoomForm,
                onCreateRoomFromEmpty = onCreateRoomFromEmpty,
            )
        }
    }
}
