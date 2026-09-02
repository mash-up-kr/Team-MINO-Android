package team.mino.feature.room.detail.vm

import team.mino.core.common.android.architecture.SideEffect

/**
 * 방 상세 화면의 유일한 Route 일회성 이벤트.
 *
 * 계약: docs/specs/room-detail/contracts/room-detail-main-contract.md
 */
internal sealed interface RoomDetailSideEffect : SideEffect {
    data object NavigateBack : RoomDetailSideEffect

    /**
     * [SCR-006] 장소 상세 열기 — 인자는 `Pin.id`(= `Place.id`)다. 핀 = (장소, 방) 쌍이라 「지금 보고 있는 방」이
     * 이 값 안에 이미 정해져 있다(docs/specs/place-detail/contracts/place-detail-entry.md §2).
     *
     * 이 화면은 목적지를 직접 열지 않는다 — `RoomDetailRoute`가 `onOpenPlaceDetail`로 올리면
     * `RoomListRoute`가 `RoomListIntent.OnPlaceSelected(pinId)`로 넘겨 `selectedPinId`를 세운다.
     */
    data class NavigateToPlaceDetail(val pinId: String) : RoomDetailSideEffect

    data object NavigateToRoomForm : RoomDetailSideEffect

    /** [FR-009] 공유 시트의 [+ 새 방 만들기] — `RoomFormLauncher`를 생성 모드(roomId 없이)로 연다. */
    data object NavigateToCreateRoomForm : RoomDetailSideEffect

    data object ShowShareCompleteToast : RoomDetailSideEffect

    data object ShowEditCompleteSnackbar : RoomDetailSideEffect

    data object NavigateToRoomList : RoomDetailSideEffect

    /** [FR-011] 초대 시트 "초대하기" — OS 공유 시트를 [link]로 연다(`OnboardingActivity.shareInviteLink`와 같은 패턴). */
    data class ShareInviteLink(val link: String) : RoomDetailSideEffect

    /** [FR-011] 초대 시트 "링크 복사하기" — [link]를 클립보드에 쓴다. */
    data class CopyInviteLink(val link: String) : RoomDetailSideEffect
}
