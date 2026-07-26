package team.mino.core.designsystem.component.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

@UiModePreviews
@Composable
private fun ChipPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Solid — 크기별
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipSize.entries.forEach { size ->
                    MinoChip(text = size.name, onClick = {}, size = size, variant = ChipVariant.Solid)
                }
            }
            // Solid — active 상태
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MinoChip(text = "비활성 선택", onClick = {}, variant = ChipVariant.Solid, active = false)
                MinoChip(text = "선택됨", onClick = {}, variant = ChipVariant.Solid, active = true)
                MinoChip(text = "비활성화", onClick = {}, variant = ChipVariant.Solid, enabled = false)
            }
            // Outlined — 크기별
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipSize.entries.forEach { size ->
                    MinoChip(text = size.name, onClick = {}, size = size, variant = ChipVariant.Outlined)
                }
            }
            // Outlined — active 상태
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MinoChip(text = "선택 안 됨", onClick = {}, variant = ChipVariant.Outlined, active = false)
                MinoChip(text = "선택됨", onClick = {}, variant = ChipVariant.Outlined, active = true)
                MinoChip(text = "비활성화", onClick = {}, variant = ChipVariant.Outlined, enabled = false)
            }
        }
    }
}
