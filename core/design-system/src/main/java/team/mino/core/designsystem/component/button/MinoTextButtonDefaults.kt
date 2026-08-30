package team.mino.core.designsystem.component.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.component.button.token.TextButtonTokens
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * [MinoTextButton]의 기본값 모음.
 */
object MinoTextButtonDefaults {
    /** [style]에 대응하는 [MinoTextButtonColors]. */
    @Composable
    @ReadOnlyComposable
    fun colors(style: TextButtonStyle): MinoTextButtonColors =
        when (style) {
            TextButtonStyle.Primary -> MinoAndroidTheme.colors.primaryTextButtonColors
            TextButtonStyle.Assistive -> MinoAndroidTheme.colors.assistiveTextButtonColors
        }

    /** [enabled]에 대응하는 글자·아이콘 색. */
    @Stable
    internal fun contentColor(
        colors: MinoTextButtonColors,
        enabled: Boolean,
    ): Color = if (enabled) colors.contentColor else colors.disabledContentColor

    internal val ColorScheme.primaryTextButtonColors: MinoTextButtonColors
        get() =
            primaryTextButtonColorsCached
                ?: MinoTextButtonColors(
                    contentColor = fromToken(TextButtonTokens.PrimaryContentColor),
                    disabledContentColor = fromToken(TextButtonTokens.DisabledContentColor),
                ).also { primaryTextButtonColorsCached = it }

    internal val ColorScheme.assistiveTextButtonColors: MinoTextButtonColors
        get() =
            assistiveTextButtonColorsCached
                ?: MinoTextButtonColors(
                    contentColor = fromToken(TextButtonTokens.AssistiveContentColor),
                    disabledContentColor = fromToken(TextButtonTokens.DisabledContentColor),
                ).also { assistiveTextButtonColorsCached = it }
}

/**
 * [MinoTextButton]의 상태별 색.
 *
 * 배경·테두리가 없는 컴포넌트라 글자(와 아이콘) 색 슬롯만 갖는다.
 */
@Immutable
class MinoTextButtonColors(
    val contentColor: Color,
    val disabledContentColor: Color,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinoTextButtonColors) return false

        if (contentColor != other.contentColor) return false
        if (disabledContentColor != other.disabledContentColor) return false

        return true
    }

    override fun hashCode(): Int = arrayOf(contentColor, disabledContentColor).contentHashCode()
}
