package team.mino.core.designsystem.component.textfield.token

import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.shape.token.ShapeAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * TextInput/TextArea 공통 슬롯 → 디자인 토큰 키 매핑.
 * Figma `Textinput/Textfield`(16215-31385)·`Textinput/Textarea`(16215-32165) 실측값 기준.
 */
internal object TextFieldTokens {
    // 색 슬롯
    val LabelColor = ColorAccessKeyToken.LabelNeutral
    val DisabledLabelColor = ColorAccessKeyToken.LabelDisable
    val TextColor = ColorAccessKeyToken.LabelNormal
    val DisabledTextColor = ColorAccessKeyToken.LabelDisable
    val PlaceholderColor = ColorAccessKeyToken.LabelAssistive
    val BackgroundColor = ColorAccessKeyToken.BackgroundNormalNormal
    val DisabledBackgroundColor = ColorAccessKeyToken.FillAlternative
    val BorderColor = ColorAccessKeyToken.LineNormalAlternative
    val FocusedBorderColor = ColorAccessKeyToken.LineNormalNeutral
    val ErrorBorderColor = ColorAccessKeyToken.StatusNegative
    val HelperColor = ColorAccessKeyToken.LabelAlternative
    val ErrorHelperColor = ColorAccessKeyToken.StatusNegative
    val CounterColor = ColorAccessKeyToken.LabelAssistive
    val TrailingButtonColor = ColorAccessKeyToken.LabelNormal
    val DisabledTrailingButtonColor = ColorAccessKeyToken.LabelDisable

    // 트레일링 상태 아이콘 색. 성공은 Figma 원본이 뉴트럴(#171719=LabelNormal)이다.
    val PositiveIconColor = ColorAccessKeyToken.LabelNormal
    val NegativeIconColor = ColorAccessKeyToken.StatusNegative
    val DisabledIconColor = ColorAccessKeyToken.LabelDisable
    val ClearIconColor = ColorAccessKeyToken.LabelAssistive

    // 폰트
    val LabelFont = TypographyAccessKeyToken.Label1NormalBold
    val InputFont = TypographyAccessKeyToken.Body1NormalRegular
    val AreaInputFont = TypographyAccessKeyToken.Body1ReadingRegular
    val HelperFont = TypographyAccessKeyToken.Caption1Regular
    val CounterFont = TypographyAccessKeyToken.Caption1Regular
    val TrailingButtonFont = TypographyAccessKeyToken.Body1NormalBold

    // 셰이프·수치
    val ContainerShape = ShapeAccessKeyToken.Medium
    val BorderWidth = 1.dp
    val LabelBoxSpacing = 8.dp
    val ContentSpacing = 8.dp

    // TextInput 박스 패딩
    val InputHorizontalPadding = 16.dp
    val InputVerticalPadding = 14.dp

    // TextArea 박스 패딩 및 높이(Figma Wrapper: 1줄 박스 86, Fixed 138 → 텍스트영역 78 ≈ 3줄)
    val AreaPadding = 12.dp
    val AreaBottomSpacing = 12.dp

    // Limit/Fixed에서 텍스트 영역이 차지하는 줄 수. Body1Reading 행높이(26dp) 기준 78dp ≈ 3줄.
    val AreaMaxLines = 3

    // 트레일링 아이콘·버튼
    val StatusIconSize = 24.dp
    val ClearIconSize = 20.dp
    val TrailingButtonShape = ShapeAccessKeyToken.Small
    val TrailingButtonHorizontalPadding = 4.dp
    val TrailingButtonVerticalPadding = 2.dp

    val DefaultMaxLength = 2000
}
