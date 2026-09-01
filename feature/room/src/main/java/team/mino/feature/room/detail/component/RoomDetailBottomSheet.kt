package team.mino.feature.room.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.designsystem.component.category.CategorySize
import team.mino.core.designsystem.component.category.MinoCategory
import team.mino.core.designsystem.component.menu.AnchoredDropdownPositionProvider
import team.mino.core.designsystem.component.menu.MinoMenu
import team.mino.core.designsystem.component.menu.MinoMenuItem
import team.mino.core.designsystem.component.roomcard.MinoHeaderRoom
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.CaretDown
import team.mino.core.designsystem.foundation.icons.icons.Close
import team.mino.core.designsystem.foundation.icons.icons.MoreVertical
import team.mino.core.designsystem.foundation.icons.icons.Thumbnail
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.domain.model.MapMarkerSortOption
import team.mino.core.domain.model.PlaceCategoryFilter
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomMemberSummary
import team.mino.feature.room.detail.model.PlaceViewType
import team.mino.feature.room.main.model.BottomSheetLevel
import team.mino.feature.room.main.model.image
import kotlin.math.roundToInt
import team.mino.core.designsystem.foundation.icons.icons.List as ListPlaceIcon

/**
 * 방 상세 시트 [FR-002]·[FR-001] 높이 고정값. `room-list`의 [BottomSheetLevel]과 같은 3단 전이를
 * 쓰되(contracts/room-detail-main-contract.md "분기 규칙 — 시트 드래그 전이"), `Half`는 그룹방 수와
 * 무관하게 항상 256dp로 고정이다 — room-detail은 방 하나의 상세이지 방 개수를 세는 화면이 아니다.
 */
private object RoomDetailBottomSheetTokens {
    // Figma `2542:125409`(Peek) 실측 — 시트 프레임이 y=604에서 시작해 홈 바(y=778) 바로 위에서 끝난다
    // (778-604=174dp). 기존 88dp는 핸들+헤더 줄(아바타/더보기/닫기)+`Header_Room`(제목·메모·장소 수)을
    // 다 담기엔 너무 작아 헤더 내용이 시트 높이에 잘려 안 보이는 결함이 있었다.
    val PeekHeight = 174.dp

    // Figma `2542:125383`(Half) 실측 — 시트 프레임이 y=368에서 시작해 홈 바(y=778) 바로 위에서 끝난다
    // (778-368=410dp). 기존 256dp는 근거 없는 추정값이었다.
    val HalfHeight = 410.dp
    val Shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val FullShape = RoundedCornerShape(0.dp)
    val HandleWidth = 36.dp
    val HandleHeight = 4.dp
    val DragThreshold = 24.dp
    val HeaderRowPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    val HeaderRowSpacing = 8.dp

    // `RoomListBottomSheet`의 `Button/Icon/Outlined`(40dp 원형 아웃라인)과 같은 스펙 — More Vertical(⋮)·
    // Close(X) 버튼이 이 스펙을 공유한다(Peek/Half/Full 공통, node 2542:125409/2542:125383 대조).
    val HeaderIconButtonSize = 40.dp
    val HeaderIconButtonIconSize = 20.dp
    val HeaderIconButtonBorderWidth = 1.dp
}

/** [Room.placeCount]를 헤더 표시 문구로 포맷한다. 999 초과는 "999+개"로 클램핑한다([FR-001]). */
internal fun formatPlaceCountText(placeCount: Int): String =
    if (placeCount > MAX_DISPLAYED_PLACE_COUNT) {
        "$MAX_DISPLAYED_PLACE_COUNT+개"
    } else {
        "${placeCount}개"
    }

private const val MAX_DISPLAYED_PLACE_COUNT = 999

/**
 * `Peek`/`Half`에서 시트가 차지하는 높이(`Full`은 화면을 채워 지도 위 컨트롤 자체가 숨겨지므로 `null`).
 * [RoomListScreen]의 `bottomSheetHeightOrNull`과 같은 이유 — 현재 위치 버튼이 이 높이 + 여백만큼 위에 뜨도록
 * [RoomDetailScreen]과 공유한다.
 */
internal fun roomDetailBottomSheetHeightOrNull(sheetLevel: BottomSheetLevel): Dp? =
    when (sheetLevel) {
        BottomSheetLevel.PEEK -> RoomDetailBottomSheetTokens.PeekHeight
        BottomSheetLevel.HALF -> RoomDetailBottomSheetTokens.HalfHeight
        BottomSheetLevel.FULL -> null
    }

