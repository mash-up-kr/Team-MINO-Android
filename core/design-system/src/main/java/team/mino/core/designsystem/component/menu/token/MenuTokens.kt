package team.mino.core.designsystem.component.menu.token

import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.AtomicOpacityToken
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.shadow.token.ShadowAccessKeyToken
import team.mino.core.designsystem.foundation.shape.token.ShapeAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Menu 컴포넌트 슬롯 → 디자인 토큰 키 매핑. Figma `Menu/Menu`(node 16215-18387) 실측값 기준.
 */
internal object MenuTokens {
    val ContainerColor = ColorAccessKeyToken.BackgroundElevatedNormal
    val ContainerBorderColor = ColorAccessKeyToken.LineSolidNeutral
    val ContainerBorderWidth = 1.dp
    val ContainerShape = ShapeAccessKeyToken.Large
    val ContainerShadow = ShadowAccessKeyToken.NormalSmall
    val ContainerMinWidth = 140.dp

    // Figma의 콘텐츠 좌우 여백 20dp 중 12dp는 셀의 프레스 하이라이트 영역이므로,
    // 컨테이너가 8dp, 셀이 12dp를 나눠 가진다.
    val ContainerHorizontalPadding = 8.dp
    val ContainerVerticalPadding = 8.dp
    val CellSpacing = 4.dp

    val ItemShape = ShapeAccessKeyToken.Medium
    val ItemHorizontalPadding = 12.dp
    val ItemVerticalPadding = 12.dp
    val ItemVerticalPaddingCompact = 8.dp

    val LabelColor = ColorAccessKeyToken.LabelNormal
    val LabelFont = TypographyAccessKeyToken.Body1NormalRegular
    val LabelMinHeight = 24.dp
    val ActiveLabelColor = ColorAccessKeyToken.PrimaryNormal
    val ActiveLabelFont = TypographyAccessKeyToken.Body1NormalMedium
    val DisabledLabelColor = ColorAccessKeyToken.LabelAlternative

    val CaptionColor = ColorAccessKeyToken.LabelAlternative
    val CaptionFont = TypographyAccessKeyToken.Label2Regular
    val LabelCaptionSpacing = 4.dp

    val DisabledOpacity = AtomicOpacityToken.Opacity43
}
