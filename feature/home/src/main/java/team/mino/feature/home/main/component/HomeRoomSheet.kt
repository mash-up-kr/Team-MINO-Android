package team.mino.feature.home.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.common.ui.component.RoomThumbnailFallback
import team.mino.core.designsystem.component.roomthumbnail.MinoRoomThumbnail
import team.mino.core.designsystem.component.roomthumbnail.MinoRoomThumbnailDefaults
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Plus
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.selectable.rippleSingleSelectable
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.model.RoomType
import team.mino.feature.home.R
import team.mino.feature.home.main.model.chip

/**
 * 방을 바꾸는 바텀시트(spec FR-017·FR-018).
 *
 * **누르는 것이 곧 확정이다** — 체크박스도 확정 버튼도 없다. 그래서 [currentRoomId]는 표시가 아니라
 * 접근성 선택 상태로만 쓰인다. 같은 방을 다시 골랐을 때 덱을 다시 구성할지는 여기서 판정하지 않는다(spec EC-014).
 *
 * 열림 여부를 들지 않는다 — `HomeUiState.isRoomSheetOpen`이 참일 때만 호출부가 부른다.
 *
 * @param rooms 시트에 늘어놓을 방. 첫 칸은 언제나 `방 만들기`이고 그 뒤로 이 순서대로 놓인다.
 * @param onCreateRoom `방 만들기` 칸(spec EC-015).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeRoomSheet(
    rooms: ImmutableList<RoomSummary>,
    currentRoomId: String?,
    onSelectRoom: (String) -> Unit,
    onCreateRoom: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = SheetShape,
        containerColor = MinoAndroidTheme.colors.backgroundElevatedNormal,
        scrimColor = Color.Black.copy(alpha = SCRIM_ALPHA),
        dragHandle = { SheetHandle() },
    ) {
        // 높이는 시트가 아니라 본체에 준다. ModalBottomSheet의 modifier는 시트가 설 자리를 재는
        // 제약보다 바깥이라, 거기에 높이를 묶으면 시트가 바닥이 아니라 화면 맨 위에 붙는다.
        RoomSheetContent(
            rooms = rooms,
            currentRoomId = currentRoomId,
            onSelectRoom = onSelectRoom,
            onCreateRoom = onCreateRoom,
            modifier = Modifier.height(SheetHeight - HandleAreaHeight),
        )
    }
}

/** 시트 없이도 그릴 수 있는 본체. 프리뷰가 이걸 쓴다. */
@Composable
private fun RoomSheetContent(
    rooms: ImmutableList<RoomSummary>,
    currentRoomId: String?,
    onSelectRoom: (String) -> Unit,
    onCreateRoom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.home_room_sheet_title),
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderHeight)
                .padding(horizontal = SheetPadding)
                .wrapContentHeight(),
            color = MinoAndroidTheme.colors.labelStrong,
            style = MinoAndroidTheme.typography.title3Bold,
        )
        // 열 수는 화면 폭과 무관하게 셋이고 칸은 썸네일 크기 그대로다(spec FR-018). 그래서 폭을 나눠 갖는
        // 대신 셋이 들어갈 만큼만 폭을 잡아 가운데 두고, 남는 폭은 양옆 여백으로 흘린다.
        LazyVerticalGrid(
            columns = GridCells.Fixed(COLUMN_COUNT),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxHeight()
                .width(GridWidth),
            contentPadding = PaddingValues(top = HeaderSpacing, bottom = SheetPadding),
            verticalArrangement = Arrangement.spacedBy(RowSpacing),
            horizontalArrangement = Arrangement.spacedBy(ColumnSpacing),
        ) {
            item { CreateRoomCell(onClick = onCreateRoom) }
            items(items = rooms, key = { it.id }) { room ->
                RoomCell(
                    room = room,
                    selected = room.id == currentRoomId,
                    onClick = { onSelectRoom(room.id) },
                )
            }
        }
    }
}