/**
 * 방 상세 화면 지도 위 3단(`Peek`/`Half`/`Full`) 바텀시트(FR-001~FR-003).
 *
 * `RoomListBottomSheet`와 같은 드래그 제스처·모서리·배경 패턴을 쓰지만, 헤더 구성은 다르다 — 방 상세는
 * `Header_Room`(Figma 2542:125341, `MinoHeaderRoom`)을 그대로 조립하고 그 위에 첫 줄로 멤버 아바타 +
 * More Vertical(⋮)·Close(X)를 붙인다.
 *
 * Figma `2542:125409`(Peek)·`2542:125383`(Half) 대조 결과, 헤더 첫 줄은 좌우 끝 정렬(space-between) 한
 * 줄로 왼쪽에 참여자 아바타 그룹, 오른쪽에 More Vertical(⋮) + Close(X) 40dp 원형 아웃라인 버튼이 함께
 * 있다 — 이전에는 More Vertical이 화면 레벨에 별도로 떠 있고 Close만 시트 핸들 옆에 있어 흩어져 있었다.
 * `Full`(`2542:125333`) 첫 줄(`2542:125338`)도 같은 More Vertical(⋮) + Close(X) 페어라 헤더 줄은 Peek/
 * Half/Full 공통이다(대조 완료). `Full`의 두 번째 줄(`2542:125342`)만 구성이 다르다 — "꾹 Pick" 정렬
 * 드롭다운(왼쪽) + [RoomDetailViewTypeToggle](List/Thumbnail 뷰 전환, `2542:125346`/componentId
 * `2400:143789`·`981:52897` 대조 완료 — 기존 `MinoIcons.ListIcon`/`Thumbnail`과 같은 아이콘)가 오른쪽에
 * 있고, `MinoCategory` 카테고리 탭은 그 아래 별도 줄(`2542:125349`)이다 — [RoomDetailSortAndFilterRow]가
 * `sheetLevel`로 이 구조를 분기한다.
 *
 * @param room `null`이면 아직 로드되지 않은 상태 — 헤더를 그리지 않는다(로딩/에러 처리는 이 스코프 밖).
 * @param content `Half`/`Full`에서 헤더 아래에 그릴 장소 목록 슬롯. `Peek`은 헤더만 그린다.
 * @param onInviteClick [친구 +] 초대 트리거 클릭([RoomInviteSheet] 열기, FR-011). **정확한 Figma 배치는
 *   후속 대조가 필요하다** — 이 트리거 버튼 자체가 spec·contracts 어디에도 배치 노드가 지정돼 있지 않아
 *   ([RoomDetailHeaderRow] 옆) 최소 아이콘 버튼으로 임시 배치했다(T059). [isPersonalRoom]이면 PRD
 *   "개인방 — 초대 불가"에 따라 이 버튼 자체를 안 그린다.
 * @param onMoreMenuClick 더보기[⋮] 클릭 — 이전에는 화면 레벨 플로팅 버튼([RoomDetailScreen])이었으나,
 *   Figma 대조 결과 시트 헤더 줄에 속해 이 컴포저블 안으로 옮겼다. 앵커([RoomMoreMenu] 펼침 위치)도 이
 *   버튼 자리를 기준으로 한다. [isPersonalRoom]이면 PRD "개인방 — 삭제/나가기 금지"에 따라 이 버튼
 *   자체를 안 그린다(눌러도 반응 없는 버튼을 남기지 않는다).
 * @param onCloseClick [X] 닫기 — `room-list`는 `Full`에서만 [X]를 보이지만, 방 상세는 Peek/Half/Full
 *   **모든 단계**에서 항상 노출한다(방 상세 자체를 벗어나는 액션이라 시트 단계와 무관해야 한다는
 *   피드백 반영). [RoomDetailIntent.OnCloseClick]으로 이어진다.
 */
