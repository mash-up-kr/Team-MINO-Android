package team.mino.core.designsystem.foundation.icons.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

val MinoIcons.Person: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Person",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(12.0001f, 2.601f)
                curveTo(9.2939f, 2.601f, 7.1001f, 4.7948f, 7.1001f, 7.501f)
                curveTo(7.1001f, 10.2072f, 9.2939f, 12.401f, 12.0001f, 12.401f)
                curveTo(14.7063f, 12.401f, 16.9001f, 10.2072f, 16.9001f, 7.501f)
                curveTo(16.9001f, 4.7948f, 14.7063f, 2.601f, 12.0001f, 2.601f)
                close()
                moveTo(8.9001f, 7.501f)
                curveTo(8.9001f, 5.7889f, 10.288f, 4.401f, 12.0001f, 4.401f)
                curveTo(13.7121f, 4.401f, 15.1001f, 5.7889f, 15.1001f, 7.501f)
                curveTo(15.1001f, 9.2131f, 13.7121f, 10.601f, 12.0001f, 10.601f)
                curveTo(10.288f, 10.601f, 8.9001f, 9.2131f, 8.9001f, 7.501f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(12f, 13.5994f)
                curveTo(9.6881f, 13.5994f, 7.5306f, 14.052f, 5.9163f, 14.9215f)
                curveTo(4.3032f, 15.7904f, 3.1f, 17.1656f, 3.1f, 18.9994f)
                lineTo(3.1f, 19.3271f)
                curveTo(3.1f, 19.5138f, 3.0999f, 19.7038f, 3.1132f, 19.8662f)
                curveTo(3.128f, 20.047f, 3.1636f, 20.2712f, 3.2799f, 20.4995f)
                curveTo(3.4381f, 20.8099f, 3.6905f, 21.0624f, 4.001f, 21.2205f)
                curveTo(4.2292f, 21.3368f, 4.4535f, 21.3724f, 4.6342f, 21.3871f)
                curveTo(4.7967f, 21.4004f, 4.9867f, 21.4004f, 5.1733f, 21.4003f)
                lineTo(18.8269f, 21.3995f)
                curveTo(19.0135f, 21.3995f, 19.2035f, 21.3996f, 19.3659f, 21.3863f)
                curveTo(19.5467f, 21.3715f, 19.7709f, 21.3359f, 19.9991f, 21.2196f)
                curveTo(20.3096f, 21.0614f, 20.562f, 20.809f, 20.7201f, 20.4985f)
                curveTo(20.8364f, 20.2703f, 20.872f, 20.0461f, 20.8867f, 19.8654f)
                curveTo(20.9f, 19.7029f, 20.9f, 19.5129f, 20.9f, 19.3263f)
                lineTo(20.9f, 18.9994f)
                curveTo(20.9f, 17.1656f, 19.6968f, 15.7904f, 18.0836f, 14.9215f)
                curveTo(16.4694f, 14.052f, 14.3119f, 13.5994f, 12f, 13.5994f)
                close()
                moveTo(4.9f, 18.9994f)
                curveTo(4.9f, 18.0718f, 5.4877f, 17.197f, 6.7699f, 16.5062f)
                curveTo(8.0511f, 15.8161f, 9.8936f, 15.3994f, 12f, 15.3994f)
                curveTo(14.1063f, 15.3994f, 15.9489f, 15.8161f, 17.23f, 16.5062f)
                curveTo(18.5123f, 17.197f, 19.1f, 18.0718f, 19.1f, 18.9994f)
                lineTo(19.0981f, 19.5977f)
                lineTo(4.9018f, 19.5985f)
                lineTo(4.9f, 18.9994f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun PersonPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Person,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
