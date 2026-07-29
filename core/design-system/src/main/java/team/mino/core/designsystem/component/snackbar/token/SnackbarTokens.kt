package team.mino.core.designsystem.component.snackbar.token

import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.AtomicOpacityToken
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.shape.token.ShapeAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Snackbar 컴포넌트 슬롯 → 디자인 토큰 키 매핑. Figma `Snackbar/Snackbar`(node 16215-19587) 기준.
 *
 * 배경·콘텐츠 모두 Inverse 계열을 써서 라이트/다크에 따라 함께 반전된다(라이트=어두운 pill+밝은 글자,
 * 다크=밝은 pill+어두운 글자). 오버레이만 Figma 원본(primary/normal #000000 @5%)대로 항상 검정 틴트다.
 */
internal object SnackbarTokens {
    val ContainerColor = ColorAccessKeyToken.InverseBackground
    val ContainerOpacity = AtomicOpacityToken.Opacity52
    val OverlayColor = ColorAccessKeyToken.StaticBlack
    val OverlayOpacity = AtomicOpacityToken.Opacity5
    val ContentColor = ColorAccessKeyToken.InverseLabel
    val ContentOpacity = AtomicOpacityToken.Opacity88
    val CloseOpacity = AtomicOpacityToken.Opacity61

    val ContainerShape = ShapeAccessKeyToken.Medium
    val MessageFont = TypographyAccessKeyToken.Body2NormalBold
    val DescriptionFont = TypographyAccessKeyToken.Label2Regular
    val ActionFont = TypographyAccessKeyToken.Body2NormalBold

    val MaxWidth = 420.dp
    val HorizontalPadding = 16.dp
    val VerticalPadding = 11.dp
    val ContentSpacing = 12.dp
    val MessagePadding = 2.dp
    val LeadingIconSize = 20.dp
    val CloseIconSize = 20.dp
}
