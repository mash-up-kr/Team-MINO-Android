package team.mino.core.designsystem.component.checkbox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * @param enabled false면 입력만 막는다. Figma에 비활성 상태의 색이 정의되어 있지 않아
 *   색은 [checked] 상태만 따른다 — [MinoCheckboxColors] 참조.
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
