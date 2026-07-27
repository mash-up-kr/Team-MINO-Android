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

val MinoIcons.Menu: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Menu",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(4.0003f, 4.8495f)
                curveTo(3.5033f, 4.8495f, 3.1003f, 5.2524f, 3.1003f, 5.7495f)
                curveTo(3.1003f, 6.2465f, 3.5033f, 6.6495f, 4.0003f, 6.6495f)
                horizontalLineTo(20.0002f)
                curveTo(20.4973f, 6.6495f, 20.9002f, 6.2465f, 20.9002f, 5.7495f)
                curveTo(20.9002f, 5.2524f, 20.4973f, 4.8495f, 20.0002f, 4.8495f)
                horizontalLineTo(4.0003f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(3.1003f, 11.9997f)
                curveTo(3.1003f, 11.5026f, 3.5033f, 11.0997f, 4.0003f, 11.0997f)
                horizontalLineTo(20.0003f)
                curveTo(20.4973f, 11.0997f, 20.9003f, 11.5026f, 20.9003f, 11.9997f)
                curveTo(20.9003f, 12.4967f, 20.4973f, 12.8997f, 20.0003f, 12.8997f)
                horizontalLineTo(4.0003f)
                curveTo(3.5033f, 12.8997f, 3.1003f, 12.4967f, 3.1003f, 11.9997f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(3.1003f, 18.2494f)
                curveTo(3.1003f, 17.7523f, 3.5033f, 17.3494f, 4.0003f, 17.3494f)
                horizontalLineTo(20.0003f)
                curveTo(20.4973f, 17.3494f, 20.9003f, 17.7523f, 20.9003f, 18.2494f)
                curveTo(20.9003f, 18.7464f, 20.4973f, 19.1494f, 20.0003f, 19.1494f)
                horizontalLineTo(4.0003f)
                curveTo(3.5033f, 19.1494f, 3.1003f, 18.7464f, 3.1003f, 18.2494f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun MenuPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Menu,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