@Composable
internal fun RoomDetailBottomSheet(
    sheetLevel: BottomSheetLevel,
    room: Room?,
    placeCount: Int,
    sortOption: MapMarkerSortOption,
    categoryFilter: PlaceCategoryFilter,
    viewType: PlaceViewType,
    showMoreMenu: Boolean,
    isOwner: Boolean,
    isPersonalRoom: Boolean,
    onDraggedUp: () -> Unit,
    onDraggedDown: () -> Unit,
    onSortSelected: (MapMarkerSortOption) -> Unit,
    onCategoryFilterSelected: (PlaceCategoryFilter) -> Unit,
    onViewTypeSelected: (PlaceViewType) -> Unit,
    onInviteClick: () -> Unit,
    onCloseClick: () -> Unit,
    onMoreMenuClick: () -> Unit,
    onMoreMenuDismiss: () -> Unit,
    onEditRoomClick: () -> Unit,
    onLeaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    val heightModifier = when (sheetLevel) {
        // 고정 높이(height)로 클립하면 메모가 있는 방은 제목+메모+위치 개수 줄이 174dp를 넘어 위치
        // 개수 줄이 잘려 안 보이는 결함이 있었다(실기기 확인, 메모 없는 방은 안 잘림) — 최소 높이만
        // 보장(heightIn)해 내용이 넘치면 시트가 그만큼 커지게 한다.
        BottomSheetLevel.PEEK -> Modifier.heightIn(min = RoomDetailBottomSheetTokens.PeekHeight)
        BottomSheetLevel.HALF -> Modifier.height(RoomDetailBottomSheetTokens.HalfHeight)
        BottomSheetLevel.FULL -> Modifier.fillMaxSize()
    }
    // Full은 화면 전체를 덮어 뒤 배경(지도)이 보이지 않으므로 둥근 모서리를 두지 않는다 — room-list와
    // 같은 판단(RoomListBottomSheet 참고).
    val shape = if (sheetLevel == BottomSheetLevel.FULL) {
        RoomDetailBottomSheetTokens.FullShape
    } else {
        RoomDetailBottomSheetTokens.Shape
    }
    val thresholdPx = with(LocalDensity.current) { RoomDetailBottomSheetTokens.DragThreshold.toPx() }
    // `Half`(256dp)에서 헤더 아래 정렬줄·장소 리스트 영역이 드래그 인식 밖에 있어 위로 스와이프해도
    // Full로 전이되지 않던 결함의 조치 — 시트 본문 전체를 [nestedScroll]에 태워, `content()`의
    // `LazyColumn`/`LazyVerticalGrid`에서 시작한 스크롤도 시트 레벨 전이 신호로 넘어오게 한다.
    // `Full`에서는 반대로 리스트가 스크롤 경계(최상단)에 닿아 더 소비할 스크롤이 없을 때만
    // `onPostScroll`로 넘어온 나머지 드래그를 받아 `Half`로 접는다(리스트 자체 스크롤과 공존).
    val sheetDragNestedScrollConnection = remember(sheetLevel, thresholdPx, onDraggedUp, onDraggedDown) {
        SheetDragNestedScrollConnection(
            sheetLevel = sheetLevel,
            thresholdPx = thresholdPx,
            onDraggedUp = onDraggedUp,
            onDraggedDown = onDraggedDown,
        )
    }
    val headerDragModifier = Modifier.pointerInput(onDraggedUp, onDraggedDown) {
        var accumulatedDrag = 0f
        detectVerticalDragGestures(
            onDragStart = { accumulatedDrag = 0f },
            onVerticalDrag = { change, dragAmount ->
                accumulatedDrag += dragAmount
                change.consume()
            },
            onDragEnd = {
                when {
                    accumulatedDrag <= -thresholdPx -> onDraggedUp()
                    accumulatedDrag >= thresholdPx -> onDraggedDown()
                }
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(heightModifier)
            .clip(shape)
            .background(MinoAndroidTheme.colors.backgroundElevatedNormal)
            .nestedScroll(sheetDragNestedScrollConnection),
    ) {
        // 핸들 + 헤더 줄(아바타/More Vertical/Close)은 스크롤 가능한 자식이 없어 nestedScroll 이벤트를
        // 스스로 만들지 못한다 — 이 영역만 별도로 raw pointerInput 드래그를 그대로 붙인다.
        Column(modifier = Modifier.fillMaxWidth().then(headerDragModifier)) {
            RoomDetailBottomSheetDragHandle()
            if (room != null) {
                RoomDetailHeaderRow(
                    memberSummary = room.memberSummary,
                    showMoreMenu = showMoreMenu,
                    isOwner = isOwner,
                    isPersonalRoom = isPersonalRoom,
                    // Peek는 시트 높이가 174dp뿐이라 더보기 메뉴를 버튼 아래로 펼 공간이 없어 위로 걸치게
                    // 띄운다 — Half/Full은 아래로 펼 공간이 충분해 표준 드롭다운처럼 아래로 편다
                    // ([RoomMoreMenu] KDoc 참고, 실기기 스크린샷으로 확정).
                    moreMenuExpandUpward = sheetLevel == BottomSheetLevel.PEEK,
                    onInviteClick = onInviteClick,
                    onMoreMenuClick = onMoreMenuClick,
                    onMoreMenuDismiss = onMoreMenuDismiss,
                    onEditRoomClick = onEditRoomClick,
                    onLeaveClick = onLeaveClick,
                    onCloseClick = onCloseClick,
                )
                MinoHeaderRoom(
                    title = room.name,
                    // TODO(room-detail 장소 목록 목데이터 — API 연동 확인되면 지운다): `room.placeCount`는
                    //  `RoomMapper.RoomResponse.toDomain()`이 항상 0으로 채우는 자리 표시자라 여기서는
                    //  실제 로드된 [PlaceRemoteDataSourceImpl]의 목데이터 장소 수([placeCount])를 대신
                    //  보여준다. `RoomResponse`가 진짜 장소 수를 내려주게 되면 다시 `room.placeCount`로
                    //  되돌린다.
                    resourceCountText = formatPlaceCountText(placeCount),
                    // Figma Peek(`2542:125419`)·Half(`2542:125383`)·Full(`2542:125359`) 전부 대조 결과
                    // 이 트레일링 썸네일 버튼은 어느 단계에도 없다(위치 아이콘+개수 텍스트만 있음) —
                    // 실기기 스크린샷으로 확인. 세 단계 모두 그리지 않는다.
                    onThumbnailClick = null,
                    memo = room.description.ifEmpty { null },
                )
            }
        }
        if (sheetLevel == BottomSheetLevel.FULL) {
            // Peek/Half의 정렬줄은 Figma 대조 결과 시트 안이 아니라 지도 위 오버레이([RoomDetailMapControls],
            // RoomDetailScreen에서 그림)라 여기서는 Full일 때만 그린다 — 정렬줄도 스크롤 가능한 자식이
            // 없어 위 핸들/헤더와 같은 이유로 raw 드래그를 함께 붙인다.
            Box(modifier = Modifier.fillMaxWidth().then(headerDragModifier)) {
                RoomDetailFullSortRow(
                    sortOption = sortOption,
                    categoryFilter = categoryFilter,
                    viewType = viewType,
                    onSortSelected = onSortSelected,
                    onCategoryFilterSelected = onCategoryFilterSelected,
                    onViewTypeSelected = onViewTypeSelected,
                )
            }
        }
        if (sheetLevel != BottomSheetLevel.PEEK) {
            content()
        }
    }
}

/**
 * [SheetDragNestedScrollConnection] — [RoomDetailBottomSheet] 본문(정렬줄·장소 목록)에서 시작한 스크롤을
 * 시트 레벨 전이 신호로 바꾼다.
 *
 * - `Full`이 아닐 때: 아직 시트가 다 펼쳐지지 않은 상태이므로 리스트가 스스로 스크롤하게 두지 않고
 *   [onPreScroll]에서 세로 스크롤 시도를 전부 가로채 시트 드래그로 취급한다(`Half`에서 리스트가 짧아
 *   스크롤할 내용이 없는 것과도 일치 — 장소 목록을 끝까지 보려면 `Full`로 전이해야 한다는 게 기존 설계).
 * - `Full`일 때: 리스트가 정상적으로 스크롤하게 두고, 리스트가 스크롤 경계(최상단)에 닿아 더 소비할 수
 *   없는 나머지 드래그만 [onPostScroll]에서 받아 `Half`로 접는 신호로 쓴다.
 */
private class SheetDragNestedScrollConnection(
    private val sheetLevel: BottomSheetLevel,
    private val thresholdPx: Float,
    private val onDraggedUp: () -> Unit,
    private val onDraggedDown: () -> Unit,
) : NestedScrollConnection {
    private var accumulatedDrag = 0f

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (sheetLevel == BottomSheetLevel.FULL) return Offset.Zero
        accumulatedDrag += available.y
        fireIfThresholdReached()
        return available
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (sheetLevel != BottomSheetLevel.FULL) return Offset.Zero
        accumulatedDrag += available.y
        fireIfThresholdReached()
        return Offset.Zero
    }

    private fun fireIfThresholdReached() {
        when {
            accumulatedDrag <= -thresholdPx -> {
                onDraggedUp()
                accumulatedDrag = 0f
            }
            accumulatedDrag >= thresholdPx -> {
                onDraggedDown()
                accumulatedDrag = 0f
            }
        }
    }
}

/**
 * 헤더 첫 줄([RoomDetailBottomSheet] KDoc 참고) — 왼쪽 참여자 아바타 그룹(+ 초대 트리거), 오른쪽
 * More Vertical(⋮) + Close(X) 40dp 원형 아웃라인 버튼을 한 줄에 좌우 끝 정렬로 배치한다.
 */
@Composable
private fun RoomDetailHeaderRow(
    memberSummary: RoomMemberSummary,
    showMoreMenu: Boolean,
    isOwner: Boolean,
    isPersonalRoom: Boolean,
    moreMenuExpandUpward: Boolean,
    onInviteClick: () -> Unit,
    onMoreMenuClick: () -> Unit,
    onMoreMenuDismiss: () -> Unit,
    onEditRoomClick: () -> Unit,
    onLeaveClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(RoomDetailBottomSheetTokens.HeaderRowPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoomMemberAvatarStack(
            profileAvatars = memberSummary.visibleAvatars.map { it.image }.toImmutableList(),
            overflowLabel = if (memberSummary.overflowCount > 0) {
                formatMemberOverflowLabel(memberSummary.overflowCount)
            } else {
                null
            },
            // PRD "개인방 — 초대 불가: 다른 멤버를 초대할 수 없으며, 오직 혼자서만 장소를 모으고
            // 관리한다"를 그대로 반영한다 — RoomMemberAvatarStack은 onInviteClick이 null이면 버튼
            // 자체를 숨긴다.
            onInviteClick = if (isPersonalRoom) null else onInviteClick,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(RoomDetailBottomSheetTokens.HeaderRowSpacing)) {
            // PRD "개인방 — 삭제/나가기 금지: 어떤 경우에도 제공하지 않는다"·[SYS-007] Flow C("나가기
            // 메뉴 자체를 노출하지 않는다")를 그대로 반영한다. RoomMoreMenu 자체는 isPersonalRoom이면
            // expanded와 무관하게 아무것도 그리지 않지만(팝업이 안 열림), 그것만으로는 눌러도 반응
            // 없는 [⋮] 버튼이 그대로 남는다 — 버튼 자체를 렌더링하지 않아야 한다(실기기 확인된 결함).
            if (!isPersonalRoom) {
                // 더보기 버튼 자신을 앵커로 삼아야 메뉴가 정확히 이 버튼 기준으로 뜬다 — 예전엔 헤더 줄
                // 전체를 감싼 Box에 Modifier.align으로 위치를 흉내 냈는데, `RoomMoreMenu`의 `Popup`은
                // 레이아웃 Modifier(align/padding/offset)를 전혀 보지 않고 자신을 호출한 컴포저블의 실제
                // 앵커 좌표로만 위치를 정해서 화면 엉뚱한 곳(지도 위쪽)에 뜨는 결함이 있었다.
                Box {
                    RoomDetailHeaderIconButton(
                        icon = MinoIcons.MoreVertical,
                        contentDescription = "더보기",
                        onClick = onMoreMenuClick,
                    )
                    RoomMoreMenu(
                        expanded = showMoreMenu,
                        isOwner = isOwner,
                        isPersonalRoom = isPersonalRoom,
                        expandUpward = moreMenuExpandUpward,
                        onDismiss = onMoreMenuDismiss,
                        onEditRoomClick = onEditRoomClick,
                        onLeaveClick = onLeaveClick,
                    )
                }
            }
            RoomDetailHeaderIconButton(
                icon = MinoIcons.Close,
                contentDescription = "닫기",
                onClick = onCloseClick,
            )
        }
    }
}

/** [RoomDetailHeaderRow]의 More Vertical(⋮)·Close(X) — `RoomListBottomSheet`의 `Button/Icon/Outlined`와 같은 스펙. */
@Composable
private fun RoomDetailHeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(RoomDetailBottomSheetTokens.HeaderIconButtonSize)
            .border(
                width = RoomDetailBottomSheetTokens.HeaderIconButtonBorderWidth,
                color = MinoAndroidTheme.colors.lineNormalNeutral,
                shape = CircleShape,
            ).clip(CircleShape)
            .rippleSingleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(RoomDetailBottomSheetTokens.HeaderIconButtonIconSize),
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MinoAndroidTheme.colors.labelNormal,
        )
    }
}

