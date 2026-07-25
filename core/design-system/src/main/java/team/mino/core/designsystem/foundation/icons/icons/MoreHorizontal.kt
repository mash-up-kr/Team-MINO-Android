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

val MinoIcons.MoreHorizontal: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.MoreHorizontal",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(5.2497f, 13.75f)
                curveTo(6.2162f, 13.75f, 6.9997f, 12.9665f, 6.9997f, 12f)
                curveTo(6.9997f, 11.0335f, 6.2162f, 10.25f, 5.2497f, 10.25f)
                curveTo(4.2832f, 10.25f, 3.4997f, 11.0335f, 3.4997f, 12f)
                curveTo(3.4997f, 12.9665f, 4.2832f, 13.75f, 5.2497f, 13.75f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(13.7497f, 12f)
                curveTo(13.7497f, 12.9665f, 12.9662f, 13.75f, 11.9997f, 13.75f)
                curveTo(11.0332f, 13.75f, 10.2497f, 12.9665f, 10.2497f, 12f)
                curveTo(10.2497f, 11.0335f, 11.0332f, 10.25f, 11.9997f, 10.25f)
                curveTo(12.9662f, 10.25f, 13.7497f, 11.0335f, 13.7497f, 12f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(20.4996f, 12f)
                curveTo(20.4996f, 12.9665f, 19.7161f, 13.75f, 18.7496f, 13.75f)
                curveTo(17.7831f, 13.75f, 16.9996f, 12.9665f, 16.9996f, 12f)
                curveTo(16.9996f, 11.0335f, 17.7831f, 10.25f, 18.7496f, 10.25f)
                curveTo(19.7161f, 10.25f, 20.4996f, 11.0335f, 20.4996f, 12f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun MoreHorizontalPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.MoreHorizontal,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
