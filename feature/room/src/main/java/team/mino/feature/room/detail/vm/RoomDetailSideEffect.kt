package team.mino.feature.room.detail.vm

import team.mino.core.common.android.architecture.SideEffect

/**
 * 방 상세 화면의 유일한 Route 일회성 이벤트.
 *
 * 계약: docs/specs/room-detail/contracts/room-detail-main-contract.md
 */
internal sealed interface RoomDetailSideEffect : SideEffect {
    data object NavigateBack : RoomDetailSideEffect

    data class NavigateToPlaceDetail(val placeId: String) : RoomDetailSideEffect

    data object NavigateToRoomForm : RoomDetailSideEffect

    /** [FR-009] 공유 시트의 [+ 새 방 만들기] — `RoomFormLauncher`를 생성 모드(roomId 없이)로 연다. */
    data object NavigateToCreateRoomForm : RoomDetailSideEffect

    data object ShowShareCompleteToast : RoomDetailSideEffect

    data object ShowEditCompleteSnackbar : RoomDetailSideEffect

    data object NavigateToRoomList : RoomDetailSideEffect

    data object RequestLocationPermission : RoomDetailSideEffect
}
