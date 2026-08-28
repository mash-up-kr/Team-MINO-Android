package team.mino.feature.room.main.vm

import team.mino.core.common.android.architecture.Intent
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.model.RoomListSortOption

/**
 * 방 리스트 탭의 유일한 Route(RoomListMain) 의도.
 *
 * 계약: docs/specs/room-list/contracts/room-list-main-contract.md
 */
sealed interface RoomListIntent : Intent {
    data object OnScreenEntered : RoomListIntent

    data object OnSheetDraggedUp : RoomListIntent

    data object OnSheetDraggedDown : RoomListIntent

    data class OnMapSortSelected(val option: MapMarkerSortOption) : RoomListIntent

    data class OnCategoryFilterSelected(val category: PlaceCategoryFilter) : RoomListIntent

    data class OnRoomListSortSelected(val option: RoomListSortOption) : RoomListIntent

    data class OnRoomCardClick(val roomId: String) : RoomListIntent

    /** 방 상세 [X] 닫기 — `selectedRoomId`를 `null`로 되돌려 리스트로 복귀한다. */
    data object OnCloseRoomDetailClick : RoomListIntent

    data object OnAddRoomClick : RoomListIntent

    data object OnGhostCardClick : RoomListIntent

    data object OnNudgeCreateClick : RoomListIntent

    data class OnRoomFormResult(val createdRoomId: String?) : RoomListIntent

    data object OnCurrentLocationClick : RoomListIntent

    data class OnLocationPermissionResult(val granted: Boolean) : RoomListIntent
}
