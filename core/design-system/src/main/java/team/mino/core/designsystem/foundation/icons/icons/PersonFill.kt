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

val MinoIcons.PersonFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.PersonFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(7.6001f, 7.2529f)
                curveTo(7.6001f, 4.8228f, 9.57f, 2.8529f, 12f, 2.8529f)
                curveTo(14.4301f, 2.8529f, 16.4f, 4.8228f, 16.4f, 7.2529f)
                curveTo(16.4f, 9.683f, 14.4301f, 11.6529f, 12f, 11.6529f)
                curveTo(9.57f, 11.6529f, 7.6001f, 9.683f, 7.6001f, 7.2529f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(12f, 13.599f)
                curveTo(9.688f, 13.599f, 7.5306f, 14.0516f, 5.9163f, 14.9211f)
                curveTo(4.3032f, 15.79f, 3.1f, 17.1652f, 3.1f, 18.999f)
                lineTo(3.1f, 19.3267f)
                curveTo(3.1f, 19.5133f, 3.0999f, 19.7034f, 3.1132f, 19.8658f)
                curveTo(3.128f, 20.0465f, 3.1635f, 20.2708f, 3.2799f, 20.499f)
                curveTo(3.4381f, 20.8095f, 3.6905f, 21.0619f, 4.001f, 21.2201f)
                curveTo(4.2292f, 21.3364f, 4.4535f, 21.372f, 4.6342f, 21.3867f)
                curveTo(4.7966f, 21.4f, 4.9867f, 21.3999f, 5.1733f, 21.3999f)
                lineTo(18.8268f, 21.3991f)
                curveTo(19.0135f, 21.3991f, 19.2035f, 21.3991f, 19.3659f, 21.3859f)
                curveTo(19.5466f, 21.3711f, 19.7709f, 21.3355f, 19.9991f, 21.2192f)
                curveTo(20.3095f, 21.061f, 20.5619f, 20.8086f, 20.7201f, 20.4981f)
                curveTo(20.8364f, 20.2699f, 20.872f, 20.0457f, 20.8867f, 19.865f)
                curveTo(20.9f, 19.7025f, 20.9f, 19.5125f, 20.8999f, 19.3259f)
                lineTo(20.8999f, 18.999f)
                curveTo(20.8999f, 17.1652f, 19.6967f, 15.79f, 18.0836f, 14.9211f)
                curveTo(16.4693f, 14.0516f, 14.3119f, 13.599f, 12f, 13.599f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun PersonFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.PersonFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
