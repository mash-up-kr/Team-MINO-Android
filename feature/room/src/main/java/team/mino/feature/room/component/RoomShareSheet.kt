package team.mino.feature.room.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import team.mino.core.common.ui.component.RoomThumbnailFallback
import team.mino.core.designsystem.component.actionarea.ActionAreaAction
import team.mino.core.designsystem.component.actionarea.MinoActionArea
import team.mino.core.designsystem.component.button.MinoTextButton
import team.mino.core.designsystem.component.button.TextButtonSize
import team.mino.core.designsystem.component.button.TextButtonStyle
import team.mino.core.designsystem.component.roomcard.MinoRoomCheckBoxCard
import team.mino.core.designsystem.component.roomthumbnail.MinoRoomThumbnail
import team.mino.core.designsystem.component.scrollbar.MinoScrollBar
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Image
import team.mino.core.designsystem.foundation.icons.icons.Plus
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.RoomColor
import team.mino.feature.room.R
import team.mino.feature.room.detail.component.RoomDetailDraggableSheet
import team.mino.feature.room.detail.component.RoomDetailSheetHeight

/**
 * 장소 하나를 내가 속한 다른 방들에도 담는 방 선택 시트 — [SYS-003].
 *
 * **두 화면이 함께 부른다.** 방 상세의 장소 카드 [⋮] → [다른 방에 공유]와 장소 상세의 액션 행
 * [다른방에 공유]가 같은 시트를 연다. 그래서 이 컴포저블은 어느 화면의 모델도 알지 않고 [RoomShareItem]과
 * 콜백만 받는다 — 무엇을 공유하는지도, 어떤 API로 보내는지도 부르는 쪽이 정한다
 * (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §3.4.5·§3.4.6).
 *
 * **단계는 `Peek`/`Full` 둘이고 진입 기본값은 `Peek`이다**(Figma `2392-128669`·`2542-10516`·`2392-128693`,
 * 디자이너 확인 2026-08-31). 세 프레임의 차이는 목록 영역 하나이고 위아래 고정 영역은 어느 단계에서나
 * 같으므로, 시트 높이는 `목록 영역 + `[FixedChromeHeight]로 나온다 — 단계가 바뀌어도 [공유하기]가 화면
 * 밖으로 밀리지 않는다.
 *
 * **끄는 것을 받는 자리는 손잡이 + 대상 장소 행·[새 방 만들기] 행·구분선이다.** 스크롤하는 목록만 빼고
 * 나머지 전부를 드래그 인식 영역에 둔다 — 손잡이 30dp 하나만 받으면 실기기에서 그 좁은 자리를 정확히
 * 짚기 어려워, 못 짚은 손짓이 시트 바깥 딤 영역까지 넘어가 의도치 않게 닫혀 버리는 결함이 있었다(실기기
 * 확인). 목록을 넣으면 세로로 훑는 손짓과 단계를 바꾸는 손짓이 같은 자리에서 갈리므로 목록만 제외한다.
 *
 * **닫는 판단은 하지 않는다.** `Peek`에서 아래로 끌렸다는 사실만 [onDismissRequest]로 올리고, 치우는 것은
 * 상태를 든 쪽이다. 단계([RoomShareSheetLevel])만 로컬 상태인데, 이 컴포저블이 화면에서 빠졌다 다시 붙을
 * 때(= 시트를 다시 열 때) 초기화되므로 되돌리는 코드 없이도 매번 `Peek`부터 시작한다.
 *
 * @param placeName 공유 대상 장소의 이름.
 * @param placeAddress 공유 대상 장소의 주소.
 * @param placeImageUrl 대상 장소 행에 놓을 사진. 없으면 자리표시자 글리프를 그린다.
 * @param rooms 고를 수 있는 방 전부. [RoomShareItem.alreadySaved]가 `true`인 방도 빼지 않고 함께 그린다 —
 *   「이미 담겨 있다」를 보여 주는 것이 그 카드의 일이다.
 * @param selectedRoomIds 사용자가 새로 고른 방. 이미 담긴 방은 여기 들어오지 않는다.
 * @param isShareEnabled [공유하기] 활성 여부. 하나도 고르지 않았거나 보내는 중이면 꺼진다(FR-022).
 * @param onRoomToggle 카드 본문 탭과 체크박스 탭이 모두 여기로 모인다. 이미 담긴 방에서는 올라오지 않는다.
 * @param onCreateRoomClick [새 방 만들기] — 모든 방에 이미 담긴 사용자에게는 이것이 유일한 출구다(EC-020).
 * @param onShareClick [공유하기].
 * @param onDismissRequest 딤 영역 탭과 `Peek`에서 아래로 끌어내린 결과가 올라온다(EC-021).
 */
