package team.mino.core.designsystem.component.chip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.component.chip.token.ChipTokens
import team.mino.core.designsystem.component.chip.token.contentPadding
import team.mino.core.designsystem.component.chip.token.shape
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 항목을 제어하거나 이동할 때 쓰는 낮은 시각 위계의 칩(Figma `Chip/Chip`).
 *
 * @param active 선택된 상태 표시. [ChipVariant.Solid]는 배경이 채워지고, [ChipVariant.Outlined]는
 *   배경이 옅게 틴트되며 테두리가 진해진다.
 */
@Composable
fun MinoChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: ChipSize = ChipSize.Medium,
    variant: ChipVariant = ChipVariant.Solid,
    active: Boolean = false,
    enabled: Boolean = true,
    colors: MinoChipColors = MinoChipDefaults.colors(),
) {
    val containerColor = MinoChipDefaults.containerColor(colors, variant, active, enabled)
    val contentColor = MinoChipDefaults.contentColor(colors, variant, active, enabled)
    val borderColor = MinoChipDefaults.borderColor(colors, variant, active, enabled)

    Box(
        modifier = modifier
            .surface(
                shape = size.shape(),
                containerColor = containerColor,
                borderColor = borderColor,
                borderWidth = ChipTokens.BorderWidth,
            ).rippleSingleClickable(enabled = enabled, onClick = onClick)
            .padding(size.contentPadding()),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = contentColor, style = ChipTokens.TextFont.value)
    }
}

/** [MinoChip]의 크기. Figma `Size` 속성(XSmall·Small·Medium·Large)에 대응. */
enum class ChipSize {
    XSmall,
    Small,
    Medium,
    Large,
}

/** [MinoChip]의 배경 스타일. Figma `Variant` 속성(Solid·Outlined)에 대응. */
enum class ChipVariant {
    Solid,
    Outlined,
}
