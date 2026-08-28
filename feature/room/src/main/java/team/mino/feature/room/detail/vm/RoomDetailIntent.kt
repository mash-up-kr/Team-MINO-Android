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

    /** [research.md D10] 현재 위치 버튼 — room-list와 같은 동작(FR-011 부속). */
    data object OnCurrentLocationClick : RoomDetailIntent

    data class OnLocationPermissionResult(val granted: Boolean) : RoomDetailIntent
}
