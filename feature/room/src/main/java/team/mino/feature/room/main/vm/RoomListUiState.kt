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

/**
 * 방 리스트 탭의 유일한 Route(RoomListMain) 상태.
 *
 * 계약: docs/specs/room-list/contracts/room-list-main-contract.md
 */
data class RoomListUiState(
    val sheetLevel: BottomSheetLevel = BottomSheetLevel.HALF,
    val personalRoom: Room? = null,
    val groupRooms: ImmutableList<Room> = persistentListOf(),
    val roomListSort: RoomListSortOption = RoomListSortOption.ALL,
    val mapMarkerSort: MapMarkerSortOption = MapMarkerSortOption.ALL,
    val categoryFilter: PlaceCategoryFilter = PlaceCategoryFilter.ALL,
    val showNudge: Boolean = false,
    val showGhostCard: Boolean = false,
    val mapCenter: GeoPoint? = null,
    /**
     * `mapCenter`가 갱신될 때마다 1씩 증가하는 값. 값이 아니라 "이동 요청이 있었다는 사실" 자체를
     * 신호로 써야 해서 둔다 — `GeoPoint`는 데이터 클래스라 좌표가 이전과 같으면(예: 사용자가 지도를
     * 수동으로 옮긴 뒤 같은 위치로 되돌리는 현재 위치 버튼을 다시 누른 경우) `mapCenter` 값 자체는
     * 안 바뀌어서, 이 값이 없으면 `LaunchedEffect(mapCenter)`가 재실행되지 않아 카메라가 움직이지
     * 않는다.
     */
    val mapCenterRequestId: Int = 0,
) : UiState
