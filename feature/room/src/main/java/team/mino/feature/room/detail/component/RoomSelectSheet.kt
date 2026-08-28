package team.mino.feature.room.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import team.mino.core.designsystem.component.button.ButtonSize
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.component.button.MinoButton
import team.mino.core.designsystem.component.button.MinoTextButton
import team.mino.core.designsystem.component.button.TextButtonSize
import team.mino.core.designsystem.component.button.TextButtonStyle
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Check
import team.mino.core.designsystem.foundation.icons.icons.Image
import team.mino.core.designsystem.foundation.icons.icons.Plus
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.image.MinoAsyncImage
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.core.domain.model.Place
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomThumbnail

/**
 * [RoomSelectSheet]·[RoomSelectCard]·[RoomSelectCheckbox] 치수 토큰. 실측 근거는 Figma
 * `004-2-2 다른 방에 공유 클릭`(node 2862-175295, 리드가 직접 조회) — spec.md 「Figma」 절 참고.
 *
 * 헤더("새 방 만들기" 텍스트버튼 포함)는 장소 공유 대상 방 목록과 무관한 부가 요소라 이번 구현
 * 범위에서 생략했다 — 이 시트의 핵심 요구는 슬라이드 영역(방 목록) 고정 높이다.
 */
private object RoomSelectSheetTokens {
    val SheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val HandleSize = DpSize(38.dp, 4.dp)
    val HandleShape = RoundedCornerShape(4.dp)
    val HandleTopPadding = 8.dp
    val HandleBottomPadding = 8.dp

    val SlideAreaHeight = 416.dp

    val CardHorizontalPadding = 20.dp
    val CardVerticalPadding = 12.dp
    val CardContentSpacing = 12.dp
    val ThumbnailSize = 80.dp
    val ThumbnailShape: Shape = RoundedCornerShape(14.dp)

    // Figma `2862-175315` 실측 — 이전엔 2dp였는데 실제로는 4px 간격이다.
    val TitleMemoSpacing = 4.dp

    // 제목(+메모) 묶음과 장소 개수 줄을 담는 세로 영역 전체 높이(Figma `2862-175315`,
    // `#15852:88382` 78dp 고정) — 그 안에서 space-between으로 두 블록이 떨어진다.
    val CardContentHeight = 78.dp

    // Figma `2862-175313`/`2862-175314` 실측 — 이전엔 24dp로 잘못 구현해 체크박스가 실제보다 크고
    // 모서리 둥글기·안쪽 체크 아이콘 비율이 Figma와 달라 보였다.
    val CheckboxSize = 18.dp
    val CheckboxShape = RoundedCornerShape(4.dp)
    val CheckboxBorderWidth = 1.5.dp
    val CheckboxIconSize = 16.dp

    // 공유 대상 장소 헤더(Figma `2862-175301`/`2862-175306`) — 공유 시트가 지금까지 생략했던 부분.
    val PlaceRowHeight = 60.dp
    val PlaceRowSpacing = 14.dp
    val PlaceImageSize = 46.dp
    val PlaceImageShape: Shape = RoundedCornerShape(8.dp)
    val PlaceContentSpacing = 4.dp
    val CreateRoomRowVerticalPadding = 12.dp

    // 체크됨+비활성(이미 저장된 방, EC-004) — Figma node 2862-175295에서 확인된 실제 투명도 값.
    const val CHECKED_DISABLED_ALPHA = 0.43f

    // 하단 액션 영역(Figma `2862-175317`) "Container" 패딩 — 버튼을 감싸는 바깥 여백.
    val ActionAreaPadding = 20.dp
}

/**
 * "다른 방에 공유" 바텀시트([SYS-003], FR-009) — 방 다중 선택 카드 목록 + 공유하기 버튼.
 *
 * 슬라이드 영역(방 목록)은 [RoomSelectSheetTokens.SlideAreaHeight](416dp)로 고정한다(spec.md
 * 유저 플로우 3, "슬라이드 영역 416px 고정"). 전체 시트 높이(676dp)는 이 영역 + 구분선 + 액션
 * 영역을 쌓으면 자연히 근접하므로 별도로 강제하지 않는다 — 헤더를 생략한 채 정확히 676dp로
 * 강제하면 오히려 내용과 어긋난다.
 *
 * @param place 공유 대상 장소(Figma `2862-175301`) — 시트 상단에 썸네일+이름+주소로 보여준다.
 *   `null`이면 이 헤더 블록 자체를 그리지 않는다.
 * @param rooms 공유 대상으로 고를 수 있는 전체 방 목록(`RoomRepository.observeMyRooms()`).
 * @param alreadySavedRoomIds 이 장소가 이미 저장돼 있는 방 id 집합(EC-004) — [selectedRoomIds]와
 *   무관하게 항상 체크됨+비활성으로 그리며, 클릭해도 토글되지 않는다.
 * @param selectedRoomIds 사용자가 새로 고른(아직 저장되지 않은) 방 id 집합.
 * @param onRoomToggle 방 카드 클릭(이미 저장된 방은 무시 — 호출부가 아니라 이 컴포저블이 막는다).
 * @param onCreateRoomClick [+ 새 방 만들기] 클릭(Figma `2862-175306`) — 공유 대상에 없는 새 방을 만들어
 *   바로 공유 후보에 추가하는 진입점.
 * @param onShareClick [공유하기] 클릭(FR-009).
 */
