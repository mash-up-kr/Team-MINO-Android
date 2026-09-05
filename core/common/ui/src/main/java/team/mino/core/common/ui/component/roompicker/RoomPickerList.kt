package team.mino.core.common.ui.component.roompicker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import team.mino.core.common.ui.R
import team.mino.core.common.ui.component.RoomThumbnailFallback
import team.mino.core.common.ui.component.roompicker.model.RoomPickerItem
import team.mino.core.designsystem.component.roomcard.MinoRoomCheckBoxCard
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor
import team.mino.core.designsystem.component.roomthumbnail.MinoRoomThumbnail
import team.mino.core.designsystem.component.scrollbar.MinoScrollBar
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * 고를 수 있는 방을 세로로 늘어놓는 목록. 시트에서 유일하게 스크롤하는 영역이다(UX-004).
 *
 * **선택을 들지 않는다.** 어떤 방이 골라졌는지는 [selectedRoomIds] 한 곳에만 있고, 카드는 자기 id가
 * 거기 있는지만 본다.
 *
 * 카드 본문 탭과 체크박스 탭이 모두 [onRoomToggle]로 모인다 — 카드 영역 어디를 눌러도 선택이
 * 토글되기 때문이다(UX-003).
 *
 * 이미 그 장소가 저장된 방을 가려내지 않는다(FR-016). 시트를 그리는 시점에는 어떤 장소인지조차
 * 정해지지 않아 중복 판정이 성립하지 않는다.
 *
 * 높이는 정하지 않는다 — 헤더·액션 영역을 뺀 나머지를 얼마나 차지할지는 시트를 조립하는 쪽이 [modifier]로 준다.
 *
 * 오른쪽 끝에 겹쳐 놓인 [MinoScrollBar]는 스크롤 여지를 알리는 보조 수단이다(UX-005). 목록과 같은 [state]를
 * 보며 표시만 하고, 카드 폭 밖에 놓여 탭을 가로채지 않는다.
 */
@Composable
fun RoomPickerList(
    rooms: ImmutableList<RoomPickerItem>,
    selectedRoomIds: ImmutableSet<String>,
    onRoomToggle: (roomId: String) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
) {
    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            contentPadding = PaddingValues(horizontal = HorizontalPadding),
        ) {
            items(items = rooms, key = { it.id }) { room ->
                MinoRoomCheckBoxCard(
                    title = room.name,
                    placeCountLabel = room.placeCountLabel,
                    checked = room.id in selectedRoomIds,
                    onCheckedChange = { onRoomToggle(room.id) },
                    onClick = { onRoomToggle(room.id) },
                    thumbnail = {
                        MinoRoomThumbnail(
                            imageUrls = room.thumbnailImageUrls,
                            fallback = {
                                RoomThumbnailFallback(
                                    color = room.color,
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
                scrollState = state,
                modifier = Modifier.padding(vertical = ScrollBarVerticalInset),
            )
        }
    }
}

private val HorizontalPadding = 20.dp

private val ScrollBarVerticalInset = 12.dp

@UiModePreviews
@Composable
private fun RoomPickerListPreview() {
    MinoAndroidAppTheme {
        RoomPickerList(
            rooms = persistentListOf(
                RoomPickerItem(
                    id = "personal",
                    name = "내 장소",
                    description = null,
                    placeCountLabel = stringResource(R.string.room_picker_room_place_count, 0),
                    thumbnailImageUrls = persistentListOf(),
                    color = null,
                ),
                RoomPickerItem(
                    id = "shared-1",
                    name = "민호야 잘하자",
                    description = null,
                    placeCountLabel = stringResource(R.string.room_picker_room_place_count, 9),
                    thumbnailImageUrls = persistentListOf(),
                    color = MinoRoomColor.Cyan,
                ),
                RoomPickerItem(
                    id = "shared-2",
                    name = "매쉬업 화이팅",
                    description = "팀원 모두가 좋아할 만한 술집 모음",
                    placeCountLabel = stringResource(R.string.room_picker_room_place_count, 2),
                    thumbnailImageUrls = persistentListOf(),
                    color = MinoRoomColor.Orange,
                ),
            ),
            selectedRoomIds = persistentSetOf("shared-1"),
            onRoomToggle = {},
        )
    }
}
