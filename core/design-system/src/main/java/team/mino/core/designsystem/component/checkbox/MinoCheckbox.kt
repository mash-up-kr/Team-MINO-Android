package team.mino.core.designsystem.component.checkbox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import team.mino.core.designsystem.component.checkbox.token.CheckboxTokens
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Check
import team.mino.core.designsystem.util.modifier.selectable.rippleSingleSelectable
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 선택 여부를 켜고 끄는 체크박스(Figma `Checkbox/Resource/Control`).
 *
 * 상자 바깥 여백까지가 탭 영역이고, 리플은 그보다 넓은 원을 그린다.
 *
 * @param enabled false면 입력을 막고 [CheckboxTokens.DisabledOpacity]만큼 흐려진다. 색은 [checked]
 *   상태를 그대로 따른다 — Figma가 비활성을 색이 아니라 불투명도로 정의하기 때문이며, 그 근거는
 *   [CheckboxTokens.DisabledOpacity]가 갖는다. 흐려지는 범위는 이 컴포넌트까지다 — 「체크된 채 비활성」
 *   카드에서 방 이름·썸네일이 함께 흐려지지 않는 것은 호출부가 이 컴포넌트에만 `enabled`를 주기 때문이다.
 */
@Composable
fun MinoCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: MinoCheckboxColors = MinoCheckboxDefaults.colors(),
) {
    Box(
        modifier = modifier
            .then(if (enabled) Modifier else Modifier.alpha(CheckboxTokens.DisabledOpacity))
            .rippleSingleSelectable(
                selected = checked,
                enabled = enabled,
                role = Role.Checkbox,
                rippleRadius = CheckboxTokens.RippleRadius,
                onClick = { onCheckedChange(!checked) },
            ).padding(CheckboxTokens.ContainerPadding),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(CheckboxTokens.BoxSize)
                .surface(
                    shape = MinoCheckboxDefaults.shape,
                    containerColor = colors.containerColor(checked),
                    borderColor = colors.borderColor(checked),
                    borderWidth = CheckboxTokens.BoxBorderWidth,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MinoIcons.Check,
                contentDescription = null,
                tint = colors.checkmarkColor(checked),
                modifier = Modifier.size(CheckboxTokens.CheckmarkSize),
            )
        }
    }
}
