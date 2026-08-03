package team.mino.core.designsystem.component.textinput.token

import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.AtomicOpacityToken
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.shadow.token.ShadowAccessKeyToken
import team.mino.core.designsystem.foundation.shape.token.ShapeAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Textfield/Textarea 공통 슬롯 → 디자인 토큰 키 매핑.
 * Figma `Textinput/Textfield`(16215-31385)·`Textinput/Textarea`(16215-32165) 실측값 기준.
 *
 * 필드 배경은 불투명 흰색이 아니라 **반투명**(`Background/Transparent/Normal`)이다. Figma는 여기에
 * backdrop-blur를 얹지만 `background=Android` 변형은 블러 없이 반투명만 쓰므로, 이 매핑이 Android
 * 기준으로는 정확하다.
 */
internal object TextInputTokens {
    // 색 슬롯 — 라벨·본문
    val LabelColor = ColorAccessKeyToken.LabelNeutral
    val DisabledLabelColor = ColorAccessKeyToken.LabelDisable
    val RequiredColor = ColorAccessKeyToken.StatusNegative
    val TextColor = ColorAccessKeyToken.LabelNormal
    val DisabledTextColor = ColorAccessKeyToken.LabelDisable
    val PlaceholderColor = ColorAccessKeyToken.LabelAssistive
    val HelperColor = ColorAccessKeyToken.LabelAlternative
    val ErrorHelperColor = ColorAccessKeyToken.StatusNegative
    val CounterColor = ColorAccessKeyToken.LabelAlternative

    // 색 슬롯 — 컨테이너
    val BackgroundColor = ColorAccessKeyToken.BackgroundTransparentNormal
    val DisabledBackgroundColor = ColorAccessKeyToken.FillAlternative
    val BorderColor = ColorAccessKeyToken.LineNormalNeutral
    val DisabledBorderColor = ColorAccessKeyToken.LineNormalAlternative
    val FocusedBorderColor = ColorAccessKeyToken.PrimaryNormal
    val ErrorBorderColor = ColorAccessKeyToken.StatusNegative

    // 색 슬롯 — 트레일링 버튼(Figma `Textinput/Resource/Textfield/Button` 16215-32904)
    val TrailingButtonColor = ColorAccessKeyToken.PrimaryNormal
    val AssistiveTrailingButtonColor = ColorAccessKeyToken.LabelNormal
    val DisabledTrailingButtonColor = ColorAccessKeyToken.LabelAssistive

    // 트레일링 상태 아이콘 색. 성공은 Figma 원본이 뉴트럴(#171719=LabelNormal)이다.
    val PositiveIconColor = ColorAccessKeyToken.LabelNormal
    val NegativeIconColor = ColorAccessKeyToken.StatusNegative
    val DisabledIconColor = ColorAccessKeyToken.LabelDisable
    val ClearIconColor = ColorAccessKeyToken.LabelAssistive

    /** 포커스 테두리는 Primary/Normal을 43%로 깔아 쓴다(Figma `Inner Border`의 레이어 불투명도). */
    val FocusedBorderOpacity = AtomicOpacityToken.Opacity43

    /** 글자수 카운터는 Label/Alternative 위에 74% 불투명도가 한 번 더 얹힌다. */
    val CounterOpacity = AtomicOpacityToken.Opacity74

    // 폰트
    val LabelFont = TypographyAccessKeyToken.Label1NormalBold
    val RequiredFont = TypographyAccessKeyToken.Label1NormalMedium
    val InputFont = TypographyAccessKeyToken.Body1NormalRegular
    val AreaInputFont = TypographyAccessKeyToken.Body1ReadingRegular
    val HelperFont = TypographyAccessKeyToken.Caption1Regular
    val CounterFont = TypographyAccessKeyToken.Label2Medium
    val TrailingButtonFont = TypographyAccessKeyToken.Body1NormalBold
    val AssistiveTrailingButtonFont = TypographyAccessKeyToken.Body1NormalMedium

    // 셰이프·테두리·그림자
    val ContainerShape = ShapeAccessKeyToken.Medium
    val ContainerShadow = ShadowAccessKeyToken.NormalXsmall
    val BorderWidth = 1.dp

    /** 포커스 상태에서만 테두리가 두꺼워진다. */
    val FocusedBorderWidth = 2.dp

    // 레이아웃 — 공통
    val LabelBoxSpacing = 8.dp
    val HeadingSpacing = 4.dp

    // 레이아웃 — TextInput
    val InputPadding = 12.dp
    val ContentSpacing = 8.dp

    /** 콘텐츠 행 최소 높이. 본문 한 줄(Body1 16/24)과 같아 트레일링 슬롯 높이를 붙잡는 역할을 한다. */
    val InputMinContentHeight = 24.dp

    /** 입력 글자 좌우 여백(Figma `Text` 프레임). 컨테이너 패딩과 합쳐 좌우 16dp가 된다. */
    val InputTextHorizontalPadding = 4.dp
    val LeadingContentSize = 22.dp
    val LeadingContentPadding = 1.dp
    val TrailingContentSize = 24.dp

    // 레이아웃 — TextArea
    val AreaPadding = 12.dp
    val AreaBottomSpacing = 12.dp

    /** 하단 영역의 리딩 그룹(카운터 등)과 트레일링 그룹 사이 간격. */
    val AreaBottomContentSpacing = 16.dp

    /** 하단 영역 한 그룹 안에서 항목 사이 간격. */
    val AreaBottomSlotSpacing = 4.dp
    val AreaBottomSlotHeight = 24.dp
    val AreaBottomSlotHorizontalPadding = 4.dp

    /** Limit/Fixed에서 텍스트 영역이 차지하는 줄 수. Body1Reading 행높이(26dp) 기준 78dp = 3줄. */
    val AreaMaxLines = 3

    // 트레일링 상태 아이콘
    val StatusIconSize = 24.dp
    val ClearIconSize = 20.dp

    // 트레일링 버튼 치수
    val TrailingButtonMinWidth = 80.dp
    val TrailingButtonHorizontalPadding = 16.dp
    val TrailingButtonVerticalPadding = 12.dp

    val DefaultMaxLength = 2000
}