@Composable
internal fun RoomShareSheet(
    placeName: String,
    placeAddress: String,
    placeImageUrl: String?,
    rooms: ImmutableList<RoomShareItem>,
    selectedRoomIds: ImmutableSet<String>,
    isShareEnabled: Boolean,
    onRoomToggle: (roomId: String) -> Unit,
    onCreateRoomClick: () -> Unit,
    onShareClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var level by remember { mutableStateOf(RoomShareSheetLevel.PEEK) }

    DimmedSheetContainer(onDismissRequest = onDismissRequest, modifier = modifier) {
        RoomDetailDraggableSheet(
            levelIndex = level.ordinal,
            heights = persistentListOf(
                RoomDetailSheetHeight.Fixed(FixedChromeHeight + listAreaHeight(RoomShareSheetLevel.PEEK, rooms.size)),
                RoomDetailSheetHeight.Fixed(FixedChromeHeight + listAreaHeight(RoomShareSheetLevel.FULL, rooms.size)),
            ),
            onDraggedUp = { level = RoomShareSheetLevel.FULL },
            onDraggedDown = { level = RoomShareSheetLevel.PEEK },
            onDismiss = onDismissRequest,
            // 손잡이는 이 모듈의 시트가 함께 쓰는 [SheetDragHandle](30dp)을 그대로 쓴다 — RoomDetailDraggableSheet
            // 기본 손잡이(20dp)로 두면 [FixedChromeHeight]가 어긋난다.
            handle = { SheetDragHandle() },
            // 스크롤 가능한 목록만 드래그 인식 밖으로 뺀다([RoomShareSheet] KDoc "끄는 것을 받는 자리는
            // 손잡이 하나다"는 손잡이 30dp 하나로 너무 좁아 실기기에서 못 찾고 딤 영역까지 손이 넘어가
            // 닫혀 버리는 결함이 있었다 — 대상 장소 행·새 방 만들기 행·구분선은 스크롤하지 않으니 여기
            // 넣어도 목록 스크롤과 갈릴 게 없다). 그래서 이 셋을 header로 옮겨 손잡이와 함께 드래그
            // 인식 영역에 넣고, 실제로 스크롤하는 목록만 content에 남긴다.
            header = {
                TargetPlaceRow(
                    placeName = placeName,
                    placeAddress = placeAddress,
                    placeImageUrl = placeImageUrl,
                )
                CreateRoomRow(onCreateRoomClick = onCreateRoomClick)
                SheetSectionDivider(horizontalPadding = HorizontalPadding)
            },
            content = {
                // 목록과 액션 영역은 겹친다 — `Column`으로 순차 배치해 [listAreaHeight]에서 딱 자르면
                // 목록이 [listAreaHeight]에서 하드 컷되고 그 아래 `MinoActionArea`의 `sticky` 페이드가 가릴
                // 대상 자체가 없어져 장식으로만 남는다(Figma 실측 — peek에서 세 번째 카드 제목·메모 일부가
                // 액션 영역 뒤로 비치며 옅어진다, `2392-128669`). `MinoActionArea`의 sticky 배경은 원래 목록이
                // 그 밑을 지나가며 페이드로 흐려지는 용도로 설계돼 있다(`ActionAreaContainer` KDoc). 그래서
                // 목록 뷰포트를 [listAreaHeight] + 액션 영역 높이([ActionAreaHeight])만큼 주고, 액션 영역을
                // 그 뷰포트 맨 아래에 겹쳐 그린다 — 목록이 액션 영역 뒤로 [ActionAreaHeight]만큼 더 이어지다
                // 페이드에 잠긴다.
                Box(modifier = Modifier.height(listAreaHeight(level, rooms.size) + ActionAreaHeight)) {
                    RoomList(
                        rooms = rooms,
                        selectedRoomIds = selectedRoomIds,
                        onRoomToggle = onRoomToggle,
                        modifier = Modifier.fillMaxSize(),
                    )
                    // 시트 아랫변이 내비게이션 바 뒤까지 닿으므로(`DimmedSheetContainer`의 `systemBarBleed`)
                    // 액션 영역이 여기서 `navigationBarsPadding()`을 또 걸면 안 된다 — Figma의 102dp 자체가
                    // 이미 홈 바 자리를 품은 고정값(디자인 정책 `2400:268882` — peek 500/full 676/full+ 708
                    // 고정)이라, 실기기 내비게이션 바 인셋을 한 번 더 더하면 그 고정 높이를 넘겨 액션 영역이
                    // 시트 밖으로 밀려 잘린다(실기기 확인 — "다른 방에 공유" 시트에서 공유하기 버튼이 안
                    // 보이던 결함).
                    MinoActionArea(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        mainAction = ActionAreaAction(
                            text = stringResource(R.string.roomshare_confirm),
                            onClick = onShareClick,
                            enabled = isShareEnabled,
                        ),
                        sticky = true,
                    )
                }
            },
        )
    }
}

