package team.mino.core.designsystem.component.snackbar.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
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

    /**
     * 콘텐츠 영역(Figma `Container`)의 최소 높이. 제목 한 줄(행간 22 + 상하 5)과 같은 값이라
     * 제목만 있을 때는 드러나지 않지만, 설명만 있는 형태(행간 18)에서 pill 높이를 54dp로 붙잡는다.
     */
    val MinContentHeight = 32.dp

    /** 메시지 영역·액션·닫기 버튼 사이 간격(Figma `Container`의 gap). */
    val ContentSpacing = 12.dp

    /** 리딩 아이콘과 메시지 사이 간격(Figma `Content`의 gap). 위 [ContentSpacing]과 다른 값이다. */
    val LeadingIconSpacing = 8.dp
    val LeadingIconSize = 22.dp

    val MessageHorizontalPadding = 2.dp
    val MessageVerticalPadding = 5.dp

    val ActionHorizontalPadding = 2.dp
    val ActionVerticalPadding = 4.dp

    val CloseButtonPadding = 2.dp
    val CloseIconSize = 20.dp

    /** 액션 버튼 리플 모서리(Figma `Interaction` 레이어). 셰이프 토큰 스케일(8/12/16)에 없는 값이다. */
    val ActionShape: Shape = RoundedCornerShape(6.dp)
}
