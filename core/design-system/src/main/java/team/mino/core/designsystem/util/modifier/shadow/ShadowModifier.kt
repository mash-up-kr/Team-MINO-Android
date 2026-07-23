package team.mino.core.designsystem.util.modifier.shadow

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Shape
import team.mino.core.designsystem.foundation.shadow.MinoShadow

/**
 * [MinoShadow]의 모든 레이어를 [shape] 뒤에 겹쳐 그린다.
 */
fun Modifier.dropShadow(
    shape: Shape,
    shadow: MinoShadow,
): Modifier =
    shadow.layers.fold(this) { modifier, layer ->
        modifier.dropShadow(shape = shape, shadow = layer)
    }
