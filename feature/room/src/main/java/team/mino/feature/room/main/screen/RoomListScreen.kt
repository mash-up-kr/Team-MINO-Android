package team.mino.feature.room.main.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.designsystem.component.category.MinoCategory
import team.mino.core.designsystem.component.chip.ChipSize
import team.mino.core.designsystem.component.chip.MinoChip
import team.mino.core.designsystem.component.menu.MinoMenu
import team.mino.core.designsystem.component.menu.MinoMenuItem
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.CaretDown
import team.mino.core.designsystem.foundation.icons.icons.Location
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.model.RoomListSortOption
import team.mino.feature.room.main.component.RoomListBottomSheet
import team.mino.feature.room.main.component.RoomListMap
import team.mino.feature.room.main.component.RoomListRoomCardList
import team.mino.feature.room.main.component.bottomSheetHeightOrNull
import team.mino.feature.room.main.component.RoomNudgeSheet
import team.mino.feature.room.main.model.BottomSheetLevel
import team.mino.feature.room.main.vm.RoomListIntent
import team.mino.feature.room.main.vm.RoomListUiState

/**
 * 방 리스트 탭 화면. `Peek`/`Half`/`Full` 3단계를 [RoomListUiState.sheetLevel] 상태 하나로 그린다.
 */
@Composable
internal fun RoomListScreen(
    state: RoomListUiState,
    onIntent: (RoomListIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMapControlVisible = state.sheetLevel != BottomSheetLevel.FULL

    Box(modifier = modifier.fillMaxSize()) {
        RoomListMap(
            mapCenter = state.mapCenter,
            personalRoom = state.personalRoom,
            groupRooms = state.groupRooms,
            modifier = Modifier.fillMaxSize(),
        )

        if (isMapControlVisible) {
            RoomListMapControls(
                mapMarkerSort = state.mapMarkerSort,
                categoryFilter = state.categoryFilter,
                onMapSortSelected = { onIntent(RoomListIntent.OnMapSortSelected(it)) },
                onCategoryFilterSelected = { onIntent(RoomListIntent.OnCategoryFilterSelected(it)) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 12.dp),
            )

            val sheetHeight = bottomSheetHeightOrNull(state.sheetLevel, state.groupRooms.size) ?: 0.dp
            RoomListCurrentLocationButton(
                onClick = { onIntent(RoomListIntent.OnCurrentLocationClick) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = sheetHeight + RoomListCurrentLocationButtonTokens.GapAboveSheet),
            )
        }

        RoomListBottomSheet(
            sheetLevel = state.sheetLevel,
            groupRoomCount = state.groupRooms.size,
            onDraggedUp = { onIntent(RoomListIntent.OnSheetDraggedUp) },
            onDraggedDown = { onIntent(RoomListIntent.OnSheetDraggedDown) },
            onAddRoomClick = { onIntent(RoomListIntent.OnAddRoomClick) },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column {
                if (state.sheetLevel == BottomSheetLevel.FULL) {
                    RoomListSortChipRow(
                        selected = state.roomListSort,
                        onSelected = { onIntent(RoomListIntent.OnRoomListSortSelected(it)) },
                    )
                }
                RoomListRoomCardList(
                    personalRoom = state.personalRoom,
                    groupRooms = state.groupRooms,
                    showGhostCard = state.showGhostCard,
                    onRoomCardClick = { onIntent(RoomListIntent.OnRoomCardClick(it)) },
                    onGhostCardClick = { onIntent(RoomListIntent.OnGhostCardClick) },
                )
            }
        }

        if (state.showNudge) {
            RoomNudgeSheet(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        color = MinoAndroidTheme.colors.backgroundElevatedNormal,
                        shape = RoomListNudgeOverlayTokens.Shape,
                    ),
                onCreateClick = { onIntent(RoomListIntent.OnNudgeCreateClick) },
                onDismissClick = { onIntent(RoomListIntent.OnNudgeDismissClick) },
            )
        }
    }
}

/**
 * [RoomNudgeSheet] 오버레이 배경 모서리. `spec.md` 유저 플로우 4가 Figma 노드를 달지 않아
 * 인접한 [RoomListBottomSheet]와 같은 모서리 반경([RoomListBottomSheetTokens.Shape])을 따른다.
 */
private object RoomListNudgeOverlayTokens {
    val Shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
}

