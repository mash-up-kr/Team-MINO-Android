package team.mino.core.designsystem.foundation.shadow

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.shadow.Shadow

/**
 * 여러 겹의 드롭 섀도로 구성되는 그림자 토큰 값. [layers] 순서대로 겹쳐 그린다.
 */
@Immutable
data class MinoShadow(
    val layers: List<Shadow>,
)
