package team.mino.core.designsystem.foundation.shadow

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import team.mino.core.designsystem.foundation.shadow.token.ShadowAccessKeyToken
import team.mino.core.designsystem.foundation.shadow.token.ShadowTokens

@Immutable
class Shadows(
    val normalXsmall: MinoShadow,
    val normalSmall: MinoShadow,
    val normalMedium: MinoShadow,
    val normalLarge: MinoShadow,
    val normalXlarge: MinoShadow,
    val spreadSmall: MinoShadow,
    val spreadMedium: MinoShadow,
) {
    fun copy(
        normalXsmall: MinoShadow = this.normalXsmall,
        normalSmall: MinoShadow = this.normalSmall,
        normalMedium: MinoShadow = this.normalMedium,
        normalLarge: MinoShadow = this.normalLarge,
        normalXlarge: MinoShadow = this.normalXlarge,
        spreadSmall: MinoShadow = this.spreadSmall,
        spreadMedium: MinoShadow = this.spreadMedium,
    ): Shadows =
        Shadows(
            normalXsmall = normalXsmall,
            normalSmall = normalSmall,
            normalMedium = normalMedium,
            normalLarge = normalLarge,
            normalXlarge = normalXlarge,
            spreadSmall = spreadSmall,
            spreadMedium = spreadMedium,
        )

    override fun toString(): String =
        "Shadows(" +
            "normalXsmall=$normalXsmall, " +
            "normalSmall=$normalSmall, " +
            "normalMedium=$normalMedium, " +
            "normalLarge=$normalLarge, " +
            "normalXlarge=$normalXlarge, " +
            "spreadSmall=$spreadSmall, " +
            "spreadMedium=$spreadMedium" +
            ")"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Shadows) return false

        if (normalXsmall != other.normalXsmall) return false
        if (normalSmall != other.normalSmall) return false
        if (normalMedium != other.normalMedium) return false
        if (normalLarge != other.normalLarge) return false
        if (normalXlarge != other.normalXlarge) return false
        if (spreadSmall != other.spreadSmall) return false
        if (spreadMedium != other.spreadMedium) return false

        return true
    }

    override fun hashCode(): Int =
        arrayOf(
            normalXsmall,
            normalSmall,
            normalMedium,
            normalLarge,
            normalXlarge,
            spreadSmall,
            spreadMedium,
        ).contentHashCode()
}

internal fun minoShadows(
    normalXsmall: MinoShadow = ShadowTokens.NormalXsmall,
    normalSmall: MinoShadow = ShadowTokens.NormalSmall,
    normalMedium: MinoShadow = ShadowTokens.NormalMedium,
    normalLarge: MinoShadow = ShadowTokens.NormalLarge,
    normalXlarge: MinoShadow = ShadowTokens.NormalXlarge,
    spreadSmall: MinoShadow = ShadowTokens.SpreadSmall,
    spreadMedium: MinoShadow = ShadowTokens.SpreadMedium,
): Shadows =
    Shadows(
        normalXsmall = normalXsmall,
        normalSmall = normalSmall,
        normalMedium = normalMedium,
        normalLarge = normalLarge,
        normalXlarge = normalXlarge,
        spreadSmall = spreadSmall,
        spreadMedium = spreadMedium,
    )

internal fun Shadows.fromToken(value: ShadowAccessKeyToken): MinoShadow =
    when (value) {
        ShadowAccessKeyToken.NormalXsmall -> normalXsmall
        ShadowAccessKeyToken.NormalSmall -> normalSmall
        ShadowAccessKeyToken.NormalMedium -> normalMedium
        ShadowAccessKeyToken.NormalLarge -> normalLarge
        ShadowAccessKeyToken.NormalXlarge -> normalXlarge
        ShadowAccessKeyToken.SpreadSmall -> spreadSmall
        ShadowAccessKeyToken.SpreadMedium -> spreadMedium
    }

internal val LocalShadows = staticCompositionLocalOf { minoShadows() }
