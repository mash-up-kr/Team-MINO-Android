package team.mino.feature.room.main.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.designsystem.component.category.CategorySize
import team.mino.core.designsystem.component.category.MinoCategory
import team.mino.core.designsystem.component.chip.ChipSize
import team.mino.core.designsystem.component.chip.ChipVariant
import team.mino.core.designsystem.component.chip.MinoChip
import team.mino.core.designsystem.component.menu.MinoMenu
import team.mino.core.designsystem.component.menu.MinoMenuItem
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.CaretDown
import team.mino.core.designsystem.foundation.icons.icons.MyLocation
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.shadow.dropShadow
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.model.RoomListSortOption
import team.mino.feature.room.main.component.RoomListBottomSheet
import team.mino.feature.room.main.component.RoomListMap
import team.mino.feature.room.main.component.RoomListRoomCardList
import team.mino.feature.room.main.component.RoomNudgeAutoSheet
import team.mino.feature.room.main.component.bottomSheetHeightOrNull
import team.mino.feature.room.main.model.BottomSheetLevel
import team.mino.feature.room.main.vm.RoomListIntent
import team.mino.feature.room.main.vm.RoomListUiState
import kotlin.math.roundToInt

/**
 * 방 리스트 탭 화면. `Peek`/`Half`/`Full` 3단계를 [RoomListUiState.sheetLevel] 상태 하나로 그린다.
 *
 * @param detailContent `null`이 아니면 방 상세를 보는 중이다([RoomListUiState.selectedRoomId] 참고) —
 * 이 컴포저블이 방 상세의 컨트롤·바텀시트·오버레이를 그린다. 지도([RoomListMap])는 이 분기와 무관하게
 * 항상 이 함수가 한 번만 그려서, 리스트↔상세 전환에도 같은 컴포지션에 남아 카메라가 리셋되지 않는다
 * (`RoomListRoute` KDoc 참고).
 * @param detailSheetLevel `detailContent`가 그리는 방 상세 바텀시트의 현재 단계. 지도가 상태바 밑으로
 * 새어 나와야 하는지(`mapBleed`)는 "현재 활성화된 시트"가 `Full`인지로 판정해야 한다 — 상세 모드인데도
 * 리스트의 `state.sheetLevel`만 보면 상세가 `Full`이라 지도가 안 보여야 할 때도 지도가 계속 새어 나와
 * 상태바 영역에 지도 색이 비치고 그 아래서 흰 시트가 시작되는 이음매가 생긴다(실기기 확인된 결함).
 */
