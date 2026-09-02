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

    /** [FR-008] 자동 팝업 Nudge의 [나중에 만들래요] 클릭 또는 딤 영역 탭 — 팝업만 닫는다. */
    data object OnNudgeDismissClick : RoomListIntent

    data class OnRoomFormResult(val createdRoomId: String?) : RoomListIntent

    /**
     * [FR-002] 장소 상세 진입 — `selectedPinId`를 세우고 지도 카메라를 그 장소로 옮긴다.
     *
     * **저장 탭 안의 진입 둘**이 이 하나로 모인다: 지도 마커와 방 상세가 올린
     * `RoomDetailSideEffect.NavigateToPlaceDetail`이다
     * (`docs/specs/place-detail/contracts/place-detail-entry.md` §1·§2).
     *
     * 다른 탭이 남긴 요청은 이 인텐트로 오지 않는다 — 방을 함께 세워야 해서 ViewModel이 요청 홀더를
     * 직접 구독한다(같은 계약 §3). 카메라를 옮기는 것은 양쪽 다 같다.
     */
    data class OnPlaceSelected(val pinId: String) : RoomListIntent

    /**
     * [FR-009] 장소 상세 [나가기] — 시스템 뒤로가기도 여기로 모인다. 시트를 아래로 끌어 닫는 경로는
     * 없다(EC-003).
     * `selectedPinId`만 비우면 `selectedRoomId`가 남아 있어 그 방의 방 상세가 그대로 드러난다.
     */
    data object OnClosePlaceDetailClick : RoomListIntent

    /**
     * [FR-025] 장소 상세의 [저장된 방] 시트에서 다른 방을 골랐다 — 같은 장소를 그 방에 저장한
     * 핀([pinId])으로 갈아탄다. 지금 보고 있는 방이 [roomId]로 바뀌므로 마커 양식(TS-045)과
     * [나가기] 목적지(TS-046)가 함께 따라간다.
     */
    data class OnPlaceDetailRoomSwitched(val pinId: String, val roomId: String) : RoomListIntent

    data object OnCurrentLocationClick : RoomListIntent

    data class OnLocationPermissionResult(val granted: Boolean) : RoomListIntent
}
