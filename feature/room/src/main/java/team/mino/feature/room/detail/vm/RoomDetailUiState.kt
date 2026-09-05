package team.mino.feature.room.detail.vm

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import team.mino.core.common.android.architecture.UiState
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.Place
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomMember
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.room.component.RoomShareItem
import team.mino.feature.room.detail.model.PlaceViewType
import team.mino.feature.room.main.model.BottomSheetLevel

/**
 * 방 상세 화면의 유일한 Route 상태.
 *
 * 계약: docs/specs/room-detail/contracts/room-detail-main-contract.md
 */
internal data class RoomDetailUiState(
    val room: Room? = null,
    val sheetLevel: BottomSheetLevel = BottomSheetLevel.HALF,
    val places: ImmutableList<Place> = persistentListOf(),
    // [SYS-007과 무관, FR-011] 장소 목록 페이징 — 20개씩(`DEFAULT_PLACES_PAGE_SIZE`) 나눠 받는다("api
    // 낭비 없게"). 지도(RoomListViewModel.observePlaces)는 여전히 전체를 한 번에 받으므로 이 상태와
    // 무관하다.
    val placesNextPage: Int = 0,
    val hasMorePlaces: Boolean = true,
    val isLoadingMorePlaces: Boolean = false,
    // room-list의 mapMarkerSort 기본값(ALL)과 맞춘다 — GGUK_PICK을 기본값으로 두면 진입 즉시 트리거가
    // "꾹 Pick"으로 보여 room-list와 달라 보인다.
    val sortOption: MapMarkerSortOption = MapMarkerSortOption.ALL,
    val categoryFilter: PlaceCategoryFilter = PlaceCategoryFilter.ALL,
    val viewType: PlaceViewType = PlaceViewType.LIST,
    val isOwner: Boolean = false,
    val isPersonalRoom: Boolean = false,
    val showMoreMenu: Boolean = false,
    val menuTargetPlace: Place? = null,
    // [SYS-003] 공유 시트. null이 닫힘이다 — 여는 순간 대상 장소가 정해지므로 열림 플래그를 따로 두지 않는다.
    val placeToShare: Place? = null,
    val shareRooms: ImmutableList<RoomShareItem> = persistentListOf(),
    val shareSelectedRoomIds: ImmutableSet<String> = persistentSetOf(),
    val isSharing: Boolean = false,
    val showInviteSheet: Boolean = false,
    val inviteCode: String? = null,
    val roomMembers: ImmutableList<RoomMember> = persistentListOf(),
    val placeToDelete: Place? = null,
    val leaveDialogState: LeaveDialogState = LeaveDialogState.None,
    val selectedDelegateMemberId: String? = null,
    val loadError: MinoDomainException? = null,
) : UiState {
    /** 하나라도 고른 뒤에야 보낼 곳이 정해진다. 보내는 중에는 같은 방에 두 번 가지 않게 잠근다(FR-009). */
    val isShareEnabled: Boolean
        get() = shareSelectedRoomIds.isNotEmpty() && !isSharing

    /**
     * 시스템 뒤로가기가 방 상세 전체를 닫기 전에 먼저 닫아야 할 오버레이가 있으면, 그걸 닫는 인텐트를
     * 돌려준다(#290 QA로 발견 — 예전엔 `RoomListRoute`의 `BackHandler`가 이 화면의 오버레이 상태를 몰라
     * 무조건 방 상세 전체를 닫았다). "오버레이가 있는지"와 "무엇을 닫을지"를 한 값에서 함께 판정해야,
     * 오버레이 종류가 늘어날 때 이 자리 하나만 고치면 되고 `RoomDetailRoute`의 `BackHandler`가 따로
     * 조건을 다시 나열하다 하나만 빠뜨리는 사고를 막는다. 얼럿류(`leaveDialogState`·`placeToDelete`)가
     * 시트류보다 항상 위에 뜨므로 먼저 닫는다.
     */
    val dismissibleOverlayIntent: RoomDetailIntent?
        get() = when {
            leaveDialogState != LeaveDialogState.None -> RoomDetailIntent.OnLeaveCancel
            placeToDelete != null -> RoomDetailIntent.OnPlaceDeleteCancel
            showInviteSheet -> RoomDetailIntent.OnInviteSheetDismiss
            placeToShare != null -> RoomDetailIntent.OnRoomSelectDismiss
            showMoreMenu -> RoomDetailIntent.OnMoreMenuDismiss
            else -> null
        }
}

/**
 * 나가기/위임 모달 상태([SYS-007]).
 */
internal enum class LeaveDialogState {
    None,
    ConfirmMember,
    ConfirmOwnerSingle,
    ConfirmOwnerDelegateIntro,
    DelegateOwner,
}
