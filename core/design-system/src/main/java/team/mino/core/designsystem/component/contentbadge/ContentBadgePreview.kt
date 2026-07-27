package team.mino.core.designsystem.component.contentbadge

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
private fun ContentBadgePreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Neutral — Solid, 크기별
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ContentBadgeSize.entries.forEach { size ->
                    MinoContentBadge(text = size.name, size = size, variant = ContentBadgeVariant.Solid)
                }
            }
            // Neutral — Outlined, 크기별
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ContentBadgeSize.entries.forEach { size ->
                    MinoContentBadge(text = size.name, size = size, variant = ContentBadgeVariant.Outlined)
                }
            }
            // Accent — Solid / Outlined (Figma 예시 색상: Cyan)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val accentColor = MinoContentBadgeDefaults.defaultAccentColor
                MinoContentBadge(
                    text = "Accent Solid",
                    variant = ContentBadgeVariant.Solid,
                    color = ContentBadgeColor.Accent(accentColor),
                )
                MinoContentBadge(
                    text = "Accent Outlined",
                    variant = ContentBadgeVariant.Outlined,
                    color = ContentBadgeColor.Accent(accentColor),
                )
            }
        }
    }
}
