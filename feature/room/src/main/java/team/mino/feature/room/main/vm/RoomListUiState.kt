package team.mino.feature.room.main.vm

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.common.android.architecture.UiState
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomListSortOption
import team.mino.feature.room.main.model.BottomSheetLevel
import team.mino.feature.room.main.model.MapPinUiModel

/**
 * 방 리스트 탭의 유일한 Route(RoomListMain) 상태.
 *
 * 계약: docs/specs/room-list/contracts/room-list-main-contract.md
 */
data class RoomListUiState(
    val sheetLevel: BottomSheetLevel = BottomSheetLevel.HALF,
    val personalRoom: Room? = null,
    /**
     * 내가 속한 모든 방(개인 방 + 공동방)에 저장된 장소 전체 — 지도에 얹을 핀과 그 색을 함께 든다.
     * 방 목록이 바뀌거나 방마다의 장소 조회가 끝날 때마다 [RoomListViewModel]이 다시 계산한다.
     */
    val mapPins: ImmutableList<MapPinUiModel> = persistentListOf(),
    val groupRooms: ImmutableList<Room> = persistentListOf(),
    val roomListSort: RoomListSortOption = RoomListSortOption.ALL,
    val mapMarkerSort: MapMarkerSortOption = MapMarkerSortOption.ALL,
    val categoryFilter: PlaceCategoryFilter = PlaceCategoryFilter.ALL,
    val showNudge: Boolean = false,
    val showGhostCard: Boolean = false,
    /**
     * [FR-008] 자동 팝업 Nudge 바텀시트([RoomNudgeAutoSheet])의 닫힘 여부 — [showNudge]와 별개다.
     * `showNudge`는 "공동방 0개"만 뜻하고([RoomNudgeSheet]의 `Full` 인라인 카드가 계속 참조한다),
     * 이 값은 그중에서도 팝업을 이미 닫았는지를 이 화면 방문 동안만 기억한다. 탭에 재진입할 때마다
     * `false`로 초기화돼 다시 표출된다(EC-005, 세션당 1회 제한 없음).
     */
    val nudgeSheetDismissed: Boolean = false,
    val mapCenter: GeoPoint? = null,
    /**
     * `mapCenter`가 갱신될 때마다 1씩 증가하는 값. 값이 아니라 "이동 요청이 있었다는 사실" 자체를
     * 신호로 써야 해서 둔다 — `GeoPoint`는 데이터 클래스라 좌표가 이전과 같으면(예: 사용자가 지도를
     * 수동으로 옮긴 뒤 같은 위치로 되돌리는 현재 위치 버튼을 다시 누른 경우) `mapCenter` 값 자체는
     * 안 바뀌어서, 이 값이 없으면 `LaunchedEffect(mapCenter)`가 재실행되지 않아 카메라가 움직이지
     * 않는다.
     */
    val mapCenterRequestId: Int = 0,
    /**
     * 방 상세로 "전환"된 방 id. `null`이면 리스트, 값이 있으면 그 방의 상세를 같은 목적지 안에서
     * 그린다(`RoomNavigation.kt` KDoc 참고) — Navigation 목적지 전환이 아니라 이 로컬 상태로
     * 표현해야 지도(`RoomListMap`)가 계속 같은 컴포지션에 남아 카메라가 리셋되지 않는다.
     */
    val selectedRoomId: String? = null,
) : UiState {
    /**
     * [FR-008] 자동 팝업 [RoomNudgeAutoSheet]를 실제로 그려야 하는지 — [RoomListScreen]과
     * [RoomListRoute] 둘 다 이 값을 봐야 한다(Route는 바텀 네비게이션을 숨기려고, Screen은 시트
     * 자체를 그리려고). 한쪽만 고치고 다른 쪽을 안 고치는 사고를 막으려고 한 곳에서만 계산한다.
     * `Full`에서는 인라인 넛지 카드([RoomNudgeSheet])만 있고 이 팝업 자체가 없으므로 제외한다.
     */
    val isNudgeSheetVisible: Boolean
        get() = showNudge && !nudgeSheetDismissed && sheetLevel != BottomSheetLevel.FULL
}