/**
 * 방 한 칸. 저장된 사진이 없으면 방 대표 색의 캐릭터가 자리를 채운다.
 *
 * @param selected 지금 보고 있는 방인지. 눈에 보이는 표시는 없고(spec FR-018) 선택 상태로만 읽힌다.
 */
@Composable
private fun RoomCell(
    room: RoomSummary,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoomSheetCell(
        label = room.name,
        labelColor = Color.Black,
        modifier = modifier.rippleSingleSelectable(
            selected = selected,
            role = Role.RadioButton,
            onClick = onClick,
        ),
    ) {
        MinoRoomThumbnail(imageUrls = room.thumbnailImageUrls.toImmutableList()) {
            RoomThumbnailFallback(color = room.color.chip, modifier = Modifier.fillMaxSize())
        }
    }
}

/** 첫 칸. 방 대신 생성 흐름으로 나가는 자리라 썸네일 없이 테두리와 `+`만 둔다(spec EC-015). */
@Composable
private fun CreateRoomCell(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoomSheetCell(
        label = stringResource(R.string.home_room_sheet_create_room),
        labelColor = MinoAndroidTheme.colors.primaryNormal,
        modifier = modifier.rippleSingleClickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(MinoRoomThumbnailDefaults.size)
                .border(
                    width = CellBorderWidth,
                    color = MinoAndroidTheme.colors.lineNormalNormal,
                    shape = MinoRoomThumbnailDefaults.shape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MinoIcons.Plus,
                contentDescription = null,
                tint = MinoAndroidTheme.colors.labelNeutral,
            )
        }
    }
}

/** 칸의 공통 뼈대 — 정사각형 하나와 그 아래 이름. */
@Composable
private fun RoomSheetCell(
    label: String,
    labelColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(CellSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
        Text(
            text = label,
            color = labelColor,
            style = MinoAndroidTheme.typography.caption1Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HandleAreaHeight),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(HandleWidth)
                .height(HandleHeight)
                .background(
                    color = MinoAndroidTheme.colors.fillNormal,
                    shape = RoundedCornerShape(HandleHeight),
                ),
        )
    }
}

private const val SCRIM_ALPHA = 0.7f

private val SheetHeight = 400.dp
private val SheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
private val HandleAreaHeight = 30.dp
private val HandleWidth = 38.dp
private val HandleHeight = 4.dp
private val HeaderHeight = 60.dp
private val ColumnSpacing = 35.dp
private val CellSpacing = 12.dp
private val CellBorderWidth = 1.dp

private const val COLUMN_COUNT = 3

private val GridWidth =
    MinoRoomThumbnailDefaults.size * COLUMN_COUNT + ColumnSpacing * (COLUMN_COUNT - 1)

// Figma base-lg 변수 대응 — 토큰 미존재
private val SheetPadding = 20.dp

// Figma base 변수 대응 — 토큰 미존재
private val HeaderSpacing = 16.dp

// Figma xl 변수 대응 — 토큰 미존재
private val RowSpacing = 28.dp

@Suppress("ComposeModifierMissing") // 프리뷰 함수는 modifier가 불필요
@UiModePreviews
@Composable
private fun HomeRoomSheetPreview() {
    val rooms = List(5) { index ->
        RoomSummary(
            id = "$index",
            name = "민호야잘하자방",
            description = "",
            type = if (index == 0) RoomType.PERSONAL else RoomType.GROUP,
            color = RoomColor.entries[index],
            placeCount = index,
            thumbnailImageUrls = emptyList(),
        )
    }.toImmutableList()

    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .height(SheetHeight)
                .background(MinoAndroidTheme.colors.backgroundElevatedNormal, SheetShape),
        ) {
            SheetHandle()
            RoomSheetContent(
                rooms = rooms,
                currentRoomId = "0",
                onSelectRoom = {},
                onCreateRoom = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}
