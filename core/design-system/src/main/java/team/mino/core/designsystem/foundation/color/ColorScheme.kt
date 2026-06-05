package team.mino.core.designsystem.foundation.color

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.ColorDarkTokens
import team.mino.core.designsystem.foundation.color.token.ColorLightTokens

@Immutable
class ColorScheme(
    val purple80: Color,
    val purpleGrey80: Color,
    val pink80: Color,
    val purple40: Color,
    val purpleGrey40: Color,
    val pink40: Color,
) {
    fun copy(
        purple80: Color = this.purple80,
        purpleGrey80: Color = this.purpleGrey80,
        pink80: Color = this.pink80,
        purple40: Color = this.purple40,
        purpleGrey40: Color = this.purpleGrey40,
        pink40: Color = this.pink40,
    ): ColorScheme =
        ColorScheme(
            purple80 = purple80,
            purpleGrey80 = purpleGrey80,
            pink80 = pink80,
            purple40 = purple40,
            purpleGrey40 = purpleGrey40,
            pink40 = pink40,
        )

    override fun toString(): String =
        "ColorScheme(" +
            "purple80=$purple80, " +
            "purpleGrey80=$purpleGrey80, " +
            "pink80=$pink80, " +
            "purple40=$purple40, " +
            "purpleGrey40=$purpleGrey40, " +
            "pink40=$pink40" +
            ")"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColorScheme) return false

        if (purple80 != other.purple80) return false
        if (purpleGrey80 != other.purpleGrey80) return false
        if (pink80 != other.pink80) return false
        if (purple40 != other.purple40) return false
        if (purpleGrey40 != other.purpleGrey40) return false
        if (pink40 != other.pink40) return false

        return true
    }

    override fun hashCode(): Int =
        arrayOf(purple80, purpleGrey80, pink80, purple40, purpleGrey40, pink40).contentHashCode()
}

internal fun lightColorScheme(
    purple80: Color = ColorLightTokens.Purple80,
    purpleGrey80: Color = ColorLightTokens.PurpleGrey80,
    pink80: Color = ColorLightTokens.Pink80,
    purple40: Color = ColorLightTokens.Purple40,
    purpleGrey40: Color = ColorLightTokens.PurpleGrey40,
    pink40: Color = ColorLightTokens.Pink40,
): ColorScheme =
    ColorScheme(
        purple80 = purple80,
        purpleGrey80 = purpleGrey80,
        pink80 = pink80,
        purple40 = purple40,
        purpleGrey40 = purpleGrey40,
        pink40 = pink40,
    )

internal fun darkColorScheme(
    purple80: Color = ColorDarkTokens.Purple80,
    purpleGrey80: Color = ColorDarkTokens.PurpleGrey80,
    pink80: Color = ColorDarkTokens.Pink80,
    purple40: Color = ColorDarkTokens.Purple40,
    purpleGrey40: Color = ColorDarkTokens.PurpleGrey40,
    pink40: Color = ColorDarkTokens.Pink40,
): ColorScheme =
    ColorScheme(
        purple80 = purple80,
        purpleGrey80 = purpleGrey80,
        pink80 = pink80,
        purple40 = purple40,
        purpleGrey40 = purpleGrey40,
        pink40 = pink40,
    )

internal fun ColorScheme.fromToken(value: ColorAccessKeyToken): Color =
    when (value) {
        ColorAccessKeyToken.Purple80 -> purple80
        ColorAccessKeyToken.PurpleGrey80 -> purpleGrey80
        ColorAccessKeyToken.Pink80 -> pink80
        ColorAccessKeyToken.Purple40 -> purple40
        ColorAccessKeyToken.PurpleGrey40 -> purpleGrey40
        ColorAccessKeyToken.Pink40 -> pink40
    }

/**
 * DarkMode 대응 시 darkColorScheme() 추가
 */
@Composable
internal fun provideColorScheme(): ColorScheme =
    when {
        isSystemInDarkTheme() -> lightColorScheme()
        else -> lightColorScheme()
    }

internal val LocalColorScheme = staticCompositionLocalOf { lightColorScheme() }
