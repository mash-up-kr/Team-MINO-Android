package team.mino.core.designsystem.component.button.token

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.button.ButtonSize
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Button 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 * Figma `Button/Button`(컴포넌트셋 16215:37602)의 48개 변형 실측값 기준.
 */
internal object ButtonTokens {
    val BorderWidth = 1.dp

    val SolidPrimaryContainerColor = ColorAccessKeyToken.PrimaryNormal

    // Figma는 Static/White지만 그 색은 모드와 무관하게 항상 흰색이라, 다크모드에서 흰 배경(PrimaryNormal이
    // 뒤집힘) 위 흰 글자가 된다. InverseLabel은 배경과 함께 반전되어 항상 대비를 유지하므로 이쪽을 쓴다.
    val SolidPrimaryContentColor = ColorAccessKeyToken.InverseLabel

    val SolidAssistiveContainerColor = ColorAccessKeyToken.FillNormal
    val SolidAssistiveContentColor = ColorAccessKeyToken.LabelNeutral

    val OutlinedPrimaryContentColor = ColorAccessKeyToken.PrimaryNormal
    val OutlinedPrimaryBorderColor = ColorAccessKeyToken.LineNormalNeutral

    val OutlinedAssistiveContentColor = ColorAccessKeyToken.LabelNormal
    val OutlinedAssistiveBorderColor = ColorAccessKeyToken.LineNormalNeutral

    /** Solid 계열 비활성. 배경과 글자가 통째로 다른 토큰으로 바뀐다. */
    val SolidDisabledContainerColor = ColorAccessKeyToken.InteractionDisable
    val SolidDisabledContentColor = ColorAccessKeyToken.LabelAssistive

    /** Outlined 계열 비활성. 배경·테두리는 그대로 두고 글자만 바뀐다. */
    val OutlinedDisabledContentColor = ColorAccessKeyToken.LabelDisable
}

// 사이즈당 한 번만 만들어 재사용한다. when으로 매 호출마다 새로 만들지 않는다.
private val ContentPaddingBySize = mapOf(
    ButtonSize.Large to PaddingValues(horizontal = 28.dp, vertical = 12.dp),
    ButtonSize.Medium to PaddingValues(horizontal = 20.dp, vertical = 9.dp),
    ButtonSize.Small to PaddingValues(horizontal = 14.dp, vertical = 7.dp),
)

// 아이콘 전용 버튼은 상하좌우 패딩이 같아 정사각이 된다(Large 48 / Medium 40 / Small 32).
private val IconOnlyContentPaddingBySize = mapOf(
    ButtonSize.Large to PaddingValues(12.dp),
    ButtonSize.Medium to PaddingValues(10.dp),
    ButtonSize.Small to PaddingValues(7.dp),
)

// 크기마다 모서리가 다르다(Figma 실측).
private val ShapeBySize = mapOf(
    ButtonSize.Large to RoundedCornerShape(12.dp),
    ButtonSize.Medium to RoundedCornerShape(10.dp),
    ButtonSize.Small to RoundedCornerShape(8.dp),
)

internal fun ButtonSize.contentPadding(): PaddingValues = ContentPaddingBySize.getValue(this)

internal fun ButtonSize.iconOnlyContentPadding(): PaddingValues = IconOnlyContentPaddingBySize.getValue(this)

internal fun ButtonSize.shape(): Shape = ShapeBySize.getValue(this)

/** Figma `Leading Icon`·`Trailing Icon` 슬롯의 아이콘 박스 크기. */
internal val ButtonSize.iconSize: Dp
    get() =
        when (this) {
            ButtonSize.Large -> 20.dp
            ButtonSize.Medium -> 18.dp
            ButtonSize.Small -> 16.dp
        }

/** 아이콘 전용 버튼(Figma `Icon Only=True`)의 아이콘 크기. 글자 옆 아이콘보다 한 단계 크다. */
internal val ButtonSize.iconOnlyIconSize: Dp
    get() =
        when (this) {
            ButtonSize.Large -> 24.dp
            ButtonSize.Medium -> 20.dp
            ButtonSize.Small -> 18.dp
        }

/** 아이콘과 글자 사이 간격(Figma `Content` 프레임의 gap). */
internal val ButtonSize.iconTextSpacing: Dp
    get() =
        when (this) {
            ButtonSize.Large -> 6.dp
            ButtonSize.Medium -> 5.dp
            ButtonSize.Small -> 4.dp
        }

/**
 * 글자 스타일. 크기가 본문 단계(Body1/Body2/Label2)를, 색이 굵기(Bold/Medium)를 가른다.
 * 12개 조합 모두 Figma 실측값이다.
 */
internal fun ButtonStyle.font(size: ButtonSize): TypographyAccessKeyToken {
    val bold =
        when (this) {
            ButtonStyle.SolidPrimary, ButtonStyle.OutlinedPrimary -> true
            ButtonStyle.SolidAssistive, ButtonStyle.OutlinedAssistive -> false
        }
    return when (size) {
        ButtonSize.Large ->
            if (bold) TypographyAccessKeyToken.Body1NormalBold else TypographyAccessKeyToken.Body1NormalMedium

        ButtonSize.Medium ->
            if (bold) TypographyAccessKeyToken.Body2NormalBold else TypographyAccessKeyToken.Body2NormalMedium

        ButtonSize.Small ->
            if (bold) TypographyAccessKeyToken.Label2Bold else TypographyAccessKeyToken.Label2Medium
    }
}
