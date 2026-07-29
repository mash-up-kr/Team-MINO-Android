package team.mino.core.designsystem.component.category.token

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Category 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 */
internal object CategoryTokens {
    val ChipSpacing = 10.dp
    val GradientEdgeWidth = 48.dp

    val ChipPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
    val ChipShape = RoundedCornerShape(6.dp)
    val ChipBorderWidth = 1.dp
    val ChipFont = TypographyAccessKeyToken.Body2NormalMedium

    val ChipActiveContainerColor = ColorAccessKeyToken.PrimaryNormal
    val ChipActiveContentColor = ColorAccessKeyToken.InverseLabel
    val ChipInactiveContainerColor = ColorAccessKeyToken.BackgroundNormalNormal
    val ChipInactiveContentColor = ColorAccessKeyToken.LabelAlternative
    val ChipInactiveBorderColor = ColorAccessKeyToken.LineNormalNeutral
}
