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

val MinoIcons.PersonsFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.PersonsFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.1f, 7.9996f)
                curveTo(11.1f, 5.8457f, 12.8461f, 4.0996f, 15f, 4.0996f)
                curveTo(17.1539f, 4.0996f, 18.8999f, 5.8457f, 18.8999f, 7.9996f)
                curveTo(18.8999f, 10.1535f, 17.1539f, 11.8996f, 15f, 11.8996f)
                curveTo(12.8461f, 11.8996f, 11.1f, 10.1535f, 11.1f, 7.9996f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(14.9999f, 13.599f)
                curveTo(12.9461f, 13.599f, 11.0215f, 14.0587f, 9.5784f, 14.9471f)
                curveTo(8.1216f, 15.8439f, 7.0999f, 17.2259f, 7.0999f, 18.999f)
                lineTo(7.0998f, 19.0504f)
                curveTo(7.0991f, 19.2235f, 7.0982f, 19.4614f, 7.1566f, 19.6785f)
                curveTo(7.3094f, 20.2467f, 7.7533f, 20.6905f, 8.3215f, 20.8433f)
                curveTo(8.5386f, 20.9017f, 8.7765f, 20.9008f, 8.9496f, 20.9001f)
                lineTo(9.001f, 20.8999f)
                lineTo(20.9999f, 20.8991f)
                lineTo(21.0511f, 20.8993f)
                curveTo(21.2236f, 20.8999f, 21.4608f, 20.9008f, 21.6772f, 20.8428f)
                curveTo(22.2464f, 20.6902f, 22.691f, 20.2455f, 22.8436f, 19.6763f)
                curveTo(22.9016f, 19.4599f, 22.9007f, 19.2227f, 22.9f, 19.0502f)
                lineTo(22.8999f, 18.999f)
                curveTo(22.8999f, 17.2259f, 21.8783f, 15.8439f, 20.4215f, 14.9471f)
                curveTo(18.9784f, 14.0587f, 17.0538f, 13.599f, 14.9999f, 13.599f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(2.9745f, 16.1309f)
                curveTo(3.9928f, 15.47f, 5.308f, 15.1454f, 6.69f, 15.1036f)
                curveTo(5.849f, 16.1441f, 5.3f, 17.4502f, 5.3f, 18.9989f)
                lineTo(5.2996f, 19.0218f)
                curveTo(5.2973f, 19.1456f, 5.2877f, 19.6602f, 5.4184f, 20.146f)
                curveTo(5.4897f, 20.4111f, 5.5913f, 20.6633f, 5.7194f, 20.8986f)
                lineTo(2.5006f, 20.8989f)
                curveTo(1.7271f, 20.8989f, 1.1f, 20.2719f, 1.1f, 19.4983f)
                curveTo(1.1f, 18.0054f, 1.8472f, 16.8625f, 2.9745f, 16.1309f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(6.4999f, 7.0995f)
                curveTo(4.8983f, 7.0995f, 3.5999f, 8.3979f, 3.5999f, 9.9995f)
                curveTo(3.5999f, 11.6011f, 4.8983f, 12.8995f, 6.4999f, 12.8995f)
                curveTo(8.1015f, 12.8995f, 9.3999f, 11.6011f, 9.3999f, 9.9995f)
                curveTo(9.3999f, 8.3979f, 8.1015f, 7.0995f, 6.4999f, 7.0995f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun PersonsFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.PersonsFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
