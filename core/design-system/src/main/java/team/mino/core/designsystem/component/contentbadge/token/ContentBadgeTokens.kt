package team.mino.core.designsystem.component.contentbadge.token

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.contentbadge.ContentBadgeSize
import team.mino.core.designsystem.foundation.color.token.AtomicOpacityToken
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Content Badge 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 */
internal object ContentBadgeTokens {
    val BorderWidth = 1.dp
    val IconTextSpacing = 3.dp

    val NeutralContainerColor = ColorAccessKeyToken.FillNormal
    val NeutralContentColor = ColorAccessKeyToken.LabelAlternative
    val NeutralBorderColor = ColorAccessKeyToken.LineNormalNeutral

    // color 파라미터를 지정하면(Accent) 그 한 색에서 배경·테두리를 알파로 파생한다
    // (Figma 실측: Solid 배경 8%, Outlined 테두리 43%).
    val AccentTintOpacity = AtomicOpacityToken.Opacity8
    val AccentBorderOpacity = AtomicOpacityToken.Opacity43

    // Figma 컴포넌트셋은 Accent 색상별 variant를 따로 두지 않고 인스턴스마다 색을 바꿔 쓴다.
    // 예시로 보여준 색(Cyan)을 기본값으로 둔다.
    val DefaultAccentColor = ColorAccessKeyToken.AccentForegroundCyan
}

// 사이즈당 한 번만 만들어 재사용한다. when으로 매 호출마다 새로 만들지 않는다.
private val ContentPaddingBySize = mapOf(
    ContentBadgeSize.XSmall to PaddingValues(horizontal = 6.dp, vertical = 3.dp),
    ContentBadgeSize.Small to PaddingValues(horizontal = 6.dp, vertical = 4.dp),
    ContentBadgeSize.Medium to PaddingValues(horizontal = 8.dp, vertical = 5.dp),
)

private val ShapeBySize = mapOf(
    ContentBadgeSize.XSmall to RoundedCornerShape(6.dp),
    ContentBadgeSize.Small to RoundedCornerShape(6.dp),
    ContentBadgeSize.Medium to RoundedCornerShape(8.dp),
)

internal fun ContentBadgeSize.contentPadding(): PaddingValues = ContentPaddingBySize.getValue(this)

internal fun ContentBadgeSize.shape(): Shape = ShapeBySize.getValue(this)

internal val ContentBadgeSize.font: TypographyAccessKeyToken
    get() =
        when (this) {
            ContentBadgeSize.XSmall -> TypographyAccessKeyToken.Caption2Medium
            ContentBadgeSize.Small -> TypographyAccessKeyToken.Caption1Medium
            ContentBadgeSize.Medium -> TypographyAccessKeyToken.Label2Medium
        }

internal val ContentBadgeSize.iconSize: Dp
    get() =
        when (this) {
            ContentBadgeSize.XSmall -> 12.dp
            ContentBadgeSize.Small -> 14.dp
            ContentBadgeSize.Medium -> 16.dp
        }