/** [RoomShareSheet]가 멈춰 서는 두 단계. 진입 기본값은 [PEEK]이다. */
private enum class RoomShareSheetLevel { PEEK, FULL }

/**
 * 단계별 목록 영역 높이(Figma 실측).
 *
 * `Full`이 방 개수로 갈리는 것은 스크롤 여지를 알리기 위해서다 — 다섯 이상이면 [ScrollHintHeight]를 더 얹어
 * 5번째 카드가 일부만 보이게 하고, 넷 이하면 카드가 딱 맞아 잘릴 것이 없다.
 */
private fun listAreaHeight(
    level: RoomShareSheetLevel,
    roomCount: Int,
): Dp =
    when (level) {
        RoomShareSheetLevel.PEEK -> PeekListHeight
        RoomShareSheetLevel.FULL ->
            if (roomCount > FULL_EXACT_ROOM_COUNT) FullListHeight + ScrollHintHeight else FullListHeight
    }

/**
 * 어느 장소를 공유하는지 알리는 대상 장소 행(Figma `Frame 277`).
 *
 * 이름과 주소는 각각 한 줄을 지켜 시트 높이가 장소마다 달라지지 않는다.
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
 * [새 방 만들기] 행(Figma `Frame 209`).
 *
 * **고를 방이 하나도 없을 때 이 행이 유일한 출구다.** 속한 모든 방에 그 장소가 이미 있으면 카드가 전부
 * 비활성이고 [공유하기]도 꺼져 있어, 새 방을 만드는 것 말고는 시트를 닫는 길밖에 없다
 * (place-detail spec FR-022 · UX-010 · EC-020).
 */
@Composable
private fun CreateRoomRow(
    onCreateRoomClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(CreateRoomRowHeight)
            .padding(horizontal = HorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MinoTextButton(
            text = stringResource(R.string.roomshare_create_room),
            onClick = onCreateRoomClick,
            style = TextButtonStyle.Assistive,
            size = TextButtonSize.Medium,
            leadingIcon = { Icon(imageVector = MinoIcons.Plus, contentDescription = null) },
        )
    }
}

