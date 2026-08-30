package team.mino.core.designsystem.component.cardlocation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import team.mino.core.designsystem.component.cardlocation.token.CardLocationListTokens
import team.mino.core.designsystem.component.cardlocation.token.CardLocationTokens
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Image
import team.mino.core.designsystem.util.image.MinoAsyncImage
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 리스트형 장소 카드(Figma `Card_Location A`). 왼쪽 정사각 썸네일 하나, 오른쪽에
 * [제목+주소 · 더보기 버튼] 줄과 [코멘트 수 · 아바타 그룹] 줄을 세로로 쌓는다.
 *
 * @param onMoreClick 더보기[⋮] 클릭. 실제 메뉴 렌더링은 [actionMenu] 슬롯이 담당한다 — 트리거
 *   바로 옆에 붙어야 `Popup` 기본 정렬로 앵커링되기 때문이다.
 * @param actionMenu 더보기 버튼 자리 옆에 함께 그릴 메뉴(호출부가 `expanded` 여부를 판단해 넘긴다).
 * @param thumbnailUrl 왼쪽 썸네일 사진 URL. null이면 폴백 글리프를 보인다.
 * @param avatarGroup 코멘트 수 옆에 붙는 참여자 아바타 그룹 슬롯. null이면 표시하지 않는다.
 */
@Composable
fun MinoCardLocationList(
    title: String,
    address: String,
    commentCount: Int,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailUrl: String? = null,
    actionMenu: @Composable () -> Unit = {},
    avatarGroup: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .rippleSingleClickable(onClick = onClick)
            .padding(
                horizontal = CardLocationTokens.HorizontalPadding,
                vertical = CardLocationTokens.VerticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(CardLocationTokens.ContentSpacing),
    ) {
        MinoAsyncImage(
            imageUrl = thumbnailUrl,
            fallback = rememberVectorPainter(MinoIcons.Image),
            fallbackTint = MinoCardLocationDefaults.thumbnailPlaceholderTint,
            modifier = Modifier
                .size(CardLocationListTokens.ThumbnailSize)
                .surface(
                    shape = CardLocationListTokens.ThumbnailShape,
                    containerColor = MinoCardLocationDefaults.thumbnailBackgroundColor,
                    borderWidth = CardLocationListTokens.ThumbnailBorderWidth,
                    borderColor = MinoCardLocationDefaults.thumbnailBorderColor,
                ),
            fallbackModifier = Modifier
                .wrapContentSize()
                .size(CardLocationTokens.ThumbnailPlaceholderIconSize),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(CardLocationListTokens.ContentGroupSpacing),
        ) {
            CardLocationTitleRow(
                title = title,
                address = address,
                onMoreClick = onMoreClick,
                actionMenu = actionMenu,
            )
            CardLocationCommentRow(
                commentCount = commentCount,
                avatarGroup = avatarGroup,
            )
        }
    }
}