@Composable
private fun RoomDetailBottomSheetDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp)
                .size(
                    width = RoomDetailBottomSheetTokens.HandleWidth,
                    height = RoomDetailBottomSheetTokens.HandleHeight,
                ).clip(RoundedCornerShape(percent = 50))
                .background(MinoAndroidTheme.colors.lineSolidNeutral),
        )
    }
}

/** [RoomMemberAvatarStack]의 초과 인원 뱃지 문구(Figma `state=more`). 99 초과는 "+99+"로 클램핑한다. */
private fun formatMemberOverflowLabel(overflowCount: Int): String =
    if (overflowCount > MAX_DISPLAYED_OVERFLOW_COUNT) {
        "+$MAX_DISPLAYED_OVERFLOW_COUNT+"
    } else {
        "+$overflowCount"
    }

private const val MAX_DISPLAYED_OVERFLOW_COUNT = 99

/**
 * [RoomDetailSortAndFilterRow]·[RoomDetailSortTrigger]·[RoomDetailSortMenu] 치수 토큰.
 *
 * 커스텀 시각 디자인이 없는 조립 컴포넌트라(`RoomListScreen`의 `RoomListMapControls`/
 * `RoomListSortTrigger`/`RoomListSortMenu`와 동일 판단, research.md D13) 그 화면이 이미 확정한
 * 트리거·메뉴 치수를 그대로 재사용한다.
 */
