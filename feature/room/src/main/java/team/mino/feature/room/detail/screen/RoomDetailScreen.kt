package team.mino.feature.room.detail.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.MyLocation
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.shadow.dropShadow
import team.mino.core.domain.model.Place
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomMember
import team.mino.feature.room.detail.component.PlaceActionMenu
import team.mino.feature.room.detail.component.PlaceCardGrid
import team.mino.feature.room.detail.component.PlaceCardList
import team.mino.feature.room.detail.component.PlaceDeleteConfirmDialog
import team.mino.feature.room.detail.component.RoomDetailBottomSheet
import team.mino.feature.room.detail.component.RoomDetailMapControls
import team.mino.feature.room.detail.component.RoomInviteSheet
import team.mino.feature.room.detail.component.RoomLeaveConfirmDialog
import team.mino.feature.room.detail.component.RoomOwnerLeaveDialog
import team.mino.feature.room.detail.component.RoomSelectSheet
import team.mino.feature.room.detail.component.roomDetailBottomSheetHeightOrNull
import team.mino.feature.room.detail.model.PlaceViewType
import team.mino.feature.room.detail.vm.LeaveDialogState
import team.mino.feature.room.detail.vm.RoomDetailIntent
import team.mino.feature.room.detail.vm.RoomDetailUiState
import team.mino.feature.room.main.model.BottomSheetLevel

/**
 * 방 상세 화면([SCR-005]). 지도는 이 화면이 그리지 않는다 — 호출부(`RoomListScreen`)가 이미 그린
 * `RoomListMap` 위에 이 컴포저블의 컨트롤·3단 바텀시트([RoomDetailBottomSheet])·오버레이만 얹는다
 * (리스트↔상세 전환에도 지도 인스턴스를 하나만 유지하기 위함, `RoomListScreen`/`RoomDetailRoute` KDoc
 * 참고). 더보기[⋮]는 화면 레벨 플로팅이 아니라 [RoomDetailBottomSheet] 헤더 줄 안에서 그려진다
 * (Figma `2542:125409`(Peek)·`2542:125383`(Half) 대조 결과 — [RoomDetailBottomSheet] KDoc 참고).
 */