/**
 * 방 카드 목록. 시트에서 유일하게 스크롤하는 영역이다.
 *
 * **선택을 들지 않는다.** 어떤 방이 골라졌는지는 [selectedRoomIds] 한 곳에만 있고, 카드는 자기 id가 거기
 * 있는지만 본다. [RoomShareItem.alreadySaved]가 `true`인 방은 그 집합과 무관하게 체크된 채 비활성이며,
 * 그 카드는 탭과 체크박스가 함께 잠기고 체크박스만 흐려진다.
 *
 * 오른쪽 끝에 겹쳐 놓인 [MinoScrollBar]는 스크롤 여지를 알리는 보조 수단이다. 목록과 같은 상태를 보며
 * 표시만 하고, 카드 폭 밖에 놓여 탭을 가로채지 않는다.
 *
 * **아랫변에 [ActionAreaHeight]만큼 여백을 준다.** 이 목록의 뷰포트([RoomShareSheet]의 `Box`)는 액션
 * 영역과 겹치도록 그 높이만큼 더 받는데(`RoomShareSheet` 본문 KDoc), 이 여백이 없으면 방이 딱 뷰포트를
 * 채우는 경계 개수(Full에서 정확히 5개)일 때 스크롤할 거리 자체가 0이 되어 마지막 카드가 액션 영역
 * 뒤에 영영 가려진 채 체크박스를 못 누르는 결함이 생긴다(실기기 확인) — 방이 몇 개든 마지막 카드까지
 * 액션 영역 위로 끌어올릴 수 있어야 한다.
 */
@Composable
private fun RoomList(
    rooms: ImmutableList<RoomShareItem>,
    selectedRoomIds: ImmutableSet<String>,
    onRoomToggle: (roomId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(
                start = HorizontalPadding,
                end = HorizontalPadding,
                bottom = ActionAreaHeight,
            ),
        ) {
            items(items = rooms, key = { it.id }) { room ->
                MinoRoomCheckBoxCard(
                    title = room.name,
                    placeCountLabel = stringResource(R.string.room_place_count, room.placeCount),
                    checked = room.alreadySaved || room.id in selectedRoomIds,
                    onCheckedChange = { onRoomToggle(room.id) },
                    onClick = { onRoomToggle(room.id) },
                    enabled = !room.alreadySaved,
                    thumbnail = {
                        MinoRoomThumbnail(
                            imageUrls = room.thumbnailImageUrls,
                            fallback = {
                                RoomThumbnailFallback(color = room.color.chip, modifier = Modifier.fillMaxSize())
                            },
                        )
                    },
                    memo = room.description,
                )
            }
        }
        // matchParentSize는 목록이 정한 높이를 되밀지 않고 그대로 받아 오지만 폭까지 함께 고정한다.
        // 스크롤바를 그 안에 넣어야 자기 폭을 지킨 채 오른쪽 끝에 선다.
        Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.TopEnd) {
            MinoScrollBar(scrollState = listState, modifier = Modifier.padding(vertical = ScrollBarVerticalInset))
        }
    }
}

/**
 * 단계와 무관하게 늘 같은 위아래 영역의 합 — 손잡이 30 + 장소 행 60 + [새 방 만들기] 행 56 + 구분선 띠 12
 * + 액션 영역 102([ActionAreaHeight], 홈 바 자리 포함). 목록 영역만 단계에 따라 달라지므로 시트 높이가 이
 * 값 하나로 나온다. 액션 영역은 목록과 순차로 쌓이지 않고 목록 뷰포트 맨 아래에 겹쳐 그려지지만
 * (`RoomShareSheet` 본문의 `Box` 참고), 합산 높이 자체는 겹치기 전과 같다 — 겹친 만큼을 목록 뷰포트가
 * 그만큼 더 받기 때문이다.
 */
private val FixedChromeHeight = 260.dp

/** [RoomShareSheet] 본문에서 목록 뷰포트 마지막 [ActionAreaHeight]에 `MinoActionArea`를 겹쳐 그릴 때 쓴다. */
private val ActionAreaHeight = 102.dp

