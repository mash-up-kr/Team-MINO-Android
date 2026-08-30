package team.mino.core.designsystem.component.cardlocation.token

import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * `Card_Location` 컴포넌트 계열([team.mino.core.designsystem.component.cardlocation.MinoCardLocationList]·
 * [team.mino.core.designsystem.component.cardlocation.MinoCardLocationCollage])이 공유하는 슬롯 →
 * 디자인 토큰 키 매핑과, 대응 토큰이 없어 값으로 둔 치수.
 */
internal object CardLocationTokens {
    val HorizontalPadding = 20.dp
    val VerticalPadding = 12.dp
    val ContentSpacing = 12.dp
    val TitleAddressSpacing = 4.dp
    val MetaIconSize = 14.dp
    val MetaIconTextSpacing = 2.dp
    val MoreButtonSize = 24.dp
    val MoreIconSize = 18.dp

    val TitleFont = TypographyAccessKeyToken.Body1NormalBold
    val TitleColor = ColorAccessKeyToken.LabelNormal

    val AddressFont = TypographyAccessKeyToken.Label2Medium
    val AddressColor = ColorAccessKeyToken.LabelAlternative

    val CommentCountFont = TypographyAccessKeyToken.Label2Medium
    val CommentCountColor = ColorAccessKeyToken.LabelAlternative

    val MoreIconColor = ColorAccessKeyToken.LabelAlternative

    val ThumbnailBackgroundColor = ColorAccessKeyToken.FillNormal
    val ThumbnailBorderColor = ColorAccessKeyToken.LineNormalNeutral
    val ThumbnailPlaceholderTint = ColorAccessKeyToken.LabelAssistive

    /**
     * 사진이 없을 때 썸네일 박스 안에 그리는 폴백 글리프 크기. `MinoAsyncImage`의 `fallbackModifier`로
     * 넘기지 않으면 아이콘이 박스 전체(94dp 등)를 꽉 채우며 커져 깨져 보인다(실기기 확인된 결함,
     * `docs/failures/2026-08-30-thumbnail-fallback-icon-size.md`) — `feature:home`의
     * `PlaceCardImageSlot.ImageFallbackGlyphSize`와 같은 값을 쓴다.
     */
    val ThumbnailPlaceholderIconSize = 24.dp
}
