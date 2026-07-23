package team.mino.core.designsystem.foundation.shadow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.shadow.token.ShadowAccessKeyToken
import team.mino.core.designsystem.foundation.shadow.token.value
import team.mino.core.designsystem.foundation.shape.token.ShapeAccessKeyToken
import team.mino.core.designsystem.foundation.shape.token.value
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.modifier.shadow.dropShadow
import team.mino.core.designsystem.util.preview.UiModePreviews

@UiModePreviews
@Composable
private fun ShadowsPreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ShadowAccessKeyToken.entries.forEach { token ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .dropShadow(
                                shape = ShapeAccessKeyToken.Medium.value,
                                shadow = token.value,
                            ).background(
                                color = ColorAccessKeyToken.BackgroundElevatedNormal.value,
                                shape = ShapeAccessKeyToken.Medium.value,
                            ),
                    )
                    Text(
                        text = token.name,
                        style = TypographyAccessKeyToken.Label2Regular.value.copy(
                            color = ColorAccessKeyToken.LabelNormal.value,
                        ),
                    )
                }
            }
        }
    }
}
