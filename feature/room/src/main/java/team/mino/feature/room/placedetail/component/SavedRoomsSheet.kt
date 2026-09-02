package team.mino.feature.room.placedetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.common.ui.component.RoomThumbnailFallback
import team.mino.core.designsystem.component.roomcard.MinoRoomChevronCard
import team.mino.core.designsystem.component.roomthumbnail.MinoRoomThumbnail
import team.mino.core.designsystem.component.scrollbar.MinoScrollBar
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.RoomColor
import team.mino.feature.room.R
import team.mino.feature.room.main.model.chip
import team.mino.feature.room.placedetail.model.RoomPickerItem
import team.mino.feature.room.placedetail.vm.SavedRoomsSheetUiState
import kotlin.math.roundToInt

/**
 * 지금 보고 있는 장소가 저장된 다른 방으로 옮겨 가는 시트(spec 유저 플로우 7 · FR-024).
 *
 * **카드를 누르는 것이 곧 확정이다.** 체크박스도 확정 CTA도 없어 고른 뒤 다시 누를 곳이 없고, 그래서 이 시트는
 * 선택 상태라는 것을 갖지 않는다(spec TS-043).
 *
 * **지금 보고 있는 방은 목록에 없다.** 선택된 카드로 세워 두는 것이 아니라 빼는 것이며, 거르는 일은 이미
 * [SavedRoomsSheetUiState.rooms]를 만들 때 끝나 있다(spec FR-024 · TS-042 · EC-026). 여기서 다시 거르지 않는
 * 것은 그 판정이 두 곳에 있으면 갈라지기 때문이고, 그 결과 이 시트에는 눌러도 아무 일이 없는 카드가 서지
 * 않는다(spec UX-012) — 남아 있는 카드가 곧 옮겨 갈 수 있는 방이다.
 *
 * **높이가 방 개수를 따라가지 않는다.** 시트도 목록 영역도 고정이라 방이 늘면 목록만 세로로 스크롤한다
 * (spec TS-048). 맨 아래 띠는 시스템 바가 지나갈 자리로, 시트 높이 안에 들어 있어 인셋만큼 더 자라지 않는다
 * (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §3.3).
 *
 * **[다른방에 공유] 시트와 치수를 나눠 쓰지 않는다.** 손잡이·모서리처럼 어느 시트에나 같아야 하는 것만
 * `SheetParts.kt`에서 오고, 높이 체계는 서로 다른 규정에서 나온다.
 *
 * **상태를 소유하지 않는다.** 무엇이 보이는지는 [state] 한 곳에 있고, 닫는 판단은 [onDismissRequest]를 받는
 * 쪽이 한다. 뒤로가기로 닫는 경로는 여기 없다 — 그 처리는 Route가 갖는다(spec EC-025).
 *
 * @param state 옮겨 갈 수 있는 방 목록. `null`이 곧 닫힘이라 열린 채 비어 있는 시트가 생기지 않는다.
 * @param onRoomSelected 카드 탭. 옮겨 갈 방의 핀과 그 방을 함께 올린다 — 방 id만으로는 무엇을 다시 조회할지
 *  정해지지 않는다(spec FR-025).
 * @param onDismissRequest 딤 영역 탭과 아래로 끝까지 끌어내린 결과가 올라온다(spec EC-025).
 */
@Composable
internal fun SavedRoomsSheet(
    state: SavedRoomsSheetUiState,
    onRoomSelected: (pinId: String, roomId: String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dragState = rememberSheetDismissDragState(sheetHeight = SheetHeight, onDismissRequest = onDismissRequest)

    DimmedSheetContainer(onDismissRequest = onDismissRequest, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(SheetHeight)
                .offset { IntOffset(x = 0, y = dragState.requireOffset().roundToInt()) }
                .anchoredDraggable(state = dragState, orientation = Orientation.Vertical)
                // 시트가 히트 테스트에 잡혀야 그 위의 탭이 뒤의 딤으로 내려가 닫히지 않는다.
                .pointerInput(Unit) {},
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SheetBodyHeight)
                    .surface(
                        shape = SheetContainerShape,
                        containerColor = MinoAndroidTheme.colors.backgroundElevatedNormal,
                    ),
            ) {
                SavedRoomsSheetHeader()
                SavedRoomsList(
                    rooms = state.rooms,
                    onRoomSelected = onRoomSelected,
                    modifier = Modifier.height(ScrollAreaHeight),
                )
            }
            SheetBottomInsetBand()
        }
    }
}

