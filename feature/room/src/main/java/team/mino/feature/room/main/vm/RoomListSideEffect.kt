package team.mino.feature.room.main.vm

import team.mino.core.common.android.architecture.SideEffect

/**
 * 방 리스트 탭의 유일한 Route(RoomListMain) 일회성 이벤트.
 *
 * 계약: docs/specs/room-list/contracts/room-list-main-contract.md
 */
sealed interface RoomListSideEffect : SideEffect {
    data object RequestLocationPermission : RoomListSideEffect

    data object NavigateToRoomForm : RoomListSideEffect

    /**
     * [FR-009] 장소 상세 [나가기]가 홈 탭으로 되돌아가야 한다 — [SCR-003] 홈 카드로 들어왔고 그 뒤
     * [저장된 방]으로 방을 바꾸지 않은 경우다
     * (`docs/specs/place-detail/contracts/place-detail-entry.md` §4.2).
     *
     * 탭을 실제로 옮기는 것은 셸이다 — 이 모듈은 탭 목록을 모르므로 `roomGraph`의 콜백으로 올린다.
     *
     * 상태가 아니라 SideEffect인 이유는 탭 전환이 한 번 일어나고 끝나는 사건이기 때문이다. 상태로
     * 두면 구성 변경 때 다시 소비돼, 사용자가 저장 탭으로 돌아올 때마다 홈으로 튕긴다.
     */
    data object NavigateToHome : RoomListSideEffect
}
