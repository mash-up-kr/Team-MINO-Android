package team.mino.core.designsystem.component.switch

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import team.mino.core.designsystem.component.switch.token.SwitchTokens
import team.mino.core.designsystem.util.modifier.selectable.rippleSingleSelectable
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 켜짐·꺼짐 두 상태를 즉시 전환하는 스위치.
 *
 * Figma 노드 `2410:114367`(Switch 컴포넌트셋) variant `2410:114368`/`2410:114369`와 대조
 * 완료(disable variant는 노드 접근 불가로 미검증).
 *
 * @param checked 켜짐 여부.
 * @param onCheckedChange 상태가 바뀔 때 호출. `null`이면 클릭이 막혀 읽기 전용으로 표시된다.
 */
@Composable
fun MinoSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: MinoSwitchColors = MinoSwitchDefaults.colors(),
) {
    val trackColor = MinoSwitchDefaults.trackColor(colors, checked, enabled)
    val thumbColor = MinoSwitchDefaults.thumbColor(colors, checked, enabled)
    val thumbOffset = animateDpAsState(
        targetValue = if (checked) SwitchTokens.ThumbCheckedOffset else SwitchTokens.ThumbUncheckedOffset,
        label = "MinoSwitchThumbOffset",
    )
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .size(width = SwitchTokens.TrackWidth, height = SwitchTokens.TrackHeight)
            .surface(shape = SwitchTokens.TrackShape, containerColor = trackColor)
            .then(
                if (onCheckedChange != null) {
                    Modifier.rippleSingleSelectable(
                        selected = checked,
                        enabled = enabled,
                        role = Role.Switch,
                        onClick = { onCheckedChange(!checked) },
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(x = with(density) { thumbOffset.value.roundToPx() }, y = 0) }
                .size(SwitchTokens.ThumbSize)
                .background(color = thumbColor, shape = CircleShape),
        )
    }
}
