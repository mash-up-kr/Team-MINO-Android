package team.mino.feature.placedetail.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.drop
import team.mino.core.common.ui.component.RoomThumbnailFallback
import team.mino.core.designsystem.component.actionarea.ActionAreaAction
import team.mino.core.designsystem.component.actionarea.MinoActionArea
import team.mino.core.designsystem.component.roomcard.MinoRoomCheckBoxCard
import team.mino.core.designsystem.component.roomthumbnail.MinoRoomThumbnail
import team.mino.core.designsystem.component.scrollbar.MinoScrollBar
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Image
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.singleClickable
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.RoomColor
import team.mino.feature.placedetail.R
import team.mino.feature.placedetail.main.model.RoomPickerItem
import team.mino.feature.placedetail.main.model.palette
import team.mino.feature.placedetail.main.vm.ShareSheetUiState
import kotlin.math.roundToInt

/**
 * 지금 보고 있는 장소를 다른 방에도 담는 방 선택 시트(spec 유저 플로우 6 · FR-018).
 *
 * **딤 위에 하단 정렬로 떠 있고 단계가 하나뿐이다.** 시트 높이는 방이 몇 개든 고정이며(spec §3.2), 방이 넘치면
 * 시트가 늘어나는 대신 목록만 세로로 스크롤한다. 아래로 끌면 그 자리에서 닫힌다 — 중간에 머무는 자리를 두지
 * 않아 조금 끌었다 놓으면 제자리로 되돌아온다.
 *
 * **상태를 소유하지 않는다.** 어떤 방이 골라졌는지도 보내는 중인지도 [state] 한 곳에 있고, 닫는 판단은
 * [onDismissRequest]를 받는 쪽이 한다. 뒤로가기로 닫는 경로는 여기 없다 — 그 처리는 Route가 갖는다.
 *
 * **이미 저장된 방의 비활성 표현이 아직 없다.** [RoomPickerItem.hasPlace]가 이번 라운드에 전부 `false`여서
 * (`docs/specs/place-detail/research.md` D15) 체크만 그 값에서 나오고, 눌리지 않게 막는 것과 흐리게 보이는
 * 것은 값이 실제로 채워질 때 함께 붙는다.
 *
 * **[새 방 만들기] 행을 그리지 않는다.** 그 행은 다른 feature의 공동방 생성 화면으로 나가는 입구라 이 시트가
 * 단독으로 배선할 수 없고, 그것을 실어 나를 Intent도 아직 없다.
 *
 * @param placeName 공유 대상 장소의 이름.
 * @param placeAddress 공유 대상 장소의 주소.
 * @param placeImageUrl 대상 장소 카드에 놓을 사진. 여러 장 중 첫 장이며, 없으면 자리표시자 글리프를 그린다.
 * @param state 목록·선택·전송 여부. [ShareSheetUiState.isShareEnabled]가 CTA 활성을 정한다(spec FR-022).
 * @param onRoomToggle 카드 본문 탭과 체크박스 탭이 모두 여기로 모인다 — 카드 어디를 눌러도 선택이 토글된다.
 * @param onShareClick [공유하기].
 * @param onDismissRequest 딤 영역 탭과 아래로 끝까지 끌어내린 결과가 올라온다(spec EC-021).
 */