@Composable
internal fun RoomListScreen(
    state: RoomListUiState,
    onIntent: (RoomListIntent) -> Unit,
    modifier: Modifier = Modifier,
    detailSheetLevel: BottomSheetLevel? = null,
    detailContent: (@Composable BoxScope.() -> Unit)? = null,
) {
    val isDetailMode = detailContent != null
    val listIsMapControlVisible = state.sheetLevel != BottomSheetLevel.FULL
    val isMapControlVisible = if (isDetailMode) {
        detailSheetLevel != BottomSheetLevel.FULL
    } else {
        listIsMapControlVisible
    }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    // 트리거 위치를 이 Box 기준 좌표(root 기준 아님)로 계산하려고 두 LayoutCoordinates를 그대로
    // 들고 있는다 — RoomListSortMenu의 KDoc 「루트 좌표계 이중 오프셋」 참조.
    var rootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var sortTriggerCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val density = LocalDensity.current
    // SortMenuGap은 상수라 매 배치마다 다시 px로 환산할 필요가 없다.
    val sortMenuGapPx = remember(density) { with(density) { SortMenuGap.toPx() } }

    // Figma(node 2661-157242 Peek/2661-157338 Half)는 지도가 상태바 뒤까지 edge-to-edge로 깔리고
    // 상태바는 그 위에 투명 오버레이로 얹힌다 — `Full`(003-1-3)은 반대로 상태바 영역이 일반 inset이다.
    // `MainShell`의 `MinoScaffold`가 상태바 높이만큼 top padding을 이미 소비해서 이 컴포저블이 받는
    // 영역은 항상 상태바 아래에서 시작한다 — 그래서 지도만 [mapBleed]만큼 위로 끌어올려 그 padding을
    // 뚫고 진짜 화면 최상단부터 채운다(아래 `Modifier.layout` — 측정 높이 자체를 늘려야 지도 하단에
    // 빈틈이 안 남는다). 지도 위 다른 콘텐츠(정렬 컨트롤 등)는 원래 자리(이미 올바르게 inset된
    // 위치) 그대로 둔다.
    val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val mapBleed = remember(isMapControlVisible, statusBarInset) {
        if (isMapControlVisible) statusBarInset else 0.dp
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { rootCoordinates = it },
    ) {
        RoomListMap(
            mapCenter = state.mapCenter,
            mapCenterRequestId = state.mapCenterRequestId,
            mapPins = state.mapPins,
            modifier = Modifier
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val bleedPx = mapBleed.roundToPx()
                    val targetHeight = if (constraints.hasBoundedHeight) {
                        constraints.maxHeight + bleedPx
                    } else {
                        constraints.maxHeight
                    }
                    val placeable = measurable.measure(
                        constraints.copy(minHeight = targetHeight, maxHeight = targetHeight),
                    )
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, -bleedPx)
                    }
                },
        )

        if (isDetailMode) {
            detailContent()
        } else {
            if (isMapControlVisible) {
                RoomListMapControls(
                    mapMarkerSort = state.mapMarkerSort,
                    categoryFilter = state.categoryFilter,
                    onSortTriggerClick = { sortMenuExpanded = !sortMenuExpanded },
                    onSortTriggerPositioned = { sortTriggerCoordinates = it },
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
                Column(modifier = Modifier.fillMaxHeight()) {
                    // Figma 003-1-2(half)·003-2-3(full) 대조 — 정렬 칩은 Full 전용이 아니라 Half에서도
                    // 보인다(FR-005). Peek는 헤더만 그리므로 content() 자체가 호출되지 않아 자동으로 숨는다.
                    RoomListSortChipRow(
                        selected = state.roomListSort,
                        onSelected = { onIntent(RoomListIntent.OnRoomListSortSelected(it)) },
                    )
                    RoomListRoomCardList(
                        personalRoom = state.personalRoom,
                        groupRooms = state.groupRooms,
                        // Figma 003-1-3(full_개인방만 존재) — 넛지는 개인방 카드 바로 아래, 시트 남은
                        // 높이를 채우며 뜬다(화면 하단에 별도로 띄우면 개인방 카드와 넛지 사이가 붕 뜬다).
                        // Peek·Half에서도 groupRooms.isEmpty()는 true일 수 있지만 Full에서만 보인다.
                        showNudge = state.showNudge && state.sheetLevel == BottomSheetLevel.FULL,
                        onRoomCardClick = { onIntent(RoomListIntent.OnRoomCardClick(it)) },
                        onNudgeCreateClick = { onIntent(RoomListIntent.OnNudgeCreateClick) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // [FR-008] 공동방 0개 상태로 탭에 진입할 때마다 자동으로 뜨는 딤 팝업. `Full`에서는 인라인
            // 넛지 카드([RoomListRoomCardList]의 showNudge)가 이미 같은 CTA를 보여주므로
            // [RoomListUiState.isNudgeSheetVisible]가 중복 노출을 피한다.
            if (state.isNudgeSheetVisible) {
                RoomNudgeAutoSheet(
                    onCreateClick = { onIntent(RoomListIntent.OnNudgeCreateClick) },
                    onDismissRequest = { onIntent(RoomListIntent.OnNudgeDismissClick) },
                )
            }

            if (isMapControlVisible && sortMenuExpanded) {
                RoomListSortMenu(
                    selected = state.mapMarkerSort,
                    onSelected = {
                        onIntent(RoomListIntent.OnMapSortSelected(it))
                        sortMenuExpanded = false
                    },
                    modifier = Modifier.offset {
                        val root = rootCoordinates
                        val trigger = sortTriggerCoordinates
                        if (root == null || trigger == null) {
                            IntOffset.Zero
                        } else {
                            val position = root.localPositionOf(
                                sourceCoordinates = trigger,
                                relativeToSource = Offset(0f, trigger.size.height + sortMenuGapPx),
                            )
                            IntOffset(position.x.roundToInt(), position.y.roundToInt())
                        }
                    },
                )
            }
        }
    }
}

/**
 * [FR-005] 정렬 칩(전체/최근 저장 순/코멘트 순). `:core:design-system`의 [MinoChip]을 재사용한다.
 *
 * Figma `Category/Resource/Chip/Normal/Small`(node 2661-157350) 실측값 — radius 8dp·패딩
 * 8h/6v·`Label 1/Normal - Medium`(14sp) 모두 [ChipSize.Small]과 일치한다. `Medium`(radius 10dp)을
 * 쓰면 모서리·패딩이 실제보다 커 보인다.
 *
 * `variant`도 선택 여부에 따라 갈린다 — Figma 렌더링을 직접 대조해보면 선택된 칩(`Active=True`)만
 * 검정 배경·흰 글자([ChipVariant.Solid])이고, 나머지는 흰 배경에 옅은 테두리·회색 글자
 * ([ChipVariant.Outlined])다. 전부 `Solid`로 두면 비선택 칩에 테두리 없이 회색 틴트만 채워져
 * Figma와 달라진다.
 */
@Composable
private fun RoomListSortChipRow(
    selected: RoomListSortOption,
    onSelected: (RoomListSortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(RoomListSortOption.entries.toList()) { option ->
            val active = option == selected
            MinoChip(
                text = option.label(),
                onClick = { onSelected(option) },
                size = ChipSize.Small,
                variant = if (active) ChipVariant.Solid else ChipVariant.Outlined,
                active = active,
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
    onSortTriggerClick: () -> Unit,
    onSortTriggerPositioned: (LayoutCoordinates) -> Unit,
    onCategoryFilterSelected: (PlaceCategoryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoomListSortTrigger(
            selected = mapMarkerSort,
            onClick = onSortTriggerClick,
            onPositioned = onSortTriggerPositioned,
        )

        MinoCategory(
            items = PlaceCategoryFilter.entries.map { it.label() }.toImmutableList(),
            selectedIndex = PlaceCategoryFilter.entries.indexOf(categoryFilter),
            onItemClick = { index -> onCategoryFilterSelected(PlaceCategoryFilter.entries[index]) },
            // Figma(node 2542-125408) 칩 실측값(padding 12h/9v, radius 10px)은 CategorySize.XLarge가
            // 매핑하는 ChipSize.Large와 일치한다. 기본값 Medium(→ChipSize.Small, radius 8dp)을 그대로
            // 두면 실제보다 작게 나온다.
            size = CategorySize.XLarge,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Figma(node 2542-125408) 실측값 — 정렬 드롭다운 패널 고정 폭. */
private val SortDropdownMenuWidth = 140.dp

/** Figma(node 2542-125408) 실측값 — 트리거 아래에서 메뉴 패널까지의 세로 간격. */
private val SortMenuGap = 8.dp

/**
 * Figma `Button/Button`(node 2542-125408, componentId 1247:76920) 실측값 — 정렬 드롭다운 트리거.
 * 이전엔 `CircleShape`(완전 원형)로 그려서 실제보다 훨씬 둥글게 나왔다 — 실제론 10dp 라운드에 1px
 * 테두리가 있는 사각형이다.
 */
private val SortDropdownTriggerShape = RoundedCornerShape(10.dp)
private val SortDropdownTriggerBorderWidth = 1.dp

/**
 * [FR-011] 정렬 드롭다운 트리거. 펼쳤을 때 메뉴 패널([RoomListSortMenu])은 이 컴포저블이 그리지
 * 않는다 — [RoomListScreen]이 [onPositioned]로 받은 [LayoutCoordinates]로 상대 위치를 계산해
 * 화면 최상단(root [Box])에 직접 그린다.
 *
 * Figma는 이 메뉴를 `position: absolute`로 띄운다(node 2542-125408) — 트리거·카테고리 칩이 있는
 * `Row` 레이아웃과 무관하게 지도 위에 얹힌다는 뜻이다. 세 가지를 실기기에서 확인했다:
 * 1. 트리거 [Column] 아래에 메뉴를 이어 그리면, 펼쳤을 때 이 컴포저블의 측정 높이 자체가 늘어나
 *    상위 `Row`(→ [RoomListMapControls])가 옆 카테고리 칩을 `CenterVertically`로 그 늘어난 높이
 *    가운데로 밀어버린다.
 * 2. [Popup]으로 띄우면 위 문제는 없지만, 별도 Android 윈도우라 [RoomListMap]의 Google Maps
 *    `SurfaceView`(별도 하드웨어 레이어로 합성됨) 뒤에 가려진다 — SurfaceView가 시스템 팝업
 *    윈도우와 정상적인 Z-order로 합성되지 않는 잘 알려진 Android 제약([MinoChipRoom]의
 *    `SortDropdown`은 지도 없는 화면이라 이 문제가 없다).
 * 3. `coordinates.positionInRoot()`는 **컴포지션 전체의 최상위 루트** 기준 좌표다 — `RoomListScreen`의
 *    [Box]가 그 진짜 루트보다 아래(예: 셸의 padding/inset)에 있으면, 그 조상 오프셋이 이 값에 이미
 *    포함된 채로 다시 [RoomListScreen]의 [Box] 안에 배치되면서 **두 번 더해져** 실기기에서 간격이
 *    Figma보다 훨씬 크게 나왔다(재현·확인됨). 그래서 진짜 루트가 아니라 [RoomListScreen]의 [Box] —
 *    메뉴와 같은 좌표계를 공유하는 가장 가까운 조상 — 를 기준으로 [LayoutCoordinates.localPositionOf]로
 *    상대 위치를 구해야 한다. 이 컴포저블은 [LayoutCoordinates] 원본만 위로 올려 보내고, 실제 상대
 *    위치 계산은 그 조상 좌표를 함께 쥔 [RoomListScreen]이 한다.
 */
@Composable
private fun RoomListSortTrigger(
    selected: MapMarkerSortOption,
    onClick: () -> Unit,
    onPositioned: (LayoutCoordinates) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .onGloballyPositioned(onPositioned)
            .border(
                width = SortDropdownTriggerBorderWidth,
                color = MinoAndroidTheme.colors.lineNormalNeutral,
                shape = SortDropdownTriggerShape,
            ).clip(SortDropdownTriggerShape)
            .background(MinoAndroidTheme.colors.backgroundNormalNormal)
            .rippleSingleClickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = selected.label(),
            color = MinoAndroidTheme.colors.labelNormal,
            style = MinoAndroidTheme.typography.body2NormalMedium,
        )
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = MinoIcons.CaretDown,
            contentDescription = null,
            tint = MinoAndroidTheme.colors.labelNormal,
        )
    }
}

/** [RoomListSortTrigger] 펼침 패널. [RoomListScreen]의 root [Box]에서 오프셋으로 위치를 맞춰 그린다. */
@Composable
private fun RoomListSortMenu(
    selected: MapMarkerSortOption,
    onSelected: (MapMarkerSortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    MinoMenu(modifier = modifier.width(SortDropdownMenuWidth)) {
        MapMarkerSortOption.entries.forEach { option ->
            MinoMenuItem(
                text = option.label(),
                active = option == selected,
                onClick = { onSelected(option) },
            )
        }
    }
}

/**
 * [research.md D10] 현재 위치 버튼. `sheetLevel == FULL`이면 호출부가 아예 그리지 않는다(UX-002).
 * Figma(003-1-2/003-2-1/003-2-2, node 3276-210006)는 시트 상단에서 [GapAboveSheet]만큼 띄운
 * 위치에, 흰 배경 + `Shadow/Normal/Medium`(2겹 그림자)으로 그린다 — 화면 하단 고정이 아니라
 * [RoomListScreen]이 시트 높이만큼 bottom padding을 더해 맞춘다. 단일 레이어 `Modifier.shadow`
 * (elevation)로는 이 2겹 그림자를 못 내서 [dropShadow] + [MinoAndroidTheme.shadows]를 쓴다.
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
            .dropShadow(shape = CircleShape, shadow = MinoAndroidTheme.shadows.normalMedium)
            .clip(CircleShape)
            .background(MinoAndroidTheme.colors.staticWhite)
            .rippleSingleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(RoomListCurrentLocationButtonTokens.IconSize),
            imageVector = MinoIcons.MyLocation,
            contentDescription = null,
            tint = MinoAndroidTheme.colors.labelAlternative,
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
