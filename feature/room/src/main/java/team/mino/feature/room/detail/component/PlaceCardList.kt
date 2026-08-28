package team.mino.feature.room.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Bubble
import team.mino.core.designsystem.foundation.icons.icons.Image
import team.mino.core.designsystem.foundation.icons.icons.MoreVertical
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.image.MinoAsyncImage
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.core.domain.model.Place

/**
 * [PlaceCardList]·[PlaceListItem] 치수 토큰.
 *
 * 디자인 시스템 Figma 파일("MU_Wanted Design System (Community)") `Card_Location A`
 * (node `15852-88674`) 실측값 — spec.md·contracts에는 이 카드의 node-id가 없었지만, 별도 디자인
 * 시스템 파일에 정식 컴포넌트로 등록돼 있는 걸 확인해 그 값을 그대로 옮겼다. 이전 값(썸네일 72dp,
 * 본문 타이포 등)은 이 노드를 못 찾은 채 추측으로 채운 자리표시자였다.
 */
private object PlaceListItemTokens {
    val HorizontalPadding = 20.dp
    val VerticalPadding = 12.dp
    val ThumbnailSize = 94.dp
    val ThumbnailShape: Shape = RoundedCornerShape(4.7.dp)
    val ThumbnailBorderWidth = 1.dp
    val ContentSpacing = 12.dp
    val ContentGroupSpacing = 24.dp
    val TitleAddressSpacing = 4.dp
    val MetaIconSize = 14.dp
    val MetaIconTextSpacing = 2.dp
    val MoreButtonSize = 24.dp
    val MoreIconSize = 18.dp
}

/**
 * 리스트형 장소 카드([PlaceViewType.LIST], FR-007) 목록 — 좌측 썸네일 + 우측 이름·주소·카테고리·코멘트
 * 수. [LazyColumn] 항목으로 [PlaceListItem]을 그대로 쓴다.
 *
 * @param onPlaceClick 장소 선택([SCR-006] 이동, [FR-001] 유저 플로우 1-4). 실제 네비게이션 배선은 호출부
 *   책임.
 * @param onPlaceMoreClick 카드 더보기[⋮] 클릭([FR-008]). 실제 메뉴([PlaceActionMenu]) 렌더링은
 *   [actionMenu] 슬롯이 담당한다 — 트리거 바로 옆에 붙어야 `Popup` 기본 정렬로 앵커링되기 때문이다.
 * @param actionMenu 더보기 버튼 자리 옆에 함께 그릴 메뉴(호출부가 `expanded` 여부를 판단해 넘긴다).
 */
@Composable
internal fun PlaceCardList(
    places: ImmutableList<Place>,
    onPlaceClick: (Place) -> Unit,
    onPlaceMoreClick: (Place) -> Unit,
    modifier: Modifier = Modifier,
    actionMenu: @Composable (Place) -> Unit = {},
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(items = places, key = { it.id }) { place ->
            PlaceListItem(
                place = place,
                onClick = { onPlaceClick(place) },
                onMoreClick = { onPlaceMoreClick(place) },
                actionMenu = { actionMenu(place) },
            )
        }
    }
}

/**
 * [PlaceCardList] 단일 항목 — 디자인 시스템 `Card_Location A`(`15852-88674`) 그대로. 왼쪽 94dp 정사각
 * 썸네일, 오른쪽에 [제목+더보기 버튼] 줄과 [코멘트 수+참여자 아바타] 줄을 세로 24dp 간격으로 쌓는다.
 *
 * [TBD] 참여자 아바타 그룹(Figma `Avatar/Avatar Group`)은 "이 장소를 저장/코멘트한 사람"을 보여주는
 * 자리인데, [Place] 도메인 모델에는 장소별 참여자 정보가 없다(방 전체 멤버([RoomMemberSummary])와는
 * 다른 개념이라 그걸 대신 꽂으면 사실과 다른 정보가 된다) — 백엔드가 필드를 확정하기 전까지 이 자리는
 * 비워 둔다.
 */
@Composable
internal fun PlaceListItem(
    place: Place,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionMenu: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .rippleSingleClickable(onClick = onClick)
            .padding(
                horizontal = PlaceListItemTokens.HorizontalPadding,
                vertical = PlaceListItemTokens.VerticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(PlaceListItemTokens.ContentSpacing),
    ) {
        MinoAsyncImage(
            imageUrl = place.thumbnailUrl,
            fallback = rememberVectorPainter(MinoIcons.Image),
            fallbackTint = MinoAndroidTheme.colors.labelAssistive,
            modifier = Modifier
                .size(PlaceListItemTokens.ThumbnailSize)
                .surface(
                    shape = PlaceListItemTokens.ThumbnailShape,
                    containerColor = MinoAndroidTheme.colors.fillNormal,
                    borderWidth = PlaceListItemTokens.ThumbnailBorderWidth,
                    borderColor = MinoAndroidTheme.colors.lineNormalNeutral,
                ),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PlaceListItemTokens.ContentGroupSpacing),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PlaceListItemTokens.ContentSpacing),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(PlaceListItemTokens.TitleAddressSpacing),
                ) {
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

                Box {
                    Icon(
                        modifier = Modifier
                            .size(PlaceListItemTokens.MoreButtonSize)
                            .rippleSingleClickable(onClick = onMoreClick)
                            .padding((PlaceListItemTokens.MoreButtonSize - PlaceListItemTokens.MoreIconSize) / 2),
                        imageVector = MinoIcons.MoreVertical,
                        contentDescription = "더보기",
                        tint = MinoAndroidTheme.colors.labelAlternative,
                    )
                    actionMenu()
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PlaceListItemTokens.MetaIconTextSpacing),
            ) {
                Icon(
                    modifier = Modifier.size(PlaceListItemTokens.MetaIconSize),
                    imageVector = MinoIcons.Bubble,
                    contentDescription = null,
                    tint = MinoAndroidTheme.colors.labelAlternative,
                )
                Text(
                    text = place.commentCount.toString(),
                    style = MinoAndroidTheme.typography.label2Medium,
                    color = MinoAndroidTheme.colors.labelAlternative,
                )
            }
        }
    }
}