/**
 * 손잡이와 시트 이름. 목록이 스크롤돼도 함께 움직이지 않게 스크롤 영역 밖에 선다.
 *
 * 배경이 두 칸으로 갈린다 — 손잡이 구간은 시트 본체 색(`Background/Elevated/Normal`)을 그대로 두고
 * 타이틀 줄에만 `Background/Normal/Normal`을 건다. 라이트는 두 토큰이 같은 원시 토큰이라 차이가 없지만
 * 다크에서 갈리므로(CoolNeutral17 / CoolNeutral15) 한 겹으로 합치면 시안과 어긋난다.
 */
@Composable
private fun SavedRoomsSheetHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(HeaderHeight),
    ) {
        SheetDragHandle()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(TitleRowHeight)
                .background(MinoAndroidTheme.colors.backgroundNormalNormal)
                .padding(
                    start = HorizontalPadding,
                    end = HorizontalPadding,
                    bottom = TitleRowBottomPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.placedetail_saved_rooms),
                style = MinoAndroidTheme.typography.heading2Bold,
                color = MinoAndroidTheme.colors.labelNormal,
                maxLines = 1,
            )
        }
    }
}

/**
 * 옮겨 갈 수 있는 방을 세로로 늘어놓는 목록. 시트에서 유일하게 스크롤하는 영역이다.
 *
 * **거르지 않는다.** [rooms]는 이미 옮겨 갈 수 있는 방만 담고 있다([SavedRoomsSheet] KDoc).
 * `matchedPinId`를 널 검사하는 것도 거르기가 아니라 타입을 여는 절차다 — [RoomPickerItem]이 저장 여부를 묻지
 * 않는 시트와 타입을 나눠 쓰느라 그 자리를 열어 두었을 뿐, 이 목록에 오는 항목에는 값이 있다.
 *
 * 오른쪽 끝에 겹쳐 놓인 [MinoScrollBar]는 스크롤 여지를 알리는 보조 수단이다. 목록과 같은 상태를 보며 표시만
 * 하고, 카드 폭 밖에 놓여 탭을 가로채지 않는다.
 */
@Composable
private fun SavedRoomsList(
    rooms: ImmutableList<RoomPickerItem>,
    onRoomSelected: (pinId: String, roomId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = HorizontalPadding),
        ) {
            items(items = rooms, key = { it.id }) { room ->
                MinoRoomChevronCard(
                    title = room.name,
                    placeCountLabel = stringResource(R.string.placedetail_room_place_count, room.placeCount),
                    onClick = { room.matchedPinId?.let { pinId -> onRoomSelected(pinId, room.id) } },
                    thumbnail = {
                        MinoRoomThumbnail(
                            imageUrls = room.thumbnailImageUrls,
                            fallback = {
                                RoomThumbnailFallback(
                                    color = room.color.chip,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            },
                        )
                    },
                    memo = room.description,
                )
            }
        }
        // matchParentSize는 목록이 정한 높이를 되밀지 않고 그대로 받아 오지만 폭까지 함께 고정한다.
        // 스크롤바를 그 안에 넣어야 자기 폭을 지킨 채 오른쪽 끝에 선다.
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.TopEnd,
        ) {
            MinoScrollBar(
                scrollState = listState,
                modifier = Modifier.padding(vertical = ScrollBarVerticalInset),
            )
        }
    }
}

/**
 * 시트 맨 아래의 빈 띠. 시스템 바가 지나갈 자리를 시트 높이 안에 미리 비워 둔 것이라 인셋을 따로 얹지 않는다.
 *
 * 담는 것이 없어도 목록이 여기까지 흘러내려 잘리는 것처럼 보이지 않게, 위쪽 선 하나로 스크롤 영역과 끊는다.
 */
@Composable
private fun SheetBottomInsetBand(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BottomInsetHeight)
            .background(MinoAndroidTheme.colors.backgroundNormalNormal),
    ) {
        HorizontalDivider(
            thickness = SheetDividerThickness,
            color = MinoAndroidTheme.colors.lineNormalNeutral,
        )
    }
}

private val HeaderHeight = 70.dp

private val TitleRowHeight = 40.dp

private val TitleRowBottomPadding = 12.dp

private val ScrollAreaHeight = 312.dp

private val BottomInsetHeight = 60.dp

