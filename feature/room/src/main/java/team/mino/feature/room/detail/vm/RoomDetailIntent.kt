package team.mino.feature.room.detail.vm

import kotlinx.collections.immutable.ImmutableList
import team.mino.core.common.android.architecture.Intent
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.Place
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.feature.room.detail.model.PlaceViewType

/**
 * 방 상세 화면의 유일한 Route 의도.
 *
 * 계약: docs/specs/room-detail/contracts/room-detail-main-contract.md
 */
internal sealed interface RoomDetailIntent : Intent {
    data object OnScreenEntered : RoomDetailIntent

    /**
     * 방 상세를 벗어날 때(방 리스트로 복귀) 발행한다. `RoomDetailRoute`가 이 화면의 유일한 진입점이 아니라
     * `RoomListRoute`가 `selectedRoomId` 로컬 상태로 열고 닫는 구조라, 시스템 뒤로가기가 `RoomListRoute`의
     * `BackHandler`에서 바로 처리돼([RoomListRoute.kt] 참고) 이 ViewModel의 닫기 경로([OnCloseClick])를
     * 거치지 않을 수 있다. `hiltViewModel(key = roomId)`가 같은 방으로 다시 들어올 때 **같은 인스턴스**를
     * 돌려주므로, 화면을 뜨는 시점(`DisposableEffect.onDispose`)에 이 화면 전용 오버레이 상태를 정리해
     * 두지 않으면 다음 진입 때 이전에 닫지 않은 시트가 그대로 남는다.
     */
    data object OnScreenExited : RoomDetailIntent

    data object OnSheetDraggedUp : RoomDetailIntent

    data object OnSheetDraggedDown : RoomDetailIntent

    data class OnSortSelected(val option: MapMarkerSortOption) : RoomDetailIntent

    data class OnCategoryFilterSelected(val category: PlaceCategoryFilter) : RoomDetailIntent

    data class OnViewTypeSelected(val viewType: PlaceViewType) : RoomDetailIntent

    data object OnCloseClick : RoomDetailIntent

    data object OnPlaceClick : RoomDetailIntent

    data class OnPlaceMoreClick(val place: Place) : RoomDetailIntent

    data object OnPlaceMoreDismiss : RoomDetailIntent

    data class OnShareToOtherRoomClick(val place: Place) : RoomDetailIntent

    data class OnPlaceDeleteClick(val place: Place) : RoomDetailIntent

    data object OnPlaceDeleteConfirm : RoomDetailIntent

    data object OnPlaceDeleteCancel : RoomDetailIntent

    data class OnRoomSelectConfirm(val targetRoomIds: ImmutableList<String>) : RoomDetailIntent

    data object OnRoomSelectDismiss : RoomDetailIntent

    /** [FR-009] 공유 시트의 [+ 새 방 만들기] — Figma `2862-175306`. */
    data object OnShareCreateRoomClick : RoomDetailIntent

    /**
     * `RoomFormLauncher` 생성 모드 결과 수신([OnShareCreateRoomClick]에서 연 폼) — `createdRoomId`가
     * 있으면(생성 완료) 그 방을 [RoomDetailUiState.myRooms]에 더한다. `null`(취소)이면 아무 것도 하지
     * 않는다. [OnRoomFormResult]는 "현재 방 편집" 전용이라 이 결과와 섞으면 안 된다.
     */
    data class OnShareRoomFormResult(val createdRoomId: String?) : RoomDetailIntent

    data object OnMoreMenuClick : RoomDetailIntent

    data object OnMoreMenuDismiss : RoomDetailIntent

    data object OnInviteClick : RoomDetailIntent

    data object OnInviteSheetDismiss : RoomDetailIntent

    /**
     * 초대 시트의 "초대하기" 버튼 — `inviteCode`를 링크로 조립해 OS 공유 시트를 연다
     * ([RoomDetailSideEffect.ShareInviteLink]). Figma `3261-204321`의 전용 화면 전환은 아직
     * 범위 밖이다(spec.md "초대 링크 생성·공유 로직 자체는 SYS-006이 정의한다").
     */
    data object OnInviteConfirmClick : RoomDetailIntent

    /** 초대 시트의 "링크 복사하기" 버튼 — 같은 링크를 클립보드에 쓴다([RoomDetailSideEffect.CopyInviteLink]). */
    data object OnCopyInviteLinkClick : RoomDetailIntent

    data object OnEditRoomClick : RoomDetailIntent

    data object OnLeaveClick : RoomDetailIntent

    data object OnLeaveConfirm : RoomDetailIntent

    data object OnLeaveCancel : RoomDetailIntent

    data class OnOwnerDelegateSelected(val memberId: String) : RoomDetailIntent

    data object OnOwnerDelegateConfirm : RoomDetailIntent

    /**
     * [FR-012] `RoomFormLauncher` 편집 모드 결과 수신 — `updated`는
     * `EXTRA_ROOM_FORM_RESULT_OUTCOME == ROOM_FORM_OUTCOME_UPDATED`인지 여부다
     * (`docs/specs/group-room-form/contracts/room-form-launcher.md`).
     * `false`(취소·그 외 결과)면 아무 것도 하지 않는다.
     */
    data class OnRoomFormResult(val updated: Boolean) : RoomDetailIntent
}