private object RoomDetailSortAndFilterRowTokens {
    val RowPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 12.dp)
    val RowSpacing = 12.dp
    val TriggerShape = RoundedCornerShape(10.dp)
    val TriggerBorderWidth = 1.dp
    val TriggerIconSize = 16.dp
    val TriggerSpacing = 5.dp
    val MenuWidth = 140.dp
    val MenuGap = 8.dp
}

/**
 * 지도 위 오버레이 정렬 트리거 + [MinoCategory] 카테고리 필터([FR-005]). Figma 대조 결과 Peek
 * (`2542:125409`)·Half(`2542:125383`) 둘 다 이 줄이 바텀시트 안이 아니라 지도 위, 상태바 바로 아래 고정
 * 위치에 뜨는 오버레이다 — `RoomListScreen`의 `RoomListMapControls`와 완전히 같은 위치 규칙(연동은
 * [RoomDetailScreen]이 지도 위 오버레이로 호출). 이전에는 이 줄이 시트 안에 있었고 `Peek`에서는 아예
 * 그리지도 않아 Figma와 어긋났다.
 */
@Composable
internal fun RoomDetailMapControls(
    sortOption: MapMarkerSortOption,
    categoryFilter: PlaceCategoryFilter,
    onSortSelected: (MapMarkerSortOption) -> Unit,
    onCategoryFilterSelected: (PlaceCategoryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var rowCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var triggerCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val density = LocalDensity.current
    val menuGapPx = remember(density) { with(density) { RoomDetailSortAndFilterRowTokens.MenuGap.toPx() } }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { rowCoordinates = it },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RoomDetailSortAndFilterRowTokens.RowSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoomDetailSortTrigger(
                selected = sortOption,
                onClick = { menuExpanded = !menuExpanded },
                onPositioned = { triggerCoordinates = it },
            )
            MinoCategory(
                items = PlaceCategoryLabels,
                selectedIndex = PlaceCategoryFilter.entries.indexOf(categoryFilter),
                onItemClick = { index -> onCategoryFilterSelected(PlaceCategoryFilter.entries[index]) },
                size = CategorySize.XLarge,
                modifier = Modifier.weight(1f),
            )
        }

        if (menuExpanded) {
            RoomDetailSortMenu(
                selected = sortOption,
                onSelected = {
                    onSortSelected(it)
                    menuExpanded = false
                },
                modifier = Modifier.offset {
                    val row = rowCoordinates
                    val trigger = triggerCoordinates
                    if (row == null || trigger == null) {
                        IntOffset.Zero
                    } else {
                        val position = row.localPositionOf(
                            sourceCoordinates = trigger,
                            relativeToSource = Offset(0f, trigger.size.height + menuGapPx),
                        )
                        IntOffset(position.x.roundToInt(), position.y.roundToInt())
                    }
                },
            )
        }
    }
}