/** 헤더와 목록 영역을 합친 높이. 두 값에서 파생시켜야 한쪽만 고쳐 시트가 조용히 잘리는 일이 없다. */
private val SheetBodyHeight = HeaderHeight + ScrollAreaHeight

private val SheetHeight = SheetBodyHeight + BottomInsetHeight

private val HorizontalPadding = 20.dp

private val ScrollBarVerticalInset = 12.dp

/** 옮겨 갈 방이 둘인 보통의 경우. */
@UiModePreviews
@Composable
private fun SavedRoomsSheetPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        SavedRoomsSheet(
            state = SavedRoomsSheetUiState(rooms = previewSavedRoomItems(2)),
            onRoomSelected = { _, _ -> },
            onDismissRequest = {},
            modifier = modifier,
        )
    }
}

/**
 * 옮겨 갈 방이 하나뿐인 최소 상태.
 *
 * [저장된 방]은 이 장소가 두 방 이상에 저장돼 있어야 열리고 그 수에는 지금 보고 있는 방도 들어 있으므로
 * (spec FR-023 · EC-024), 시트가 열린 채 담을 수 있는 가장 적은 개수가 하나다.
 */
@UiModePreviews
@Composable
private fun SavedRoomsSheetSingleRoomPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        SavedRoomsSheet(
            state = SavedRoomsSheetUiState(rooms = previewSavedRoomItems(1)),
            onRoomSelected = { _, _ -> },
            onDismissRequest = {},
            modifier = modifier,
        )
    }
}

/** 목록 영역에 다 들어가지 않아 스크롤이 생기는 개수. 시트 높이는 그대로여야 한다(spec TS-048). */
@UiModePreviews
@Composable
private fun SavedRoomsSheetScrollablePreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        SavedRoomsSheet(
            state = SavedRoomsSheetUiState(rooms = previewSavedRoomItems(6)),
            onRoomSelected = { _, _ -> },
            onDismissRequest = {},
            modifier = modifier,
        )
    }
}

/**
 * 이 시트를 그려 보는 데 필요한 만큼의 방 목록.
 *
 * [다른방에 공유] 시트의 샘플을 함께 쓰지 않는 것은 그쪽 항목이 모두 「아직 저장되지 않은 방」이어서다 —
 * 그대로 넣으면 옮겨 갈 방이 하나도 없는 목록이 되어 시트가 비어 보인다. 여기 담기는 항목은 반대로 전부
 * 저장돼 있고 옮겨 갈 핀을 갖는다.
 */
private fun previewSavedRoomItems(count: Int): ImmutableList<RoomPickerItem> =
    persistentListOf(
        RoomPickerItem(
            id = "shared-1",
            name = "민호야 잘하자",
            description = null,
            placeCount = 9,
            thumbnailImageUrls = persistentListOf(),
            color = RoomColor.CYAN,
            hasPlace = true,
            matchedPinId = "pin-1",
        ),
        RoomPickerItem(
            id = "shared-2",
            name = "매쉬업 화이팅",
            description = "팀원 모두가 좋아할 만한 술집 모음",
            placeCount = 2,
            thumbnailImageUrls = persistentListOf(),
            color = RoomColor.ORANGE,
            hasPlace = true,
            matchedPinId = "pin-2",
        ),
        RoomPickerItem(
            id = "personal",
            name = "내 장소",
            description = null,
            placeCount = 14,
            thumbnailImageUrls = persistentListOf(),
            color = RoomColor.GRAY,
            hasPlace = true,
            matchedPinId = "pin-3",
        ),
        RoomPickerItem(
            id = "shared-3",
            name = "언젠가 가야지",
            description = "저장만 하고 안 간 곳들",
            placeCount = 3,
            thumbnailImageUrls = persistentListOf(),
            color = RoomColor.PURPLE,
            hasPlace = true,
            matchedPinId = "pin-4",
        ),
        RoomPickerItem(
            id = "shared-4",
            name = "성수 산책 코스",
            description = "걷다가 들르기 좋은 곳",
            placeCount = 7,
            thumbnailImageUrls = persistentListOf(),
            color = RoomColor.GREEN,
            hasPlace = true,
            matchedPinId = "pin-5",
        ),
        RoomPickerItem(
            id = "shared-5",
            name = "부모님 모시고",
            description = null,
            placeCount = 5,
            thumbnailImageUrls = persistentListOf(),
            color = RoomColor.RED,
            hasPlace = true,
            matchedPinId = "pin-6",
        ),
    ).take(count).toImmutableList()
