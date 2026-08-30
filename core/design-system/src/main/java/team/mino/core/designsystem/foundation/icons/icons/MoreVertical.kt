package team.mino.core.designsystem.foundation.icons.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

val MinoIcons.MoreVertical: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.MoreVertical",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(13.7497f, 18.75f)
                curveTo(13.7497f, 17.7835f, 12.9662f, 17f, 11.9997f, 17f)
                curveTo(11.0332f, 17f, 10.2497f, 17.7835f, 10.2497f, 18.75f)
                curveTo(10.2497f, 19.7165f, 11.0332f, 20.5f, 11.9997f, 20.5f)
                curveTo(12.9662f, 20.5f, 13.7497f, 19.7165f, 13.7497f, 18.75f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.9997f, 10.25f)
                curveTo(12.9662f, 10.25f, 13.7497f, 11.0335f, 13.7497f, 12f)
                curveTo(13.7497f, 12.9665f, 12.9662f, 13.75f, 11.9997f, 13.75f)
                curveTo(11.0332f, 13.75f, 10.2497f, 12.9665f, 10.2497f, 12f)
                curveTo(10.2497f, 11.0335f, 11.0332f, 10.25f, 11.9997f, 10.25f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.9997f, 3.5f)
                curveTo(12.9662f, 3.5f, 13.7497f, 4.2835f, 13.7497f, 5.25f)
                curveTo(13.7497f, 6.2165f, 12.9662f, 7f, 11.9997f, 7f)
                curveTo(11.0332f, 7f, 10.2497f, 6.2165f, 10.2497f, 5.25f)
                curveTo(10.2497f, 4.2835f, 11.0332f, 3.5f, 11.9997f, 3.5f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun MoreVerticalPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.MoreVertical,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