/** [FR-005] 정렬 칩(전체/최근 저장 순/코멘트 순). `:core:design-system`의 [MinoChip]을 재사용한다. */
@Composable
private fun RoomListSortChipRow(
    selected: RoomListSortOption,
    onSelected: (RoomListSortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(RoomListSortOption.entries.toList()) { option ->
            MinoChip(
                text = option.label(),
                onClick = { onSelected(option) },
                size = ChipSize.Medium,
                active = option == selected,
            )
        }
    }
}

private fun RoomListSortOption.label(): String =
    when (this) {
        RoomListSortOption.ALL -> "전체"
        RoomListSortOption.RECENTLY_SAVED -> "최근 저장 순"
        RoomListSortOption.MOST_COMMENTED -> "코멘트 순"
    }

@Composable
private fun RoomListMapControls(
    mapMarkerSort: MapMarkerSortOption,
    categoryFilter: PlaceCategoryFilter,
    onMapSortSelected: (MapMarkerSortOption) -> Unit,
    onCategoryFilterSelected: (PlaceCategoryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoomListSortDropdown(
            selected = mapMarkerSort,
            onSelected = onMapSortSelected,
        )

        MinoCategory(
            items = PlaceCategoryFilter.entries.map { it.label() }.toImmutableList(),
            selectedIndex = PlaceCategoryFilter.entries.indexOf(categoryFilter),
            onItemClick = { index -> onCategoryFilterSelected(PlaceCategoryFilter.entries[index]) },
            modifier = Modifier.weight(1f),
        )
    }
}

/** [FR-011] 정렬 드롭다운. `:core:design-system`의 [MinoMenu]를 트리거 + 인라인 패널로 조립한다(D11). */
@Composable
private fun RoomListSortDropdown(
    selected: MapMarkerSortOption,
    onSelected: (MapMarkerSortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(MinoAndroidTheme.colors.backgroundElevatedNormal)
                .rippleSingleClickable(onClick = { expanded = !expanded })
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selected.label(),
                color = MinoAndroidTheme.colors.labelNormal,
                style = MinoAndroidTheme.typography.label1NormalMedium,
            )
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = MinoIcons.CaretDown,
                contentDescription = null,
                tint = MinoAndroidTheme.colors.labelNormal,
            )
        }

        if (expanded) {
            MinoMenu(modifier = Modifier.fillMaxWidth()) {
                MapMarkerSortOption.entries.forEach { option ->
                    MinoMenuItem(
                        text = option.label(),
                        active = option == selected,
                        onClick = {
                            onSelected(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

/**
 * [research.md D10] 현재 위치 버튼. `sheetLevel == FULL`이면 호출부가 아예 그리지 않는다(UX-002).
 * Figma(003-1-2/003-2-1/003-2-2)는 시트 상단에서 [GapAboveSheet]만큼 띄운 위치에, 흰 배경 + 그림자로
 * 그린다 — 화면 하단 고정이 아니라 [RoomListScreen]이 시트 높이만큼 bottom padding을 더해 맞춘다.
 */
private object RoomListCurrentLocationButtonTokens {
    val Size = 36.dp
    val IconSize = 20.dp
    val GapAboveSheet = 20.dp
}

@Composable
private fun RoomListCurrentLocationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(RoomListCurrentLocationButtonTokens.Size)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(MinoAndroidTheme.colors.staticWhite)
            .rippleSingleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(RoomListCurrentLocationButtonTokens.IconSize),
            imageVector = MinoIcons.Location,
            contentDescription = null,
            tint = MinoAndroidTheme.colors.labelNormal,
        )
    }
}

private fun MapMarkerSortOption.label(): String =
    when (this) {
        MapMarkerSortOption.ALL -> "전체"
        MapMarkerSortOption.GGUK_PICK -> "꾹 Pick"
        MapMarkerSortOption.LATEST -> "최신순"
        MapMarkerSortOption.NEARBY -> "거리순"
        MapMarkerSortOption.MOST_COMMENTED -> "코멘트순"
    }

private fun PlaceCategoryFilter.label(): String =
    when (this) {
        PlaceCategoryFilter.ALL -> "전체"
        PlaceCategoryFilter.CAFE -> "카페"
        PlaceCategoryFilter.RESTAURANT -> "음식점"
    }