/**
 * `Full` 전용 정렬줄 — 정렬 트리거+[RoomDetailViewTypeToggle](List/Thumbnail)이 한 줄(`2542:125342`, 좌우
 * 끝 정렬), 그 아래 [MinoCategory] 카테고리 탭만 별도 줄(`2542:125349`, `Category/Category` 인스턴스) —
 * `Peek`/`Half`([RoomDetailMapControls])와 달리 지도가 아닌 시트 안에 있다(Full은 시트가 화면 전체를
 * 덮어 지도 자체가 안 보임).
 *
 * `Full`(`sheetLevel == FULL`)에서 정렬 트리거를 [Popup] 기반 [RoomDetailFullSortMenu]로 편다 —
 * [RoomDetailMapControls]가 쓰는 offset-Box 방식(`RoomDetailSortMenu`)을 여기서도 그대로 쓰면, 이 줄이
 * hug 높이 부모([RoomDetailBottomSheet]의 시트 `Column`) 안에 있어서 `Box`가 펼침 메뉴 높이까지 포함해
 * 커지는 바람에 아래 장소 목록까지 밀려 내려가는 결함이 있었다(실기기 확인 — `RoomDetailMapControls`는
 * 화면 루트의 `fillMaxSize` `Box` 안이라 이 문제가 없다). `Popup`은 별도 창 레이어라 부모 크기에
 * 관여하지 않는다.
 */
