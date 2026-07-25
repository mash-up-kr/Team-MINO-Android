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

val MinoIcons.CompassFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.CompassFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(13.4999f, 12.0002f)
                curveTo(13.4999f, 12.8286f, 12.8283f, 13.5002f, 11.9999f, 13.5002f)
                curveTo(11.1715f, 13.5002f, 10.4999f, 12.8286f, 10.4999f, 12.0002f)
                curveTo(10.4999f, 11.1718f, 11.1715f, 10.5002f, 11.9999f, 10.5002f)
                curveTo(12.8283f, 10.5002f, 13.4999f, 11.1718f, 13.4999f, 12.0002f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(2.1001f, 11.9996f)
                curveTo(2.1001f, 6.532f, 6.5325f, 2.0996f, 12.0001f, 2.0996f)
                curveTo(17.4677f, 2.0996f, 21.9001f, 6.532f, 21.9001f, 11.9996f)
                curveTo(21.9001f, 17.4672f, 17.4677f, 21.8996f, 12.0001f, 21.8996f)
                curveTo(6.5325f, 21.8996f, 2.1001f, 17.4672f, 2.1001f, 11.9996f)
                close()
                moveTo(9.9092f, 9.6766f)
                curveTo(9.8209f, 9.7425f, 9.7426f, 9.8209f, 9.6766f, 9.9092f)
                curveTo(9.5956f, 10.0176f, 9.5432f, 10.1373f, 9.53f, 10.1674f)
                lineTo(9.5275f, 10.173f)
                lineTo(7.3683f, 14.9232f)
                curveTo(7.3099f, 15.0516f, 7.2441f, 15.1962f, 7.2001f, 15.3205f)
                lineTo(7.1989f, 15.3239f)
                curveTo(7.1641f, 15.422f, 7.0544f, 15.7316f, 7.1623f, 16.0796f)
                curveTo(7.2745f, 16.4417f, 7.5581f, 16.7253f, 7.9202f, 16.8375f)
                curveTo(8.2682f, 16.9454f, 8.5777f, 16.8357f, 8.6759f, 16.8009f)
                lineTo(8.6793f, 16.7997f)
                curveTo(8.8035f, 16.7557f, 8.9481f, 16.6899f, 9.0766f, 16.6315f)
                lineTo(13.8268f, 14.4723f)
                lineTo(13.8324f, 14.4698f)
                curveTo(13.8624f, 14.4566f, 13.9822f, 14.4042f, 14.0906f, 14.3232f)
                curveTo(14.1789f, 14.2572f, 14.2572f, 14.1788f, 14.3232f, 14.0906f)
                curveTo(14.4043f, 13.9822f, 14.4567f, 13.8624f, 14.4698f, 13.8323f)
                lineTo(14.4723f, 13.8267f)
                lineTo(16.6315f, 9.0766f)
                curveTo(16.6899f, 8.9481f, 16.7557f, 8.8035f, 16.7997f, 8.6793f)
                lineTo(16.8009f, 8.6759f)
                curveTo(16.8357f, 8.5777f, 16.9454f, 8.2682f, 16.8375f, 7.9202f)
                curveTo(16.7253f, 7.558f, 16.4417f, 7.2745f, 16.0796f, 7.1623f)
                curveTo(15.7316f, 7.0544f, 15.4221f, 7.1641f, 15.3239f, 7.1989f)
                lineTo(15.3205f, 7.2001f)
                curveTo(15.1962f, 7.2441f, 15.0517f, 7.3099f, 14.9232f, 7.3683f)
                lineTo(10.1731f, 9.5275f)
                lineTo(10.1674f, 9.53f)
                curveTo(10.1374f, 9.5431f, 10.0176f, 9.5955f, 9.9092f, 9.6766f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun CompassFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.CompassFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
