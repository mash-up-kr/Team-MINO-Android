package team.mino.core.designsystem.foundation.shape

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import team.mino.core.designsystem.foundation.shape.token.ShapeAccessKeyToken
import team.mino.core.designsystem.foundation.shape.token.ShapeTokens

@Immutable
class Shapes(
    val small: Shape,
    val medium: Shape,
    val large: Shape,
) {
    fun copy(
        small: Shape = this.small,
        medium: Shape = this.medium,
        large: Shape = this.large,
    ): Shapes =
        Shapes(
            small = small,
            medium = medium,
            large = large,
        )

    override fun toString(): String =
        "Shapes(" +
            "small=$small, " +
            "medium=$medium, " +
            "large=$large" +
            ")"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Shapes) return false

        if (small != other.small) return false
        if (medium != other.medium) return false
        if (large != other.large) return false

        return true
    }

    override fun hashCode(): Int = arrayOf(small, medium, large).contentHashCode()
}

internal fun minoShapes(
    small: Shape = ShapeTokens.Small,
    medium: Shape = ShapeTokens.Medium,
    large: Shape = ShapeTokens.Large,
): Shapes =
    Shapes(
        small = small,
        medium = medium,
        large = large,
    )

internal fun Shapes.fromToken(value: ShapeAccessKeyToken): Shape =
    when (value) {
        ShapeAccessKeyToken.Small -> small
        ShapeAccessKeyToken.Medium -> medium
        ShapeAccessKeyToken.Large -> large
    }

internal val LocalShapes = staticCompositionLocalOf { minoShapes() }
