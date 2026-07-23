package team.mino.core.designsystem.foundation.color

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.shape.token.ShapeAccessKeyToken
import team.mino.core.designsystem.foundation.shape.token.value
import team.mino.core.designsystem.foundation.typography.token.TypographyAccessKeyToken
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

@UiModePreviews
@Composable
private fun ColorSchemePreview() {
    MinoAndroidAppTheme {
        Column(
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ColorAccessKeyToken.entries.forEach { token ->
                ColorTokenRow(token = token)
            }
        }
    }
}

@Composable
private fun ColorTokenRow(
    token: ColorAccessKeyToken,
    modifier: Modifier = Modifier,
) {
    val color = token.value
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = color,
                    shape = ShapeAccessKeyToken.Small.value,
                ).border(
                    width = 1.dp,
                    color = ColorAccessKeyToken.LineNormalNormal.value,
                    shape = ShapeAccessKeyToken.Small.value,
                ),
        )
        Text(
            text = "${token.name} ${color.toHexString()}",
            style = TypographyAccessKeyToken.Label2Regular.value.copy(
                color = ColorAccessKeyToken.LabelNormal.value,
            ),
        )
    }
}

private fun Color.toHexString(): String =
    "#" +
        toArgb()
            .toUInt()
            .toString(radix = 16)
            .uppercase()
            .padStart(8, '0')
