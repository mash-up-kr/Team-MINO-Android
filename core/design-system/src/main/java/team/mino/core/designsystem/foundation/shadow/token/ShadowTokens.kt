package team.mino.core.designsystem.foundation.shadow.token

import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.shadow.MinoShadow

internal object ShadowTokens {
    val NormalXsmall = MinoShadow(
        layers = listOf(
            shadowLayer(offsetY = 1.dp, blurRadius = 0.87.dp, spread = (-1).dp, alpha = AtomicShadowToken.Alpha10),
        ),
    )

    val NormalSmall = MinoShadow(
        layers = listOf(
            shadowLayer(offsetY = 2.dp, blurRadius = 1.73.dp, spread = (-2).dp, alpha = AtomicShadowToken.Alpha6),
            shadowLayer(offsetY = 4.dp, blurRadius = 4.33.dp, spread = (-1).dp, alpha = AtomicShadowToken.Alpha6),
        ),
    )

    val NormalMedium = MinoShadow(
        layers = listOf(
            shadowLayer(offsetY = 4.dp, blurRadius = 3.46.dp, spread = (-2).dp, alpha = AtomicShadowToken.Alpha7),
            shadowLayer(offsetY = 10.dp, blurRadius = 10.39.dp, spread = (-3).dp, alpha = AtomicShadowToken.Alpha7),
        ),
    )

    val NormalLarge = MinoShadow(
        layers = listOf(
            shadowLayer(offsetY = 6.dp, blurRadius = 5.2.dp, spread = (-4).dp, alpha = AtomicShadowToken.Alpha8),
            shadowLayer(offsetY = 16.dp, blurRadius = 15.59.dp, spread = (-6).dp, alpha = AtomicShadowToken.Alpha8),
        ),
    )

    val NormalXlarge = MinoShadow(
        layers = listOf(
            shadowLayer(offsetY = 10.dp, blurRadius = 8.66.dp, spread = (-5).dp, alpha = AtomicShadowToken.Alpha10),
            shadowLayer(offsetY = 24.dp, blurRadius = 24.25.dp, spread = (-10).dp, alpha = AtomicShadowToken.Alpha12),
        ),
    )

    val SpreadSmall = MinoShadow(
        layers = listOf(
            shadowLayer(offsetY = 0.dp, blurRadius = 60.dp, spread = 0.dp, alpha = AtomicShadowToken.Alpha10),
        ),
    )

    val SpreadMedium = MinoShadow(
        layers = listOf(
            shadowLayer(offsetY = 15.dp, blurRadius = 75.dp, spread = 0.dp, alpha = AtomicShadowToken.Alpha16),
        ),
    )
}

private fun shadowLayer(
    offsetY: Dp,
    blurRadius: Dp,
    spread: Dp,
    alpha: Float,
): Shadow =
    Shadow(
        radius = blurRadius,
        color = AtomicShadowToken.ShadowColor,
        spread = spread,
        offset = DpOffset(x = 0.dp, y = offsetY),
        alpha = alpha,
    )
