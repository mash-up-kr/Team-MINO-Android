package team.mino.feature.room.main.vm

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.common.android.architecture.UiState
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.model.ProfileAvatar
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
    /**
     * 내 프로필 아바타 — 개인 방은 `RoomColor.GRAY`(색 미선택)라 지도 핀 색을 방에서 가져올 수 없다.
     * 개인 방 핀([RoomListMap.PersonalPlacePin])은 이 색을 대신 쓴다.
     */
    val myProfileAvatar: ProfileAvatar? = null,
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
    /**
     * 방 상세로 "전환"된 방 id. `null`이면 리스트, 값이 있으면 그 방의 상세를 같은 목적지 안에서
     * 그린다(`RoomNavigation.kt` KDoc 참고) — Navigation 목적지 전환이 아니라 이 로컬 상태로
     * 표현해야 지도(`RoomListMap`)가 계속 같은 컴포지션에 남아 카메라가 리셋되지 않는다.
     */
    val selectedRoomId: String? = null,
) : UiState