@Composable
internal fun RoomShareSheet(
    placeName: String,
    placeAddress: String,
    placeImageUrl: String?,
    state: ShareSheetUiState,
    onRoomToggle: (roomId: String) -> Unit,
    onShareClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val heightPx = with(LocalDensity.current) { SheetHeight.toPx() }
    val anchors = remember(heightPx) {
        DraggableAnchors {
            ShareSheetAnchor.OPEN at 0f
            ShareSheetAnchor.GONE at heightPx
        }
    }
    val dragState = remember { AnchoredDraggableState(initialValue = ShareSheetAnchor.OPEN, anchors = anchors) }
    // 앵커가 고정 높이에서 나오므로 밀도가 바뀌어도 선 자리를 지킨 채 좌표만 옮긴다.
    SideEffect { dragState.updateAnchors(anchors, dragState.targetValue) }

    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
    LaunchedEffect(dragState) {
        snapshotFlow { dragState.settledValue }
            // 처음 선 자리는 열자마자 닫는 것이 아니다.
            .drop(1)
            .collect { anchor ->
                if (anchor == ShareSheetAnchor.GONE) currentOnDismissRequest()
            }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(MinoAndroidTheme.colors.materialDimmer)
                .singleClickable(onClick = onDismissRequest),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(SheetHeight)
                .offset { IntOffset(x = 0, y = dragState.requireOffset().roundToInt()) }
                .surface(shape = SheetContainerShape, containerColor = MinoAndroidTheme.colors.backgroundElevatedNormal)
                .anchoredDraggable(state = dragState, orientation = Orientation.Vertical)
                // 시트가 히트 테스트에 잡혀야 그 위의 탭이 뒤의 딤으로 내려가 닫히지 않는다.
                .pointerInput(Unit) {},
        ) {
            SheetDragHandle()
            TargetPlaceRow(
                placeName = placeName,
                placeAddress = placeAddress,
                placeImageUrl = placeImageUrl,
            )
            SheetSectionDivider(horizontalPadding = HorizontalPadding)
            RoomShareList(
                rooms = state.rooms,
                selectedRoomIds = state.selectedRoomIds,
                onRoomToggle = onRoomToggle,
                // 남는 자리를 목록이 가져간다. 위아래 고정 영역은 자기 높이를 지켜 스크롤에서 빠진다.
                modifier = Modifier.weight(1f),
            )
            // 시트가 화면 맨 아래에 붙으므로 시스템 바를 피하는 건 맨 밑 요소인 액션 영역 몫이다.
            // 컴포넌트도 시트도 인셋을 소비하지 않아 여기서 한 번만 얹힌다(`MinoActionArea` KDoc).
            MinoActionArea(
                modifier = Modifier.navigationBarsPadding(),
                mainAction = ActionAreaAction(
                    text = stringResource(R.string.placedetail_share_confirm),
                    onClick = onShareClick,
                    enabled = state.isShareEnabled,
                ),
                // 목록이 이 영역 밑으로 지나가며 잘리므로, 배경과 그 위 페이드가 함께 필요하다.
                sticky = true,
            )
        }
    }
}

/**
 * 어느 장소를 공유하는지 알리는 대상 장소 카드(spec TS-032).
 *
 * 사진은 그 장소의 첫 장이고, 아직 못 받았거나 로딩에 실패하면 자리표시자 글리프로 대신한다. 이름과 주소는
 * 각각 한 줄을 지켜 시트 높이가 장소마다 달라지지 않는다.
 */
