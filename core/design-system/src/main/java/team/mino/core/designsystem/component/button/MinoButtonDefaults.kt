package team.mino.core.designsystem.component.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.component.button.token.ButtonTokens
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * [MinoButton]의 기본값 모음.
 */
object MinoButtonDefaults {
    /** [style]에 대응하는 [MinoButtonColors]. */
    @Composable
    @ReadOnlyComposable
    fun colors(style: ButtonStyle): MinoButtonColors =
        when (style) {
            ButtonStyle.SolidPrimary -> MinoAndroidTheme.colors.solidPrimaryButtonColors
            ButtonStyle.OutlinedPrimary -> MinoAndroidTheme.colors.outlinedPrimaryButtonColors
            ButtonStyle.OutlinedAssistive -> MinoAndroidTheme.colors.outlinedAssistiveButtonColors
        }

    internal val ColorScheme.solidPrimaryButtonColors: MinoButtonColors
        get() =
            solidPrimaryButtonColorsCached
                ?: MinoButtonColors(
                    containerColor = fromToken(ButtonTokens.SolidPrimaryContainerColor),
                    contentColor = fromToken(ButtonTokens.SolidPrimaryContentColor),
                    borderColor = null,
                ).also { solidPrimaryButtonColorsCached = it }

    internal val ColorScheme.outlinedPrimaryButtonColors: MinoButtonColors
        get() =
            outlinedPrimaryButtonColorsCached
                ?: MinoButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = fromToken(ButtonTokens.OutlinedPrimaryContentColor),
                    borderColor = fromToken(ButtonTokens.OutlinedPrimaryBorderColor),
                ).also { outlinedPrimaryButtonColorsCached = it }

    internal val ColorScheme.outlinedAssistiveButtonColors: MinoButtonColors
        get() =
            outlinedAssistiveButtonColorsCached
                ?: MinoButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = fromToken(ButtonTokens.OutlinedAssistiveContentColor),
                    borderColor = fromToken(ButtonTokens.OutlinedAssistiveBorderColor),
                ).also { outlinedAssistiveButtonColorsCached = it }
}

/**
 * [MinoButton]의 색. [borderColor]가 `null`이면 테두리를 그리지 않는다.
 * 비활성(disabled) 상태는 별도 색 슬롯 대신 컴포넌트 쪽에서 알파를 낮춰 표현한다.
 */
@Immutable
class MinoButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinoButtonColors) return false

        if (containerColor != other.containerColor) return false
        if (contentColor != other.contentColor) return false
        if (borderColor != other.borderColor) return false

        return true
    }

    override fun hashCode(): Int =
        arrayOf(
            containerColor,
            contentColor,
            borderColor,
        ).contentHashCode()
}