@Composable
internal fun BoxScope.RoomDetailScreen(
    state: RoomDetailUiState,
    onIntent: (RoomDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMapControlVisible = state.sheetLevel != BottomSheetLevel.FULL

    if (isMapControlVisible) {
        // Figma `2542:125409`(Peek)·`2542:125383`(Half) 대조 — 정렬 트리거+카테고리 탭은 시트 안이
        // 아니라 지도 위, 상태바 바로 아래 고정 오버레이다. `RoomListScreen`의 `RoomListMapControls`와
        // 같은 위치 규칙(start/top/end 20dp, bottom 12dp).
        RoomDetailMapControls(
            sortOption = state.sortOption,
            categoryFilter = state.categoryFilter,
            onSortSelected = { onIntent(RoomDetailIntent.OnSortSelected(it)) },
            onCategoryFilterSelected = { onIntent(RoomDetailIntent.OnCategoryFilterSelected(it)) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 12.dp),
        )

        val sheetHeight = roomDetailBottomSheetHeightOrNull(state.sheetLevel) ?: 0.dp
        RoomDetailCurrentLocationButton(
            onClick = { onIntent(RoomDetailIntent.OnCurrentLocationClick) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = sheetHeight + RoomDetailCurrentLocationButtonTokens.GapAboveSheet),
        )
    }

    RoomDetailBottomSheet(
        sheetLevel = state.sheetLevel,
        room = state.room,
        placeCount = state.places.size,
        sortOption = state.sortOption,
        categoryFilter = state.categoryFilter,
        viewType = state.viewType,
        showMoreMenu = state.showMoreMenu,
        isOwner = state.isOwner,
        isPersonalRoom = state.isPersonalRoom,
        onDraggedUp = { onIntent(RoomDetailIntent.OnSheetDraggedUp) },
        onDraggedDown = { onIntent(RoomDetailIntent.OnSheetDraggedDown) },
        onSortSelected = { onIntent(RoomDetailIntent.OnSortSelected(it)) },
        onCategoryFilterSelected = { onIntent(RoomDetailIntent.OnCategoryFilterSelected(it)) },
        onViewTypeSelected = { onIntent(RoomDetailIntent.OnViewTypeSelected(it)) },
        onInviteClick = { onIntent(RoomDetailIntent.OnInviteClick) },
        onCloseClick = { onIntent(RoomDetailIntent.OnCloseClick) },
        onMoreMenuClick = { onIntent(RoomDetailIntent.OnMoreMenuClick) },
        onMoreMenuDismiss = { onIntent(RoomDetailIntent.OnMoreMenuDismiss) },
        onEditRoomClick = { onIntent(RoomDetailIntent.OnEditRoomClick) },
        onLeaveClick = { onIntent(RoomDetailIntent.OnLeaveClick) },
        modifier = modifier.align(Alignment.BottomCenter),
        content = {
            if (state.places.isEmpty()) {
                RoomDetailEmptyPlaces()
            } else {
                val onPlaceClick: (Place) -> Unit = { onIntent(RoomDetailIntent.OnPlaceClick) }
                val onPlaceMoreClick: (Place) -> Unit =
                    { onIntent(RoomDetailIntent.OnPlaceMoreClick(it)) }
                val actionMenu: @Composable (Place) -> Unit = { place ->
                    PlaceActionMenu(
                        expanded = state.menuTargetPlace?.id == place.id,
                        onDismiss = { onIntent(RoomDetailIntent.OnPlaceMoreDismiss) },
                        onShareClick = { onIntent(RoomDetailIntent.OnShareToOtherRoomClick(place)) },
                        onDeleteClick = { onIntent(RoomDetailIntent.OnPlaceDeleteClick(place)) },
                    )
                }
                when (state.viewType) {
                    PlaceViewType.LIST ->
                        PlaceCardList(
                            places = state.places,
                            onPlaceClick = onPlaceClick,
                            onPlaceMoreClick = onPlaceMoreClick,
                            actionMenu = actionMenu,
                        )
                    PlaceViewType.CARD ->
                        PlaceCardGrid(
                            places = state.places,
                            onPlaceClick = onPlaceClick,
                            onPlaceMoreClick = onPlaceMoreClick,
                            actionMenu = actionMenu,
                        )
                }
            }
        },
    )

    if (state.showRoomSelectSheet) {
        RoomDetailRoomSelectOverlay(
            place = state.placeToShare,
            myRooms = state.myRooms,
            onIntent = onIntent,
        )
    }

    if (state.placeToDelete != null) {
        PlaceDeleteConfirmDialog(
            onConfirm = { onIntent(RoomDetailIntent.OnPlaceDeleteConfirm) },
            onCancel = { onIntent(RoomDetailIntent.OnPlaceDeleteCancel) },
        )
    }

    if (state.showInviteSheet) {
        RoomDetailInviteOverlay(
            room = state.room,
            inviteCode = state.inviteCode,
            roomMembers = state.roomMembers,
            onIntent = onIntent,
        )
    }

    when (state.leaveDialogState) {
        LeaveDialogState.ConfirmMember ->
            RoomLeaveConfirmDialog(
                onConfirm = { onIntent(RoomDetailIntent.OnLeaveConfirm) },
                onCancel = { onIntent(RoomDetailIntent.OnLeaveCancel) },
            )

        LeaveDialogState.ConfirmOwnerSingle ->
            RoomOwnerLeaveDialog(
                leaveDialogState = state.leaveDialogState,
                roomMembers = state.roomMembers,
                selectedMemberId = state.selectedDelegateMemberId,
                onMemberSelected = { onIntent(RoomDetailIntent.OnOwnerDelegateSelected(it)) },
                onConfirm = { onIntent(RoomDetailIntent.OnLeaveConfirm) },
                onCancel = { onIntent(RoomDetailIntent.OnLeaveCancel) },
            )

        LeaveDialogState.DelegateOwner ->
            RoomOwnerLeaveDialog(
                leaveDialogState = state.leaveDialogState,
                roomMembers = state.roomMembers,
                selectedMemberId = state.selectedDelegateMemberId,
                onMemberSelected = { onIntent(RoomDetailIntent.OnOwnerDelegateSelected(it)) },
                onConfirm = { onIntent(RoomDetailIntent.OnOwnerDelegateConfirm) },
                onCancel = { onIntent(RoomDetailIntent.OnLeaveCancel) },
            )

        LeaveDialogState.None -> Unit
    }
}

/**
 * 초대 시트([RoomInviteSheet]) 오버레이 — [RoomDetailRoomSelectOverlay]와 같은 배경 스크림 + 하단 정렬
 * 패턴(T048)을 재사용한다.
 */
@Composable
private fun RoomDetailInviteOverlay(
    room: Room?,
    inviteCode: String?,
    roomMembers: ImmutableList<RoomMember>,
    onIntent: (RoomDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val noRippleInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MinoAndroidTheme.colors.materialDimmer)
            .rippleSingleClickable(onClick = { onIntent(RoomDetailIntent.OnInviteSheetDismiss) }),
    ) {
        RoomInviteSheet(
            room = room,
            inviteCode = inviteCode,
            roomMembers = roomMembers,
            onDismiss = { onIntent(RoomDetailIntent.OnInviteSheetDismiss) },
            onInviteClick = { onIntent(RoomDetailIntent.OnInviteConfirmClick) },
            onCopyLinkClick = { onIntent(RoomDetailIntent.OnCopyInviteLinkClick) },
            // 시트 콘텐츠 자체의 빈 여백을 눌러도 바깥 스크림 클릭으로 잘못 전달돼 닫히지 않도록
            // 클릭 이벤트를 여기서 소비한다(별도 시각 효과 없음, indication = null) —
            // RoomDetailRoomSelectOverlay와 같은 이유(T048).
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clickable(interactionSource = noRippleInteractionSource, indication = null, onClick = {}),
        )
    }
}

