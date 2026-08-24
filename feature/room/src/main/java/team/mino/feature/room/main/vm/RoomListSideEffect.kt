package team.mino.feature.room.main.vm

import team.mino.core.common.android.architecture.SideEffect

/**
 * 방 리스트 탭의 유일한 Route(RoomListMain) 일회성 이벤트.
 *
 * 계약: docs/specs/room-list/contracts/room-list-main-contract.md
 */
sealed interface RoomListSideEffect : SideEffect {
    data object RequestLocationPermission : RoomListSideEffect

    data class NavigateToRoomDetail(val roomId: String) : RoomListSideEffect

    data object NavigateToRoomForm : RoomListSideEffect
}
