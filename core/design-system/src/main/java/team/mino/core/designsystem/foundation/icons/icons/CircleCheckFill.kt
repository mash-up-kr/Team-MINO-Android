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

val MinoIcons.CircleCheckFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.CircleCheckFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(2.0999f, 12f)
                curveTo(2.0999f, 6.5324f, 6.5323f, 2.1f, 11.9999f, 2.1f)
                curveTo(17.4675f, 2.1f, 21.8999f, 6.5324f, 21.8999f, 12f)
                curveTo(21.8999f, 17.4676f, 17.4675f, 21.9f, 11.9999f, 21.9f)
                curveTo(6.5323f, 21.9f, 2.0999f, 17.4676f, 2.0999f, 12f)
                close()
                moveTo(16.6466f, 9.8758f)
                curveTo(16.9923f, 9.5187f, 16.983f, 8.9489f, 16.6258f, 8.6032f)
                curveTo(16.2687f, 8.2575f, 15.6989f, 8.2668f, 15.3532f, 8.6239f)
                lineTo(10.6773f, 13.4549f)
                lineTo(8.6475f, 11.3521f)
                curveTo(8.3023f, 10.9945f, 7.7325f, 10.9844f, 7.3749f, 11.3296f)
                curveTo(7.0173f, 11.6748f, 7.0072f, 12.2446f, 7.3524f, 12.6022f)
                lineTo(10.0289f, 15.3749f)
                curveTo(10.1983f, 15.5505f, 10.4318f, 15.6497f, 10.6758f, 15.6499f)
                curveTo(10.9198f, 15.65f, 11.1534f, 15.5511f, 11.3231f, 15.3758f)
                lineTo(16.6466f, 9.8758f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun CircleCheckFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.CircleCheckFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