/**
 * "다른 방에 공유" 시트([RoomSelectSheet])를 화면 하단에 덮는 오버레이.
 *
 * [alreadySavedRoomIds]는 항상 빈 집합으로 둔다 — `Place`·`Room` 어느 도메인 모델에도 "이 장소가 이
 * 방에 이미 저장돼 있는지"를 판정할 수 있는 필드가 없다(`Place`는 room 소속 관계를 갖지 않고, `Room`은
 * 장소를 담지 않는다, `core/domain/model/Place.kt`·`Room.kt` 확인). 추후 Place-Room 소속 관계 데이터가
 * 추가되면 이 자리를 채워야 한다.
 *
 * [selectedRoomIds]는 이 오버레이가 열려 있는 동안만 유효한 화면 로컬 다중 선택 상태다 — `RoomDetailUiState`는
 * 선택 중인 방 목록을 들고 있지 않는다(공유 확정 시점에만 `RoomDetailIntent.OnRoomSelectConfirm`으로
 * ViewModel에 전달).
 */
@Composable
private fun RoomDetailRoomSelectOverlay(
    place: Place?,
    myRooms: ImmutableList<Room>,
    onIntent: (RoomDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedRoomIds by remember { mutableStateOf<ImmutableSet<String>>(persistentSetOf()) }
    val alreadySavedRoomIds: ImmutableSet<String> = persistentSetOf()
    val noRippleInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MinoAndroidTheme.colors.materialDimmer)
            .rippleSingleClickable(onClick = { onIntent(RoomDetailIntent.OnRoomSelectDismiss) }),
    ) {
        RoomSelectSheet(
            place = place,
            rooms = myRooms,
            alreadySavedRoomIds = alreadySavedRoomIds,
            selectedRoomIds = selectedRoomIds,
            onRoomToggle = { roomId ->
                selectedRoomIds = if (roomId in selectedRoomIds) {
                    (selectedRoomIds - roomId).toImmutableSet()
                } else {
                    (selectedRoomIds + roomId).toImmutableSet()
                }
            },
            onCreateRoomClick = { onIntent(RoomDetailIntent.OnShareCreateRoomClick) },
            onShareClick = {
                onIntent(RoomDetailIntent.OnRoomSelectConfirm(selectedRoomIds.toImmutableList()))
            },
            onDismiss = { onIntent(RoomDetailIntent.OnRoomSelectDismiss) },
            // 시트 콘텐츠 자체의 빈 여백을 눌러도 바깥 스크림 클릭으로 잘못 전달돼 닫히지 않도록
            // 클릭 이벤트를 여기서 소비한다(별도 시각 효과 없음, indication = null).
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clickable(interactionSource = noRippleInteractionSource, indication = null, onClick = {}),
        )
    }
}

/**
 * 방에 저장된 장소가 0개일 때 `Half`/`Full` 시트에 보여줄 최소 안내 문구([EC-001]).
 *
 * [TBD] 정확한 빈 상태 문구·디자인은 Figma 대조가 필요하다 — 이 세션에서 해당 노드를 확보하지 못해
 * 최소 텍스트로 구현했다.
 */
@Composable
private fun RoomDetailEmptyPlaces(modifier: Modifier = Modifier) {
    Text(
        text = "저장된 장소가 없어요",
        modifier = modifier
            .fillMaxWidth()
            .padding(RoomDetailEmptyPlacesTokens.Padding),
        textAlign = TextAlign.Center,
        color = MinoAndroidTheme.colors.labelAlternative,
        style = MinoAndroidTheme.typography.body1NormalRegular,
    )
}

private object RoomDetailEmptyPlacesTokens {
    val Padding = 20.dp
}

/**
 * [research.md D10] 현재 위치 버튼. `RoomListScreen`의 `RoomListCurrentLocationButton`과 같은 스펙·같은
 * 위치 규칙(시트 상단에서 [GapAboveSheet]만큼 띄움) — room-list와 동일하게 보여야 한다는 요구에 맞춰
 * 그대로 재사용한다. `sheetLevel == FULL`이면 호출부가 아예 그리지 않는다(UX-002).
 */
private object RoomDetailCurrentLocationButtonTokens {
    val Size = 36.dp
    val IconSize = 20.dp
    val GapAboveSheet = 20.dp
}

@Composable
private fun RoomDetailCurrentLocationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(RoomDetailCurrentLocationButtonTokens.Size)
            .dropShadow(shape = CircleShape, shadow = MinoAndroidTheme.shadows.normalMedium)
            .clip(CircleShape)
            .background(MinoAndroidTheme.colors.staticWhite)
            .rippleSingleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(RoomDetailCurrentLocationButtonTokens.IconSize),
            imageVector = MinoIcons.MyLocation,
            contentDescription = null,
            tint = MinoAndroidTheme.colors.labelAlternative,
        )
    }
}
