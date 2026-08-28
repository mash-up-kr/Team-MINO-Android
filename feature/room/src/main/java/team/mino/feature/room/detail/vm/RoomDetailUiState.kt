package team.mino.feature.room.detail.vm

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.common.android.architecture.UiState
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.Place
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomMember
import team.mino.core.errorhandling.MinoDomainException
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
    // room-list의 mapMarkerSort 기본값(ALL)과 맞춘다 — GGUK_PICK을 기본값으로 두면 진입 즉시 트리거가
    // "꾹 Pick"으로 보여 room-list와 달라 보인다.
    val sortOption: MapMarkerSortOption = MapMarkerSortOption.ALL,
    val categoryFilter: PlaceCategoryFilter = PlaceCategoryFilter.ALL,
    val mapCenter: GeoPoint? = null,
    /** [RoomListUiState.mapCenterRequestId]와 같은 이유로 둔다 — 좌표값이 같아도 이동 요청을 신호할 수 있어야 한다. */
    val mapCenterRequestId: Int = 0,
    val viewType: PlaceViewType = PlaceViewType.LIST,
    val isOwner: Boolean = false,
    val isPersonalRoom: Boolean = false,
    val showMoreMenu: Boolean = false,
    val menuTargetPlace: Place? = null,
    val showRoomSelectSheet: Boolean = false,
    val placeToShare: Place? = null,
    val myRooms: ImmutableList<Room> = persistentListOf(),
    val showInviteSheet: Boolean = false,
    val inviteCode: String? = null,
    val roomMembers: ImmutableList<RoomMember> = persistentListOf(),
    val placeToDelete: Place? = null,
    val leaveDialogState: LeaveDialogState = LeaveDialogState.None,
    val selectedDelegateMemberId: String? = null,
    val loadError: MinoDomainException? = null,
) : UiState

/**
 * 나가기/위임 모달 상태([SYS-007]).
 */
internal enum class LeaveDialogState { None, ConfirmMember, ConfirmOwnerSingle, DelegateOwner }