@Composable
private fun RoomDetailFullSortRow(
    sortOption: MapMarkerSortOption,
    categoryFilter: PlaceCategoryFilter,
    viewType: PlaceViewType,
    onSortSelected: (MapMarkerSortOption) -> Unit,
    onCategoryFilterSelected: (PlaceCategoryFilter) -> Unit,
    onViewTypeSelected: (PlaceViewType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RoomDetailSortAndFilterRowTokens.RowPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                RoomDetailSortTrigger(
                    selected = sortOption,
                    onClick = { menuExpanded = !menuExpanded },
                    onPositioned = {},
                )
                RoomDetailFullSortMenu(
                    expanded = menuExpanded,
                    selected = sortOption,
                    onSelected = {
                        onSortSelected(it)
                        menuExpanded = false
                    },
                    onDismiss = { menuExpanded = false },
                )
            }
            RoomDetailViewTypeToggle(selected = viewType, onSelected = onViewTypeSelected)
        }
        MinoCategory(
            items = PlaceCategoryLabels,
            selectedIndex = PlaceCategoryFilter.entries.indexOf(categoryFilter),
            onItemClick = { index -> onCategoryFilterSelected(PlaceCategoryFilter.entries[index]) },
            size = CategorySize.XLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(RoomDetailSortAndFilterRowTokens.RowPadding),
        )
    }
}

/** [RoomDetailFullSortRow]의 [Popup] 기반 정렬 펼침 패널 — 트리거 왼쪽 끝에 맞춰 아래로 편다. */
@Composable
private fun RoomDetailFullSortMenu(
    expanded: Boolean,
    selected: MapMarkerSortOption,
    onSelected: (MapMarkerSortOption) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!expanded) return

    val density = LocalDensity.current
    val positionProvider = remember(density) { AnchoredDropdownPositionProvider(density, alignEnd = false) }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        MinoMenu(modifier = modifier.width(RoomDetailSortAndFilterRowTokens.MenuWidth)) {
            MapMarkerSortOption.entries.forEach { option ->
                MinoMenuItem(
                    text = option.label(),
                    active = option == selected,
                    onClick = { onSelected(option) },
                )
            }
        }
    }
}

