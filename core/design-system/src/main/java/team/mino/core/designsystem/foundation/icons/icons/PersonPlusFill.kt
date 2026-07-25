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

val MinoIcons.PersonPlusFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.PersonPlusFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(12f, 2.8529f)
                curveTo(9.57f, 2.8529f, 7.6001f, 4.8229f, 7.6001f, 7.2529f)
                curveTo(7.6001f, 9.683f, 9.57f, 11.6529f, 12f, 11.6529f)
                curveTo(14.4301f, 11.6529f, 16.4f, 9.683f, 16.4f, 7.2529f)
                curveTo(16.4f, 4.8229f, 14.4301f, 2.8529f, 12f, 2.8529f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(5.9163f, 14.9211f)
                curveTo(7.5306f, 14.0516f, 9.688f, 13.599f, 12f, 13.599f)
                curveTo(14.3119f, 13.599f, 16.4693f, 14.0516f, 18.0836f, 14.9211f)
                curveTo(19.6967f, 15.7901f, 20.8999f, 17.1652f, 20.8999f, 18.999f)
                lineTo(20.8999f, 19.3259f)
                curveTo(20.9f, 19.5125f, 20.9f, 19.7026f, 20.8867f, 19.865f)
                curveTo(20.872f, 20.0457f, 20.8364f, 20.2699f, 20.7201f, 20.4982f)
                curveTo(20.5619f, 20.8086f, 20.3095f, 21.061f, 19.9991f, 21.2192f)
                curveTo(19.7709f, 21.3355f, 19.5466f, 21.3711f, 19.3659f, 21.3859f)
                curveTo(19.2035f, 21.3992f, 19.0135f, 21.3991f, 18.8268f, 21.3991f)
                lineTo(5.1733f, 21.3999f)
                curveTo(4.9867f, 21.4f, 4.7966f, 21.4f, 4.6342f, 21.3868f)
                curveTo(4.4535f, 21.372f, 4.2292f, 21.3364f, 4.001f, 21.2201f)
                curveTo(3.6905f, 21.062f, 3.4381f, 20.8095f, 3.2799f, 20.4991f)
                curveTo(3.1635f, 20.2708f, 3.128f, 20.0466f, 3.1132f, 19.8658f)
                curveTo(3.0999f, 19.7034f, 3.1f, 19.5134f, 3.1f, 19.3267f)
                lineTo(3.1f, 18.999f)
                curveTo(3.1f, 17.1652f, 4.3032f, 15.7901f, 5.9163f, 14.9211f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(21.4001f, 8.5004f)
                curveTo(21.4001f, 8.0033f, 20.9972f, 7.6004f, 20.5001f, 7.6004f)
                curveTo(20.0031f, 7.6004f, 19.6001f, 8.0033f, 19.6001f, 8.5004f)
                verticalLineTo(10.1004f)
                horizontalLineTo(18.0001f)
                curveTo(17.5031f, 10.1004f, 17.1001f, 10.5033f, 17.1001f, 11.0004f)
                curveTo(17.1001f, 11.4974f, 17.5031f, 11.9004f, 18.0001f, 11.9004f)
                horizontalLineTo(19.6001f)
                verticalLineTo(13.5004f)
                curveTo(19.6001f, 13.9974f, 20.0031f, 14.4004f, 20.5001f, 14.4004f)
                curveTo(20.9972f, 14.4004f, 21.4001f, 13.9974f, 21.4001f, 13.5004f)
                verticalLineTo(11.9004f)
                horizontalLineTo(23.0001f)
                curveTo(23.4972f, 11.9004f, 23.9001f, 11.4974f, 23.9001f, 11.0004f)
                curveTo(23.9001f, 10.5033f, 23.4972f, 10.1004f, 23.0001f, 10.1004f)
                horizontalLineTo(21.4001f)
                verticalLineTo(8.5004f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun PersonPlusFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.PersonPlusFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