/** Figma `2392-128669`("011-1-1 다른 방에 공유_peek") 실측 — 카드 두 장과 세 번째의 대부분이 액션 영역
 * 페이드 뒤로 비쳐 보인다. */
private val PeekListHeight = 240.dp

/** Figma `2542-10516`("011-1-2-1 다른 방에 공유_full_4개") 실측 — 104dp × 4로 딱 맞는다. */
private val FullListHeight = 416.dp

/** Figma `2392-128693`("011-1-2-2 다른 방에 공유_full_4개 이상") 실측 — 416 + 32. */
private val ScrollHintHeight = 32.dp

/** 이 수를 넘으면 `Full`이 [ScrollHintHeight]만큼 커진다. */
private const val FULL_EXACT_ROOM_COUNT = 4

private val HorizontalPadding = 20.dp

private val TargetPlaceRowHeight = 60.dp

private val CreateRoomRowHeight = 56.dp

private val TargetPlaceSpacing = 14.dp

private val TargetPlaceImageSize = 46.dp

private val TargetPlaceImageShape: Shape = RoundedCornerShape(7.83.dp)

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
            rooms = previewRoomShareItems(),
            selectedRoomIds = persistentSetOf("shared-2"),
            isShareEnabled = true,
            onRoomToggle = {},
            onCreateRoomClick = {},
            onShareClick = {},
            onDismissRequest = {},
            modifier = modifier,
        )
    }
}

/**
 * 속한 모든 방에 그 장소가 이미 있어 고를 카드가 하나도 없는 상태(place-detail spec EC-019 · TS-034).
 *
 * 카드는 전부 체크된 채 체크박스만 흐리고 [공유하기]도 비활성이다 — 안내 문구 없이 이 모습만으로
 * "더 담을 곳이 없다"를 알리고, 남은 길이 [새 방 만들기] 하나임을 같은 화면이 함께 보여 준다(UX-010).
 */
@UiModePreviews
@Composable
private fun RoomShareSheetAllSavedPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        RoomShareSheet(
            placeName = "성수 감자탕",
            placeAddress = "서울 성동구 아차산로 100",
            placeImageUrl = null,
            rooms = previewRoomShareItems().map { it.copy(alreadySaved = true) }.toImmutableList(),
            selectedRoomIds = persistentSetOf(),
            isShareEnabled = false,
            onRoomToggle = {},
            onCreateRoomClick = {},
            onShareClick = {},
            onDismissRequest = {},
            modifier = modifier,
        )
    }
}

/** 시트를 그려 보는 데 필요한 만큼의 방 목록. 실제 목록은 두 화면이 각자의 조회에서 받아 옮겨 담는다. */
internal fun previewRoomShareItems(): ImmutableList<RoomShareItem> =
    persistentListOf(
        RoomShareItem(
            id = "personal",
            name = "내 장소",
            description = null,
            placeCount = 0,
            thumbnailImageUrls = persistentListOf(),
            color = RoomColor.GRAY,
            alreadySaved = false,
        ),
        RoomShareItem(
            id = "shared-1",
            name = "민호야 잘하자",
            description = null,
            placeCount = 9,
            thumbnailImageUrls = persistentListOf(),
            color = RoomColor.CYAN,
            alreadySaved = true,
        ),
        RoomShareItem(
            id = "shared-2",
            name = "매쉬업 화이팅",
            description = "팀원 모두가 좋아할 만한 술집 모음",
            placeCount = 2,
            thumbnailImageUrls = persistentListOf(),
            color = RoomColor.ORANGE,
            alreadySaved = false,
        ),
        RoomShareItem(
            id = "shared-3",
            name = "언젠가 가야지",
            description = "저장만 하고 안 간 곳들",
            placeCount = 3,
            thumbnailImageUrls = persistentListOf(),
            color = RoomColor.PURPLE,
            alreadySaved = false,
        ),
    )
