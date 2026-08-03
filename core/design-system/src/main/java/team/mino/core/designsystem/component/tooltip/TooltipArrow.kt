package team.mino.core.designsystem.component.tooltip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import team.mino.core.designsystem.component.tooltip.token.arrowDepth
import team.mino.core.designsystem.component.tooltip.token.arrowLength

/**
 * 말풍선 밖으로 돌출해 앵커를 가리키는 화살표. [position]이 가리키는 방향에 맞춰 회전한다.
 *
 * 말풍선과 같은 두 겹(Inverse 88% + 검정 5%)으로 칠해 이어붙였을 때 색이 맞는다.
 */
@Composable
internal fun TooltipArrow(
    size: TooltipSize,
    position: TooltipPosition,
    modifier: Modifier = Modifier,
) {
    val shape = position.arrowShape
    Box(
        modifier = modifier
            .then(
                if (position.isVertical) {
                    Modifier.size(width = size.arrowLength, height = size.arrowDepth)
                } else {
                    Modifier.size(width = size.arrowDepth, height = size.arrowLength)
                },
            ).background(color = MinoTooltipDefaults.containerColor, shape = shape)
            .background(color = MinoTooltipDefaults.overlayColor, shape = shape),
    )
}

/** 화살표가 말풍선의 위/아래에 붙는 방향인지. `false`면 좌/우에 붙는다. */
internal val TooltipPosition.isVertical: Boolean
    get() = this == TooltipPosition.Top || this == TooltipPosition.Bottom

internal val TooltipPosition.arrowShape: Shape
    get() = ArrowShapeByPosition.getValue(this)

// Figma `Tooltip/Resource/Medium/Arrow`(node 16764-137885)의 패스를 정규화한 값이다.
// u = 밑변을 따르는 위치(0..1), v = 깊이(0 = 꼭짓점, 1 = 말풍선에 닿는 밑변).
// 밑변 양끝이 바깥으로 벌어지는 곡선이 말풍선과의 이음매를 매끄럽게 만든다.
// Small 화살표는 Figma에 별도 패스가 있으나 실측 차이가 1dp 미만이라 같은 패스를 크기만 바꿔 쓴다.
private val ArrowOutline = listOf(
    // (u, v) — moveTo 이후 [곡선 제어점 2개 + 끝점] 또는 [끝점]만 있는 직선
    floatArrayOf(0.0864f, 1.0000f),
    floatArrayOf(0.1194f, 0.9989f, 0.1398f, 0.9950f, 0.1591f, 0.9778f),
    floatArrayOf(0.1814f, 0.9579f, 0.2025f, 0.9251f, 0.2216f, 0.8809f),
    floatArrayOf(0.2430f, 0.8311f, 0.2606f, 0.7616f, 0.2959f, 0.6229f),
    floatArrayOf(0.3785f, 0.2973f),
    floatArrayOf(0.4204f, 0.1325f, 0.4413f, 0.0501f, 0.4662f, 0.0199f),
    floatArrayOf(0.4880f, -0.0066f, 0.5120f, -0.0066f, 0.5338f, 0.0199f),
    floatArrayOf(0.5587f, 0.0501f, 0.5796f, 0.1325f, 0.6215f, 0.2973f),
    floatArrayOf(0.7041f, 0.6229f),
    floatArrayOf(0.7394f, 0.7616f, 0.7570f, 0.8311f, 0.7784f, 0.8809f),
    floatArrayOf(0.7975f, 0.9251f, 0.8186f, 0.9579f, 0.8409f, 0.9778f),
    floatArrayOf(0.8602f, 0.9950f, 0.8806f, 0.9989f, 0.9136f, 1.0000f),
    floatArrayOf(1.0000f, 1.0000f),
)

// 방향별로 한 번만 만들어 재사용한다.
private val ArrowShapeByPosition = TooltipPosition.entries.associateWith { position ->
    GenericShape { size, _ ->
        // (u, v) 쌍으로 이어진 정규 좌표를 화살표가 향하는 방향의 실제 좌표로 옮긴다.
        fun mapped(normalized: FloatArray): FloatArray {
            val points = FloatArray(normalized.size)
            for (i in normalized.indices step 2) {
                val u = normalized[i]
                val v = normalized[i + 1]
                points[i] =
                    when (position) {
                        TooltipPosition.Top, TooltipPosition.Bottom -> u * size.width
                        TooltipPosition.Left -> (1f - v) * size.width
                        TooltipPosition.Right -> v * size.width
                    }
                points[i + 1] =
                    when (position) {
                        TooltipPosition.Bottom -> v * size.height
                        TooltipPosition.Top -> (1f - v) * size.height
                        TooltipPosition.Left, TooltipPosition.Right -> u * size.height
                    }
            }
            return points
        }

        val start = mapped(floatArrayOf(0f, 1f))
        moveTo(start[0], start[1])
        ArrowOutline.forEach { segment ->
            val p = mapped(segment)
            if (p.size == 2) lineTo(p[0], p[1]) else cubicTo(p[0], p[1], p[2], p[3], p[4], p[5])
        }
        close()
    }
}
