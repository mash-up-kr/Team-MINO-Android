package team.mino.core.designsystem.component.cardlocation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.component.cardlocation.token.CardLocationCollageTokens
import team.mino.core.designsystem.component.cardlocation.token.CardLocationTokens
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Image
import team.mino.core.designsystem.util.image.MinoAsyncImage
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 콜라주형 장소 카드(Figma `Card_Location B`). [제목+주소 · 더보기 버튼] 줄, 사진 2장이 나란한 줄,
 * [코멘트 수 · 아바타 그룹] 줄을 세로로 쌓는다. 열(grid) 배치가 아니라 세로 1열에 놓는 카드다.
 *
 * @param onMoreClick 더보기[⋮] 클릭. 실제 메뉴 렌더링은 [actionMenu] 슬롯이 담당한다 — 트리거
 *   바로 옆에 붙어야 `Popup` 기본 정렬로 앵커링되기 때문이다.
 * @param actionMenu 더보기 버튼 자리 옆에 함께 그릴 메뉴(호출부가 `expanded` 여부를 판단해 넘긴다).
 * @param thumbnailUrls 사진 줄에 채울 URL. 슬롯은 항상
 *   [CardLocationCollageTokens.THUMBNAIL_SLOT_COUNT]개(2장)로 고정이라, 이 목록에서 모자란 슬롯이나
 *   `null` 원소는 폴백 글리프로 채운다. **사진 줄 자체는 목록이 비어도 항상 그린다** — 리스트형
 * [MinoCardLocationList]의 썸네일 박스가 사진 유무와 무관하게 항상 보이는 것과 같은 규칙이다.
 * @param avatarGroup 코멘트 수 옆에 붙는 참여자 아바타 그룹 슬롯. null이면 표시하지 않는다.
 */
@Composable
fun MinoCardLocationCollage(
    title: String,
    address: String,
    commentCount: Int,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailUrls: ImmutableList<String?> = persistentListOf(),
    actionMenu: @Composable () -> Unit = {},
    avatarGroup: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .rippleSingleClickable(onClick = onClick)
            .padding(
                horizontal = CardLocationTokens.HorizontalPadding,
                vertical = CardLocationTokens.VerticalPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(CardLocationCollageTokens.ContentGroupSpacing),
    ) {
        CardLocationTitleRow(
            title = title,
            address = address,
            onMoreClick = onMoreClick,
            actionMenu = actionMenu,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(CardLocationCollageTokens.ThumbnailSpacing)) {
            repeat(CardLocationCollageTokens.THUMBNAIL_SLOT_COUNT) { index ->
                MinoAsyncImage(
                    imageUrl = thumbnailUrls.getOrNull(index),
                    fallback = rememberVectorPainter(MinoIcons.Image),
                    fallbackTint = MinoCardLocationDefaults.thumbnailPlaceholderTint,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(CardLocationCollageTokens.ThumbnailAspectRatio)
                        .surface(
                            shape = CardLocationCollageTokens.ThumbnailShape,
                            containerColor = MinoCardLocationDefaults.thumbnailBackgroundColor,
                            borderColor = MinoCardLocationDefaults.thumbnailBorderColor,
                            borderWidth = CardLocationCollageTokens.ThumbnailBorderWidth,
                        ),
                    fallbackModifier = Modifier
                        .wrapContentSize()
                        .size(CardLocationTokens.ThumbnailPlaceholderIconSize),
                )
            }
        }

        CardLocationCommentRow(
            commentCount = commentCount,
            avatarGroup = avatarGroup,
        )
    }
}