@Composable
private fun TargetPlaceRow(
    placeName: String,
    placeAddress: String,
    placeImageUrl: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HorizontalPadding)
            .height(TargetPlaceRowHeight),
        horizontalArrangement = Arrangement.spacedBy(TargetPlaceSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaceImage(
            imageUrl = placeImageUrl,
            fallback = rememberVectorPainter(MinoIcons.Image),
            size = TargetPlaceImageSize,
            shape = TargetPlaceImageShape,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(TargetPlaceTextSpacing),
        ) {
            Text(
                text = placeName,
                style = MinoAndroidTheme.typography.body1NormalBold,
                color = MinoAndroidTheme.colors.labelNormal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = placeAddress,
                style = MinoAndroidTheme.typography.label2Medium,
                color = MinoAndroidTheme.colors.labelAlternative,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 고를 수 있는 방을 세로로 늘어놓는 목록. 시트에서 유일하게 스크롤하는 영역이다.
 *
 * **선택을 들지 않는다.** 어떤 방이 골라졌는지는 [selectedRoomIds] 한 곳에만 있고, 카드는 자기 id가 거기 있는지만
 * 본다. [RoomPickerItem.hasPlace]는 이미 저장된 방을 체크된 채로 세우지만, 그 카드를 눌리지 않게 막고 흐리게
 * 그리는 것은 아직 없다 — 그 값이 실제로 채워질 때 함께 붙는다.
 *
 * 오른쪽 끝에 겹쳐 놓인 [MinoScrollBar]는 스크롤 여지를 알리는 보조 수단이다. 목록과 같은 상태를 보며 표시만
 * 하고, 카드 폭 밖에 놓여 탭을 가로채지 않는다.
 */
@Composable
private fun RoomShareList(
    rooms: ImmutableList<RoomPickerItem>,
    selectedRoomIds: ImmutableSet<String>,
    onRoomToggle: (roomId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(horizontal = HorizontalPadding),
        ) {
            items(items = rooms, key = { it.id }) { room ->
                MinoRoomCheckBoxCard(
                    title = room.name,
                    placeCountLabel = stringResource(R.string.placedetail_share_room_place_count, room.placeCount),
                    checked = room.hasPlace || room.id in selectedRoomIds,
                    onCheckedChange = { onRoomToggle(room.id) },
                    onClick = { onRoomToggle(room.id) },
                    thumbnail = {
                        MinoRoomThumbnail(
                            imageUrls = room.thumbnailImageUrls,
                            fallback = {
                                RoomThumbnailFallback(
                                    color = room.color.palette,
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
 * 시트가 멈춰 서는 자리. 열린 자리와 화면 밖 두 곳뿐이라 끌어 옮길 단계가 없고, 아래로 끝까지 끌면 닫힌다.
 */
private enum class ShareSheetAnchor {
    OPEN,
    GONE,
}

private val SheetHeight = 676.dp

private val HorizontalPadding = 20.dp

private val TargetPlaceRowHeight = 60.dp

private val TargetPlaceSpacing = 14.dp

private val TargetPlaceImageSize = 46.dp

private val TargetPlaceImageShape = RoundedCornerShape(7.83.dp)

private val TargetPlaceTextSpacing = 4.dp

private val ScrollBarVerticalInset = 12.dp

@UiModePreviews
@Composable
private fun RoomShareSheetPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        RoomShareSheet(
            placeName = "성수 감자탕",
            placeAddress = "서울 성동구 아차산로 100",
            placeImageUrl = null,
            state = ShareSheetUiState(rooms = previewRoomPickerItems(), selectedRoomIds = persistentSetOf("shared-2")),
            onRoomToggle = {},
            onShareClick = {},
            onDismissRequest = {},
            modifier = modifier,
        )
    }
}

/** 하나도 고르지 않아 [공유하기]가 비활성인 상태(spec FR-022). */
@UiModePreviews
@Composable
private fun RoomShareSheetEmptySelectionPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        RoomShareSheet(
            placeName = "성수 감자탕",
            placeAddress = "서울 성동구 아차산로 100",
            placeImageUrl = null,
            state = ShareSheetUiState(rooms = previewRoomPickerItems()),
            onRoomToggle = {},
            onShareClick = {},
            onDismissRequest = {},
            modifier = modifier,
        )
    }
}

/**
 * 시트를 그려 보는 데 필요한 만큼의 방 목록. 실제 목록은 `getRooms()`에서 오므로 샘플 데이터에 없다
 * (`docs/specs/place-detail/research.md` D15).
 *
 * 화면 프리뷰도 이것을 부른다 — 같은 샘플을 두 벌 적어 두면 [RoomPickerItem]에 필드가 늘 때 한쪽만 고쳐진다.
 */
internal fun previewRoomPickerItems(): ImmutableList<RoomPickerItem> =
    persistentListOf(
        RoomPickerItem(
            id = "personal",
            name = "내 장소",
            description = null,
            placeCount = 0,
            thumbnailImageUrls = persistentListOf(),
            color = RoomColor.GRAY,
            hasPlace = false,
        ),
        RoomPickerItem(
            id = "shared-1",
            name = "민호야 잘하자",
            description = null,
            placeCount = 9,
            thumbnailImageUrls = persistentListOf(),
            color = RoomColor.CYAN,
            hasPlace = false,
        ),
        RoomPickerItem(
            id = "shared-2",
            name = "매쉬업 화이팅",
            description = "팀원 모두가 좋아할 만한 술집 모음",
            placeCount = 2,
            thumbnailImageUrls = persistentListOf(),
            color = RoomColor.ORANGE,
            hasPlace = false,
        ),
        RoomPickerItem(
            id = "shared-3",
            name = "언젠가 가야지",
            description = "저장만 하고 안 간 곳들",
            placeCount = 3,
            thumbnailImageUrls = persistentListOf(),
            color = RoomColor.PURPLE,
            hasPlace = false,
        ),
    )
