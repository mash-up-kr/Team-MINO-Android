package team.mino.core.designsystem.component.button.token

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.button.ButtonSize
import team.mino.core.designsystem.component.button.ButtonStyle
import team.mino.core.designsystem.foundation.color.token.AtomicOpacityToken
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken

/**
 * Button 컴포넌트 슬롯 → 디자인 토큰 키 매핑.
 * Figma `Button/Button`(컴포넌트셋 16215:37602) 실측값 기준.
 */
internal object ButtonTokens {
    val BorderWidth = 1.dp
    val DisabledOpacity = AtomicOpacityToken.Opacity43

    val SolidPrimaryContainerColor = ColorAccessKeyToken.PrimaryNormal

    // Figma는 Static/White지만 그 색은 모드와 무관하게 항상 흰색이라, 다크모드에서 흰 배경(PrimaryNormal이
    // 뒤집힘) 위 흰 글자가 된다. InverseLabel은 배경과 함께 반전되어 항상 대비를 유지하므로 이쪽을 쓴다.
    val SolidPrimaryContentColor = ColorAccessKeyToken.InverseLabel

    val OutlinedPrimaryContentColor = ColorAccessKeyToken.PrimaryNormal
    val OutlinedPrimaryBorderColor = ColorAccessKeyToken.LineNormalNeutral

    val OutlinedAssistiveContentColor = ColorAccessKeyToken.LabelNormal
    val OutlinedAssistiveBorderColor = ColorAccessKeyToken.LineNormalNeutral
}

// 사이즈당 한 번만 만들어 재사용한다. when으로 매 호출마다 새로 만들지 않는다.
private val ContentPaddingBySize = mapOf(
    ButtonSize.Large to PaddingValues(horizontal = 28.dp, vertical = 12.dp),
    ButtonSize.Medium to PaddingValues(horizontal = 20.dp, vertical = 9.dp),
)

// Medium의 10dp가 셰이프 스케일(Small 8 / Medium 12 / Large 16)에 없어 ShapeAccessKeyToken을 쓰지
// 못한다. ContentBadge와 같이 컴포넌트가 자기 셰이프를 직접 들고 있는다.
private val ShapeBySize = mapOf(
    ButtonSize.Large to RoundedCornerShape(12.dp),
    ButtonSize.Medium to RoundedCornerShape(10.dp),
)

internal fun ButtonSize.contentPadding(): PaddingValues = ContentPaddingBySize.getValue(this)

internal fun ButtonSize.shape(): Shape = ShapeBySize.getValue(this)

/** Figma `Leading Icon`·`Trailing Icon` 슬롯의 아이콘 박스 크기. */
internal val ButtonSize.iconSize: Dp
    get() =
        when (this) {
            ButtonSize.Large -> 20.dp
            ButtonSize.Medium -> 18.dp
        }

/** 아이콘과 글자 사이 간격(Figma `Content` 프레임의 gap). */
internal val ButtonSize.iconTextSpacing: Dp
    get() =
        when (this) {
            ButtonSize.Large -> 6.dp
            ButtonSize.Medium -> 5.dp
        }

/**
 * 글자 스타일. 크기가 본문 단계(Body1/Body2)를, 색이 굵기(Bold/Medium)를 가른다.
 *
 * Large 3종과 Medium+SolidPrimary는 Figma 실측값이다. Medium의 나머지 두 조합은 위 규칙에서
 * 유추한 값이라, 해당 조합이 실제 디자인에 등장하면 실측값으로 확인할 것.
 */
internal fun ButtonStyle.font(size: ButtonSize): TypographyAccessKeyToken {
    val bold =
        when (this) {
            ButtonStyle.SolidPrimary, ButtonStyle.OutlinedPrimary -> true
            ButtonStyle.OutlinedAssistive -> false
        }
    return when (size) {
        ButtonSize.Large ->
            if (bold) TypographyAccessKeyToken.Body1NormalBold else TypographyAccessKeyToken.Body1NormalMedium

        ButtonSize.Medium ->
            if (bold) TypographyAccessKeyToken.Body2NormalBold else TypographyAccessKeyToken.Body2NormalMedium
    }
}
