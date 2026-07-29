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
            // Figma 컴포넌트셋 전수(Color 2 × Variant 2 × Size 3)
            ContentBadgeColor.entries.forEach { color ->
                ContentBadgeVariant.entries.forEach { variant ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ContentBadgeSize.entries.forEach { size ->
                            MinoContentBadge(
                                text = "${color.name} ${variant.name}",
                                size = size,
                                variant = variant,
                                color = color,
                            )
                        }
                    }
                }
            }
        }
    }
}
