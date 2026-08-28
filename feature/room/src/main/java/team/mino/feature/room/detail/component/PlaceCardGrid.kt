package team.mino.feature.room.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
 * [PlaceCardGrid]·[PlaceGridItem] 치수 토큰.
 *
 * 디자인 시스템 Figma 파일("MU_디자인") `Card_Location`(`thumbnail=on`, node `2542-125378`)·
 * `Card_Location B` 실측값. 이전엔 이 노드를 못 찾아 2열 격자에 작은 정사각 썸네일만 있는 카드로
 * 추측 구현했었는데(spec.md에 이 카드형 node-id가 없었음), 실제로는 [PlaceCardList]와 같은 세로 1열
 * 목록이고 카드 한 장이 훨씬 크다 — 제목/주소+더보기 줄, 사진 줄, 코멘트 수 줄을 세로로 쌓는다.
 */
private object PlaceGridItemTokens {
    val HorizontalPadding = 20.dp
    val VerticalPadding = 12.dp
    val ContentSpacing = 12.dp
    val ContentGroupSpacing = 12.dp
    val TitleAddressSpacing = 4.dp
    val ThumbnailShape: Shape = RoundedCornerShape(12.dp)
    val ThumbnailAspectRatio = 4f / 5f
    val ThumbnailSpacing = 8.dp
    val ThumbnailSlotCount = 2
    val MetaIconSize = 14.dp
    val MetaIconTextSpacing = 2.dp
    val MoreButtonSize = 24.dp
    val MoreIconSize = 18.dp
}

/**
 * 카드형 장소 카드([PlaceViewType.CARD], FR-007) 목록 — [PlaceCardList]와 같은 세로 1열
 * [LazyColumn]이다. "카드형"은 열 배치가 아니라 사진이 크게 보이는 카드 스타일을 뜻한다(디자인 시스템
 * `Card_Location` 대조 결과, 위 [PlaceGridItemTokens] KDoc 참고).
 *
 * @param onPlaceClick 장소 선택([SCR-006] 이동, [FR-001] 유저 플로우 1-4). 실제 네비게이션 배선은 호출부
 *   책임.
 * @param onPlaceMoreClick 카드 더보기[⋮] 클릭([FR-008]). 실제 메뉴([PlaceActionMenu]) 렌더링은
 *   [actionMenu] 슬롯이 담당한다 — 트리거 바로 옆에 붙어야 `Popup` 기본 정렬로 앵커링되기 때문이다.
 * @param actionMenu 더보기 버튼 자리 옆에 함께 그릴 메뉴(호출부가 `expanded` 여부를 판단해 넘긴다).
 */
@Composable
internal fun PlaceCardGrid(
    places: ImmutableList<Place>,
    onPlaceClick: (Place) -> Unit,
    onPlaceMoreClick: (Place) -> Unit,
    modifier: Modifier = Modifier,
    actionMenu: @Composable (Place) -> Unit = {},
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(items = places, key = { it.id }) { place ->
            PlaceGridItem(
                place = place,
                onClick = { onPlaceClick(place) },
                onMoreClick = { onPlaceMoreClick(place) },
                actionMenu = { actionMenu(place) },
            )
        }
    }
}

/**
 * [PlaceCardGrid] 단일 항목 — 디자인 시스템 `Card_Location`(`2542-125378`) 그대로. 제목+더보기 줄, 사진
 * 줄(있으면), 코멘트 수 줄을 세로로 쌓는다.
 *
 * 사진 줄은 Figma상 항상 [PlaceGridItemTokens.ThumbnailSlotCount]개(2장)의 정사각형에 가까운
 * 슬롯이 나란히 배치되는 콜라주 — 사진 하나만 `fillMaxWidth`로 늘려 채우면 안 된다. [TBD] [Place]
 * 도메인 모델은 [Place.thumbnailUrl] 단일 URL 하나만 가지므로, 첫 슬롯만 실제 사진을 채우고 나머지
 * 슬롯은 폴백 아이콘으로 남긴다 — 여러 장 저장 계약이 생기면 슬롯 수만큼 채우도록 바꿔야 한다.
 * 참여자 아바타 그룹도 [PlaceListItem]과 같은 이유로 비워 둔다.
 */
@Composable
internal fun PlaceGridItem(
    place: Place,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionMenu: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .rippleSingleClickable(onClick = onClick)
            .padding(
                horizontal = PlaceGridItemTokens.HorizontalPadding,
                vertical = PlaceGridItemTokens.VerticalPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(PlaceGridItemTokens.ContentGroupSpacing),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(PlaceGridItemTokens.ContentSpacing)) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(PlaceGridItemTokens.TitleAddressSpacing),
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
                        .size(PlaceGridItemTokens.MoreButtonSize)
                        .rippleSingleClickable(onClick = onMoreClick)
                        .padding((PlaceGridItemTokens.MoreButtonSize - PlaceGridItemTokens.MoreIconSize) / 2),
                    imageVector = MinoIcons.MoreVertical,
                    contentDescription = "더보기",
                    tint = MinoAndroidTheme.colors.labelAlternative,
                )
                actionMenu()
            }
        }

        if (place.thumbnailUrl != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(PlaceGridItemTokens.ThumbnailSpacing)) {
                repeat(PlaceGridItemTokens.ThumbnailSlotCount) { index ->
                    MinoAsyncImage(
                        imageUrl = place.thumbnailUrl.takeIf { index == 0 },
                        fallback = rememberVectorPainter(MinoIcons.Image),
                        fallbackTint = MinoAndroidTheme.colors.labelAssistive,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(PlaceGridItemTokens.ThumbnailAspectRatio)
                            .surface(
                                shape = PlaceGridItemTokens.ThumbnailShape,
                                containerColor = MinoAndroidTheme.colors.fillNormal,
                                borderColor = MinoAndroidTheme.colors.lineNormalNeutral,
                                borderWidth = 1.dp,
                            ),
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PlaceGridItemTokens.MetaIconTextSpacing),
        ) {
            Icon(
                modifier = Modifier.size(PlaceGridItemTokens.MetaIconSize),
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
