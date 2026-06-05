package team.mino.core.designsystem.foundation.typography

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.TypographyTokens

@Immutable
class Typography(
    val bodyLarge: TextStyle,
) {
    fun copy(bodyLarge: TextStyle = this.bodyLarge): Typography =
        Typography(
            bodyLarge = bodyLarge,
        )

    override fun toString(): String =
        "Typography(" +
            "bodyLarge=$bodyLarge" +
            ")"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Typography) return false

        if (bodyLarge != other.bodyLarge) return false

        return true
    }

    override fun hashCode(): Int = arrayOf(bodyLarge).contentHashCode()
}

internal fun minoTypography(bodyLarge: TextStyle = TypographyTokens.BodyLarge): Typography =
    Typography(
        bodyLarge = bodyLarge,
    )

internal fun Typography.fromToken(value: TypographyAccessKeyToken): TextStyle =
    when (value) {
        TypographyAccessKeyToken.BodyLarge -> bodyLarge
    }

internal val LocalTypography = staticCompositionLocalOf { minoTypography() }
