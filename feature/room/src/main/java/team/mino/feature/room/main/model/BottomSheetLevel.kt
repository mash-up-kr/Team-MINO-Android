package team.mino.feature.room.main.model

/**
 * 방 리스트 화면 바텀시트 3단계.
 *
 * 별도 Route가 아니라 [team.mino.feature.room.main.vm.RoomListUiState]의 상태다
 * (docs/specs/room-list/contracts/room-list-main-contract.md 참고).
 */
enum class BottomSheetLevel {
    PEEK,
    HALF,
    FULL,
}
