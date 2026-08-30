package team.mino.core.designsystem.component.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import team.mino.core.designsystem.component.button.token.ButtonTokens
import team.mino.core.designsystem.component.button.token.shape
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * [MinoButton]·[MinoIconButton]의 기본값 모음.
 */
object MinoButtonDefaults {
    /** [style]에 대응하는 [MinoButtonColors]. */
    @Composable
    @ReadOnlyComposable
    fun colors(style: ButtonStyle): MinoButtonColors =
        when (style) {
            ButtonStyle.SolidPrimary -> MinoAndroidTheme.colors.solidPrimaryButtonColors
            ButtonStyle.SolidAssistive -> MinoAndroidTheme.colors.solidAssistiveButtonColors
            ButtonStyle.OutlinedPrimary -> MinoAndroidTheme.colors.outlinedPrimaryButtonColors
            ButtonStyle.OutlinedAssistive -> MinoAndroidTheme.colors.outlinedAssistiveButtonColors
        }

    /**
     * [size]에 대응하는 버튼 모서리.
     *
     * 버튼 바깥에 그림자·배경을 얹는 호출부가 같은 모서리를 필요로 한다. 그 값을 호출부가 다시 적으면 Figma가
     * 반경을 바꿀 때 버튼과 그 위에 얹힌 것이 서로 다른 모서리로 갈라진다.
     */
    @Stable
    fun shape(size: ButtonSize): Shape = size.shape()

    /** [enabled]에 대응하는 배경색. */
    @Stable
    internal fun containerColor(
        colors: MinoButtonColors,
        enabled: Boolean,
    ): Color = if (enabled) colors.containerColor else colors.disabledContainerColor

    /** [enabled]에 대응하는 글자·아이콘 색. */
    @Stable
    internal fun contentColor(
        colors: MinoButtonColors,
        enabled: Boolean,
    ): Color = if (enabled) colors.contentColor else colors.disabledContentColor

    internal val ColorScheme.solidPrimaryButtonColors: MinoButtonColors
        get() =
            solidPrimaryButtonColorsCached
                ?: MinoButtonColors(
                    containerColor = fromToken(ButtonTokens.SolidPrimaryContainerColor),
                    contentColor = fromToken(ButtonTokens.SolidPrimaryContentColor),
                    borderColor = null,
                    disabledContainerColor = fromToken(ButtonTokens.SolidDisabledContainerColor),
                    disabledContentColor = fromToken(ButtonTokens.SolidDisabledContentColor),
                ).also { solidPrimaryButtonColorsCached = it }

    internal val ColorScheme.solidAssistiveButtonColors: MinoButtonColors
        get() =
            solidAssistiveButtonColorsCached
                ?: MinoButtonColors(
                    containerColor = fromToken(ButtonTokens.SolidAssistiveContainerColor),
                    contentColor = fromToken(ButtonTokens.SolidAssistiveContentColor),
                    borderColor = null,
                    disabledContainerColor = fromToken(ButtonTokens.SolidDisabledContainerColor),
                    disabledContentColor = fromToken(ButtonTokens.SolidDisabledContentColor),
                ).also { solidAssistiveButtonColorsCached = it }

    internal val ColorScheme.outlinedPrimaryButtonColors: MinoButtonColors
        get() =
            outlinedPrimaryButtonColorsCached
                ?: MinoButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = fromToken(ButtonTokens.OutlinedPrimaryContentColor),
                    borderColor = fromToken(ButtonTokens.OutlinedPrimaryBorderColor),
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = fromToken(ButtonTokens.OutlinedDisabledContentColor),
                ).also { outlinedPrimaryButtonColorsCached = it }

    internal val ColorScheme.outlinedAssistiveButtonColors: MinoButtonColors
        get() =
            outlinedAssistiveButtonColorsCached
                ?: MinoButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = fromToken(ButtonTokens.OutlinedAssistiveContentColor),
                    borderColor = fromToken(ButtonTokens.OutlinedAssistiveBorderColor),
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = fromToken(ButtonTokens.OutlinedDisabledContentColor),
                ).also { outlinedAssistiveButtonColorsCached = it }
}

/**
 * [MinoButton]·[MinoIconButton]의 상태별 색. [borderColor]가 `null`이면 테두리를 그리지 않는다.
 *
 * 테두리는 활성·비활성이 같은 색이라 슬롯을 하나만 둔다(Figma `Disable=True`도 `Line/Normal/Neutral`
 * 그대로다). 배경·글자는 비활성에서 다른 토큰으로 바뀌므로 슬롯을 따로 둔다.
 */
@Immutable
class MinoButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color?,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinoButtonColors) return false

        if (containerColor != other.containerColor) return false
        if (contentColor != other.contentColor) return false
        if (borderColor != other.borderColor) return false
        if (disabledContainerColor != other.disabledContainerColor) return false
        if (disabledContentColor != other.disabledContentColor) return false

        return true
    }

    override fun hashCode(): Int =
        arrayOf(
            containerColor,
            contentColor,
            borderColor,
            disabledContainerColor,
            disabledContentColor,
        ).contentHashCode()
}