/**
 * [FR-007] 장소 목록 뷰 타입 토글(리스트형/카드형) — Figma `2542:125346`(Full) 대조 결과
 * componentId `2400:143789`("Icon/Normal/List")·`981:52897`("Icon/Normal/Thumbnail")로 확인됐고, 이는
 * `MinoIcons.ListIcon`/`MinoIcons.Thumbnail`과 같은 아이콘이다(새 아이콘 추가 불필요).
 *
 * 활성 표시는 회색 배경 박스가 아니라 **아이콘 자체의 색(굵기 느낌) 차이**다 — Figma `2542:125368`
 * 실측 결과 아이콘은 배경 없이 24dp 그대로 노출되고, 눌림 리플 레이어(`Interaction`, opacity 0)의
 * 색만 활성/비활성 아이콘마다 다르게 지정돼 있다(활성=`Semantic/Label/Normal`, 비활성=
 * `Semantic/Label/Alternative`) — 이 색을 배경이 아니라 아이콘 틴트로 그대로 옮긴다. 이전엔 32dp
 * 회색 박스 배경을 임의로 추가했었는데, 그런 배경 자체가 Figma에 없었다.
 */
private object RoomDetailViewTypeToggleTokens {
    val Spacing = 8.dp
    val IconSize = 24.dp
}

@Composable
private fun RoomDetailViewTypeToggle(
    selected: PlaceViewType,
    onSelected: (PlaceViewType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(RoomDetailViewTypeToggleTokens.Spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoomDetailViewTypeToggleButton(
            icon = MinoIcons.ListPlaceIcon,
            active = selected == PlaceViewType.LIST,
            onClick = { onSelected(PlaceViewType.LIST) },
        )
        RoomDetailViewTypeToggleButton(
            icon = MinoIcons.Thumbnail,
            active = selected == PlaceViewType.CARD,
            onClick = { onSelected(PlaceViewType.CARD) },
        )
    }
}

@Composable
private fun RoomDetailViewTypeToggleButton(
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tintColor = if (active) {
        MinoAndroidTheme.colors.labelNormal
    } else {
        MinoAndroidTheme.colors.labelAlternative
    }
    Icon(
        modifier = modifier
            .size(RoomDetailViewTypeToggleTokens.IconSize)
            .rippleSingleClickable(onClick = onClick),
        imageVector = icon,
        contentDescription = null,
        tint = tintColor,
    )
}

/** 정렬 드롭다운 트리거. 펼침 패널([RoomDetailSortMenu])은 이 컴포저블 바깥(호출부)에서 그린다. */
@Composable
private fun RoomDetailSortTrigger(
    selected: MapMarkerSortOption,
    onClick: () -> Unit,
    onPositioned: (LayoutCoordinates) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .onGloballyPositioned(onPositioned)
            .border(
                width = RoomDetailSortAndFilterRowTokens.TriggerBorderWidth,
                color = MinoAndroidTheme.colors.lineNormalNeutral,
                shape = RoomDetailSortAndFilterRowTokens.TriggerShape,
            ).clip(RoomDetailSortAndFilterRowTokens.TriggerShape)
            .background(MinoAndroidTheme.colors.backgroundNormalNormal)
            .rippleSingleClickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(RoomDetailSortAndFilterRowTokens.TriggerSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = selected.label(),
            color = MinoAndroidTheme.colors.labelNormal,
            style = MinoAndroidTheme.typography.body2NormalMedium,
        )
        Icon(
            modifier = Modifier.size(RoomDetailSortAndFilterRowTokens.TriggerIconSize),
            imageVector = MinoIcons.CaretDown,
            contentDescription = null,
            tint = MinoAndroidTheme.colors.labelNormal,
        )
    }
}

/** [RoomDetailSortTrigger] 펼침 패널. 펼침 순서는 spec.md FR-005 「꾹 Pick/전체/최신순/거리순/코멘트순」. */
@Composable
private fun RoomDetailSortMenu(
    selected: MapMarkerSortOption,
    onSelected: (MapMarkerSortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    MinoMenu(modifier = modifier.width(RoomDetailSortAndFilterRowTokens.MenuWidth)) {
        MapMarkerSortOption.entries.forEach { option ->
            MinoMenuItem(
                text = option.label(),
                active = option == selected,
                onClick = { onSelected(option) },
            )
        }
    }
}

private fun MapMarkerSortOption.label(): String =
    when (this) {
        MapMarkerSortOption.GGUK_PICK -> "꾹 Pick"
        MapMarkerSortOption.ALL -> "전체"
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

/** [PlaceCategoryFilter.entries]는 고정 목록이라 매 리컴포지션마다 다시 만들 필요가 없다. */
private val PlaceCategoryLabels = PlaceCategoryFilter.entries.map { it.label() }.toImmutableList()
