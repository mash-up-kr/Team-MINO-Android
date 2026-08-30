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

val MinoIcons.MoreVerticalTight: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.MoreVerticalTight",
            defaultWidth = 12.dp,
            defaultHeight = 24.dp,
            viewportWidth = 12f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(7.7497f, 18.75f)
                curveTo(7.7497f, 17.7835f, 6.9662f, 17f, 5.9997f, 17f)
                curveTo(5.0332f, 17f, 4.2497f, 17.7835f, 4.2497f, 18.75f)
                curveTo(4.2497f, 19.7165f, 5.0332f, 20.5f, 5.9997f, 20.5f)
                curveTo(6.9662f, 20.5f, 7.7497f, 19.7165f, 7.7497f, 18.75f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(5.9997f, 10.25f)
                curveTo(6.9662f, 10.25f, 7.7497f, 11.0335f, 7.7497f, 12f)
                curveTo(7.7497f, 12.9665f, 6.9662f, 13.75f, 5.9997f, 13.75f)
                curveTo(5.0332f, 13.75f, 4.2497f, 12.9665f, 4.2497f, 12f)
                curveTo(4.2497f, 11.0335f, 5.0332f, 10.25f, 5.9997f, 10.25f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(5.9997f, 3.5f)
                curveTo(6.9662f, 3.5f, 7.7497f, 4.2835f, 7.7497f, 5.25f)
                curveTo(7.7497f, 6.2165f, 6.9662f, 7f, 5.9997f, 7f)
                curveTo(5.0332f, 7f, 4.2497f, 6.2165f, 4.2497f, 5.25f)
                curveTo(4.2497f, 4.2835f, 5.0332f, 3.5f, 5.9997f, 3.5f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun MoreVerticalTightPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.MoreVerticalTight,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