@Composable
internal fun RoomSelectSheet(
    place: Place?,
    rooms: ImmutableList<Room>,
    alreadySavedRoomIds: ImmutableSet<String>,
    selectedRoomIds: ImmutableSet<String>,
    onRoomToggle: (String) -> Unit,
    onCreateRoomClick: () -> Unit,
    onShareClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .surface(
                shape = RoomSelectSheetTokens.SheetShape,
                containerColor = MinoAndroidTheme.colors.backgroundElevatedNormal,
            ),
    ) {
        RoomSelectDragHandle()

        if (place != null) {
            RoomSelectPlaceHeader(place = place, onCreateRoomClick = onCreateRoomClick)
        }

        HorizontalDivider(color = MinoAndroidTheme.colors.lineNormalNormal)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(RoomSelectSheetTokens.SlideAreaHeight),
        ) {
            items(items = rooms, key = { it.id }) { room ->
                val isAlreadySaved = room.id in alreadySavedRoomIds
                val isChecked = isAlreadySaved || room.id in selectedRoomIds
                RoomSelectCard(
                    room = room,
                    checked = isChecked,
                    checkedDisabled = isAlreadySaved,
                    onClick = { if (!isAlreadySaved) onRoomToggle(room.id) },
                )
            }
        }

        RoomSelectActionArea(onShareClick = onShareClick)
    }
    // onDismiss는 이 시트를 호스팅하는 바텀시트 컨테이너(RoomDetailScreen 조립부)가 바깥 영역
    // 클릭·백 제스처에 연결한다 — 이 컴포저블 자체는 항상 떠 있는 콘텐츠라 스스로 닫힘을 그리지 않는다.
}

/**
 * 공유 대상 장소 헤더(Figma `2862-175301` + `2862-175306`) — 썸네일+이름+주소 한 줄, 그 아래
 * [+ 새 방 만들기] 텍스트 버튼.
 */
