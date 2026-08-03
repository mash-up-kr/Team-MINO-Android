package team.mino.core.designsystem.component.chip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.component.chip.token.ChipTokens
import team.mino.core.designsystem.component.chip.token.contentPadding
import team.mino.core.designsystem.component.chip.token.contentSize
import team.mino.core.designsystem.component.chip.token.contentSpacing
import team.mino.core.designsystem.component.chip.token.font
import team.mino.core.designsystem.component.chip.token.shape
import team.mino.core.designsystem.component.chip.token.textHorizontalPadding
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 항목을 제어하거나 이동할 때 쓰는 낮은 시각 위계의 칩(Figma `Chip/Chip`).
 *
 * @param active 선택된 상태 표시. [ChipVariant.Solid]는 배경이 채워지고, [ChipVariant.Outlined]는
 *   배경이 옅게 틴트되며 테두리가 진해진다.
 * @param leadingContent 글자 앞 정사각 슬롯. Figma `content` 속성이 아이콘(기본)·썸네일 둘 다
 *   허용하므로 슬롯으로 열어 둔다. 크기는 [size]에 맞춰 강제되고, `Icon`은 글자와 같은 색을
 *   `LocalContentColor`로 물려받는다. `null`이면 표시하지 않는다.
 * @param trailingContent 글자 뒤 정사각 슬롯. 규칙은 [leadingContent]와 같다.
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
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val containerColor = MinoChipDefaults.containerColor(colors, variant, active, enabled)
    val contentColor = MinoChipDefaults.contentColor(colors, variant, active, enabled)
    val borderColor = MinoChipDefaults.borderColor(colors, variant, active, enabled)

    Row(
        modifier = modifier
            .surface(
                shape = size.shape(),
                containerColor = containerColor,
                borderColor = borderColor,
                borderWidth = ChipTokens.BorderWidth,
            ).rippleSingleClickable(enabled = enabled, onClick = onClick)
            .padding(size.contentPadding()),
        horizontalArrangement = Arrangement.spacedBy(size.contentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            if (leadingContent != null) {
                ChipContentSlot(size = size, content = leadingContent)
            }
            Text(
                modifier = Modifier.padding(horizontal = size.textHorizontalPadding),
                text = text,
                color = contentColor,
                style = size.font.value,
            )
            if (trailingContent != null) {
                ChipContentSlot(size = size, content = trailingContent)
            }
        }
    }
}

@Composable
private fun ChipContentSlot(
    size: ChipSize,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.size(size.contentSize),
        contentAlignment = Alignment.Center,
    ) {
        content()
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
