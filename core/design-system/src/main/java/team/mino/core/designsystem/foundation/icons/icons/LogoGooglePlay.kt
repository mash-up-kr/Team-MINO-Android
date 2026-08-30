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

val MinoIcons.LogoGooglePlay: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.LogoGooglePlay",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(15.7177f, 7.7244f)
                lineTo(7.2294f, 2.803f)
                curveTo(6.9116f, 2.6109f, 6.5347f, 2.5f, 6.1283f, 2.5f)
                curveTo(5.2987f, 2.5f, 4.5797f, 2.979f, 4.229f, 3.6705f)
                lineTo(12.0033f, 11.4407f)
                lineTo(15.7177f, 7.7244f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(4.0007f, 4.5735f)
                curveTo(4.0002f, 4.5916f, 3.9999f, 4.61f, 3.9999f, 4.6284f)
                verticalLineTo(19.3865f)
                curveTo(3.9999f, 19.407f, 4.0002f, 19.4273f, 4.0007f, 19.4475f)
                lineTo(11.4377f, 12.0066f)
                lineTo(4.0007f, 4.5735f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(4.2353f, 20.3445f)
                curveTo(4.5883f, 21.032f, 5.3037f, 21.5001f, 6.1283f, 21.5001f)
                curveTo(6.5199f, 21.5001f, 6.8894f, 21.3966f, 7.2072f, 21.2045f)
                lineTo(7.2294f, 21.1897f)
                lineTo(15.7236f, 16.2903f)
                lineTo(12.0036f, 12.5721f)
                lineTo(4.2353f, 20.3445f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(16.4486f, 15.8837f)
                lineTo(19.896f, 13.8956f)
                curveTo(20.5611f, 13.5335f, 21.0119f, 12.8314f, 21.0119f, 12.0259f)
                curveTo(21.0119f, 11.2204f, 20.5685f, 10.5183f, 19.9034f, 10.1636f)
                verticalLineTo(10.1562f)
                horizontalLineTo(19.896f)
                lineTo(16.4332f, 8.1401f)
                lineTo(12.5691f, 12.0063f)
                lineTo(16.4486f, 15.8837f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun LogoGooglePlayPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.LogoGooglePlay,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
