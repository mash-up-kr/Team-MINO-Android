package team.mino.core.designsystem.component.menu.token

import androidx.compose.foundation.shape.RoundedCornerShape
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

    // 라디오·체크박스 표식. 선택되면 상자가 Primary로 채워지고 흰 표시가 얹힌다.
    val ControlActiveColor = ColorAccessKeyToken.PrimaryNormal
    val ControlBorderColor = ColorAccessKeyToken.LineNormalNormal
    val ControlIconColor = ColorAccessKeyToken.StaticWhite
    val ControlBorderWidth = 1.5.dp

    /** 표식과 라벨 사이 간격. 표식 자체의 오른쪽 여백([ControlEndPadding])이 여기에 더해진다. */
    val ControlLabelSpacing = 8.dp
    val ControlEndPadding = 2.dp

    val RadioSize = 20.dp
    val RadioVerticalPadding = 2.dp

    /**
     * 선택된 라디오 안쪽 흰 점의 지름.
     *
     * Figma는 이 자리에 `Icon/Normal/Dot`을 얹는다. 글리프가 아이콘 박스의 정확히 절반이고
     * (24 뷰박스에 반지름 6), 상자(20dp)에서 세로 패딩 2dp를 뺀 16dp로 렌더되므로 8dp가 된다.
     */
    val RadioDotSize = 8.dp

    val CheckboxSize = 18.dp
    val CheckboxShape = RoundedCornerShape(5.dp)
    val CheckboxIconSize = 16.dp
    val CheckboxVerticalPadding = 3.dp
    val CheckboxHorizontalPadding = 1.dp

    val TitleColor = ColorAccessKeyToken.LabelAlternative
    val TitleFont = TypographyAccessKeyToken.Caption1Bold
    val TitleHorizontalPadding = 1.dp
    val TitleVerticalPadding = 4.dp

    val ActionAreaDividerColor = ColorAccessKeyToken.LineNormalAlternative
    val ActionAreaDividerThickness = 1.dp
    val ActionAreaPadding = 12.dp
    val ActionAreaContentSpacing = 24.dp
    val ActionAreaLeadingStartPadding = 8.dp
}
