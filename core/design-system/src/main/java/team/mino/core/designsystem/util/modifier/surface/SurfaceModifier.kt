package team.mino.core.designsystem.util.modifier.surface

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

/**
 * [shape]로 자르고 [containerColor]를 채운 뒤, [borderColor]가 있으면 [borderWidth] 테두리를 그린다.
 * 칩·배지류 컴포넌트의 "채우기 + 선택적 테두리" 배경을 한 번에 적용할 때 쓴다.
 */
fun Modifier.surface(
    shape: Shape,
    containerColor: Color,
    borderColor: Color? = null,
    borderWidth: Dp = Dp.Unspecified,
): Modifier =
    clip(shape)
        .background(containerColor)
        .then(
            if (borderColor != null) {
                Modifier.border(width = borderWidth, color = borderColor, shape = shape)
            } else {
                Modifier
            },
        )
