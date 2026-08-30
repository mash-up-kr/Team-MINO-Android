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

val MinoIcons.MenuThick: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.MenuThick",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(4.0002f, 4.4995f)
                curveTo(3.3099f, 4.4995f, 2.7502f, 5.0591f, 2.7502f, 5.7495f)
                curveTo(2.7502f, 6.4398f, 3.3099f, 6.9995f, 4.0002f, 6.9995f)
                horizontalLineTo(20.0002f)
                curveTo(20.6905f, 6.9995f, 21.2502f, 6.4398f, 21.2502f, 5.7495f)
                curveTo(21.2502f, 5.0591f, 20.6905f, 4.4995f, 20.0002f, 4.4995f)
                horizontalLineTo(4.0002f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(2.7502f, 11.9997f)
                curveTo(2.7502f, 11.3093f, 3.3099f, 10.7497f, 4.0002f, 10.7497f)
                horizontalLineTo(20.0002f)
                curveTo(20.6905f, 10.7497f, 21.2502f, 11.3093f, 21.2502f, 11.9997f)
                curveTo(21.2502f, 12.69f, 20.6905f, 13.2497f, 20.0002f, 13.2497f)
                horizontalLineTo(4.0002f)
                curveTo(3.3099f, 13.2497f, 2.7502f, 12.69f, 2.7502f, 11.9997f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(2.7502f, 18.2494f)
                curveTo(2.7502f, 17.559f, 3.3099f, 16.9994f, 4.0002f, 16.9994f)
                horizontalLineTo(20.0002f)
                curveTo(20.6905f, 16.9994f, 21.2502f, 17.559f, 21.2502f, 18.2494f)
                curveTo(21.2502f, 18.9397f, 20.6905f, 19.4994f, 20.0002f, 19.4994f)
                horizontalLineTo(4.0002f)
                curveTo(3.3099f, 19.4994f, 2.7502f, 18.9397f, 2.7502f, 18.2494f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun MenuThickPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.MenuThick,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