@Composable
private fun RoomSelectPlaceHeader(
    place: Place,
    onCreateRoomClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = RoomSelectSheetTokens.CardHorizontalPadding),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(RoomSelectSheetTokens.PlaceRowHeight),
            horizontalArrangement = Arrangement.spacedBy(RoomSelectSheetTokens.PlaceRowSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MinoAsyncImage(
                imageUrl = place.thumbnailUrl,
                fallback = rememberVectorPainter(MinoIcons.Image),
                fallbackTint = MinoAndroidTheme.colors.labelAssistive,
                modifier = Modifier
                    .size(RoomSelectSheetTokens.PlaceImageSize)
                    .surface(
                        shape = RoomSelectSheetTokens.PlaceImageShape,
                        containerColor = MinoAndroidTheme.colors.fillNormal,
                    ),
            )
            Column(verticalArrangement = Arrangement.spacedBy(RoomSelectSheetTokens.PlaceContentSpacing)) {
                Text(
                    text = place.name,
                    style = MinoAndroidTheme.typography.body1NormalBold,
                    color = MinoAndroidTheme.colors.labelNormal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = place.address,
                    style = MinoAndroidTheme.typography.label2Medium,
                    color = MinoAndroidTheme.colors.labelAlternative,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Row(modifier = Modifier.padding(vertical = RoomSelectSheetTokens.CreateRoomRowVerticalPadding)) {
            MinoTextButton(
                text = "새 방 만들기",
                onClick = onCreateRoomClick,
                style = TextButtonStyle.Assistive,
                size = TextButtonSize.Medium,
                leadingIcon = {
                    Icon(
                        imageVector = MinoIcons.Plus,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Composable
private fun RoomSelectDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = RoomSelectSheetTokens.HandleTopPadding,
                bottom = RoomSelectSheetTokens.HandleBottomPadding,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(RoomSelectSheetTokens.HandleSize.width, RoomSelectSheetTokens.HandleSize.height)
                .background(color = MinoAndroidTheme.colors.fillNormal, shape = RoomSelectSheetTokens.HandleShape),
        )
    }
}

/** 방 선택 카드(Figma `Card_Room`) — 좌측 썸네일 + 우측 제목·메모·장소 개수 + 우측 체크박스. */
@Composable
private fun RoomSelectCard(
    room: Room,
    checked: Boolean,
    checkedDisabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .rippleSingleClickable(enabled = !checkedDisabled, onClick = onClick)
            .padding(
                horizontal = RoomSelectSheetTokens.CardHorizontalPadding,
                vertical = RoomSelectSheetTokens.CardVerticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(RoomSelectSheetTokens.CardContentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MinoAsyncImage(
            imageUrl = (room.thumbnail as? RoomThumbnail.Collage)?.imageUrls?.firstOrNull(),
            fallback = rememberVectorPainter(MinoIcons.Image),
            fallbackTint = MinoAndroidTheme.colors.labelAssistive,
            modifier = Modifier
                .size(RoomSelectSheetTokens.ThumbnailSize)
                .surface(
                    shape = RoomSelectSheetTokens.ThumbnailShape,
                    containerColor = MinoAndroidTheme.colors.fillNormal,
                ),
        )

        // Figma `2862-175315` 실측 — 제목(+메모) 묶음과 장소 개수 줄이 78dp 고정 높이 안에서
        // space-between으로 떨어져 있다. 이전엔 place count 줄을 2dp 위 패딩만으로 붙여서 Figma보다
        // 훨씬 좁게 붙어 보였다.
        Column(
            modifier = Modifier
                .weight(1f)
                .height(RoomSelectSheetTokens.CardContentHeight),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(RoomSelectSheetTokens.TitleMemoSpacing)) {
                Text(
                    text = room.name,
                    style = MinoAndroidTheme.typography.body1NormalBold,
                    color = MinoAndroidTheme.colors.labelNormal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (room.description.isNotEmpty()) {
                    Text(
                        text = room.description,
                        style = MinoAndroidTheme.typography.label2Medium,
                        color = MinoAndroidTheme.colors.labelAlternative,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                // Figma `2862-175315` 실측 — "장소 3개"처럼 "장소 " 접두어가 붙는다. 이 접두어 없이
                // 개수만 보여주는 `formatPlaceCountText`(RoomDetailBottomSheet.kt)는 위치 아이콘이
                // 옆에 따로 있는 자리에 쓰는 거라 여기 그대로 재사용할 수 없다.
                text = "장소 ${formatPlaceCountText(room.placeCount)}",
                style = MinoAndroidTheme.typography.label2Bold,
                color = MinoAndroidTheme.colors.labelAlternative,
            )
        }

        RoomSelectCheckbox(checked = checked, checkedDisabled = checkedDisabled)
    }
}

/**
 * 체크박스 3상태(spec.md EC-004) — 미체크(빈 사각형) / 체크됨(검정 배경 + 흰 체크) /
 * 체크됨+비활성(같은 체크 모양에 [RoomSelectSheetTokens.CHECKED_DISABLED_ALPHA] 적용, 이미 저장된 방).
 */
@Composable
private fun RoomSelectCheckbox(
    checked: Boolean,
    checkedDisabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val boxModifier = if (checked) {
        modifier
            .size(RoomSelectSheetTokens.CheckboxSize)
            .then(if (checkedDisabled) Modifier.alpha(RoomSelectSheetTokens.CHECKED_DISABLED_ALPHA) else Modifier)
            .surface(
                shape = RoomSelectSheetTokens.CheckboxShape,
                containerColor = MinoAndroidTheme.colors.primaryNormal,
            )
    } else {
        modifier
            .size(RoomSelectSheetTokens.CheckboxSize)
            .surface(
                shape = RoomSelectSheetTokens.CheckboxShape,
                containerColor = MinoAndroidTheme.colors.backgroundElevatedNormal,
                // Figma `2862-175315` 실측 — lineNormalNeutral(.16)이 아니라 lineNormalNormal(.22)이다.
                borderColor = MinoAndroidTheme.colors.lineNormalNormal,
                borderWidth = RoomSelectSheetTokens.CheckboxBorderWidth,
            )
    }

    Box(modifier = boxModifier, contentAlignment = Alignment.Center) {
        if (checked) {
            Icon(
                modifier = Modifier.size(RoomSelectSheetTokens.CheckboxIconSize),
                imageVector = MinoIcons.Check,
                contentDescription = null,
                tint = MinoAndroidTheme.colors.staticWhite,
            )
        }
    }
}

/**
 * 하단 액션 영역(Figma `2862-175317`, "Action Area/Action Area") — [공유하기] 버튼 하나.
 *
 * 이전엔 버튼 자체의 텍스트 콘텐츠 패딩(`MinoButton` [ButtonSize.Large]의 상하 12dp·좌우 28dp)이 화면
 * 가장자리 여백을 대신한다고 잘못 가정해서, 버튼이 시트 폭 끝까지 꽉 찬 채로 그려졌다 — 실제로는 Figma의
 * "Container"가 버튼을 20px 여백으로 한 번 더 감싸고, 그 안에서 버튼이 남은 폭을 채운다. 두 패딩은
 * 서로 다른 목적(바깥 여백 vs 안쪽 텍스트 정렬)이라 하나가 다른 하나를 대신할 수 없다.
 */
@Composable
private fun RoomSelectActionArea(
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.padding(RoomSelectSheetTokens.ActionAreaPadding)) {
        MinoButton(
            text = "공유하기",
            onClick = onShareClick,
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.Large,
            style = ButtonStyle.SolidPrimary,
        )
    }
}
