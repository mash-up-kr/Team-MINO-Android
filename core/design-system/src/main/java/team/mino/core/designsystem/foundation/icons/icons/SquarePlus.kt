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

val MinoIcons.SquarePlus: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.SquarePlus",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(12.9f, 8.0005f)
                curveTo(12.9f, 7.5034f, 12.4971f, 7.1005f, 12f, 7.1005f)
                curveTo(11.503f, 7.1005f, 11.1f, 7.5034f, 11.1f, 8.0005f)
                verticalLineTo(11.1005f)
                horizontalLineTo(8f)
                curveTo(7.503f, 11.1005f, 7.1f, 11.5034f, 7.1f, 12.0005f)
                curveTo(7.1f, 12.4975f, 7.503f, 12.9005f, 8f, 12.9005f)
                horizontalLineTo(11.1f)
                verticalLineTo(16.0005f)
                curveTo(11.1f, 16.4976f, 11.503f, 16.9005f, 12f, 16.9005f)
                curveTo(12.4971f, 16.9005f, 12.9f, 16.4976f, 12.9f, 16.0005f)
                verticalLineTo(12.9005f)
                horizontalLineTo(16f)
                curveTo(16.4971f, 12.9005f, 16.9f, 12.4975f, 16.9f, 12.0005f)
                curveTo(16.9f, 11.5034f, 16.4971f, 11.1005f, 16f, 11.1005f)
                horizontalLineTo(12.9f)
                verticalLineTo(8.0005f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(8.2626f, 2.6021f)
                curveTo(7.4546f, 2.602f, 6.7937f, 2.602f, 6.2566f, 2.6459f)
                curveTo(5.7007f, 2.6913f, 5.1986f, 2.7882f, 4.7298f, 3.0271f)
                curveTo(3.9959f, 3.401f, 3.3993f, 3.9977f, 3.0254f, 4.7315f)
                curveTo(2.7865f, 5.2004f, 2.6896f, 5.7025f, 2.6442f, 6.2584f)
                curveTo(2.6003f, 6.7954f, 2.6003f, 7.4563f, 2.6003f, 8.2643f)
                verticalLineTo(15.7398f)
                curveTo(2.6003f, 16.5478f, 2.6003f, 17.2087f, 2.6442f, 17.7457f)
                curveTo(2.6896f, 18.3016f, 2.7865f, 18.8037f, 3.0254f, 19.2726f)
                curveTo(3.3993f, 20.0064f, 3.9959f, 20.6031f, 4.7298f, 20.977f)
                curveTo(5.1986f, 21.2159f, 5.7007f, 21.3128f, 6.2566f, 21.3582f)
                curveTo(6.7937f, 21.4021f, 7.4546f, 21.4021f, 8.2626f, 21.4021f)
                horizontalLineTo(15.738f)
                curveTo(16.5461f, 21.4021f, 17.2069f, 21.4021f, 17.744f, 21.3582f)
                curveTo(18.2999f, 21.3128f, 18.802f, 21.2159f, 19.2709f, 20.977f)
                curveTo(20.0047f, 20.6031f, 20.6013f, 20.0064f, 20.9753f, 19.2726f)
                curveTo(21.2142f, 18.8037f, 21.311f, 18.3016f, 21.3565f, 17.7457f)
                curveTo(21.4003f, 17.2087f, 21.4003f, 16.5478f, 21.4003f, 15.7398f)
                verticalLineTo(8.2644f)
                curveTo(21.4003f, 7.4563f, 21.4003f, 6.7954f, 21.3565f, 6.2584f)
                curveTo(21.311f, 5.7025f, 21.2142f, 5.2004f, 20.9753f, 4.7315f)
                curveTo(20.6013f, 3.9977f, 20.0047f, 3.401f, 19.2709f, 3.0271f)
                curveTo(18.802f, 2.7882f, 18.2999f, 2.6913f, 17.744f, 2.6459f)
                curveTo(17.2069f, 2.602f, 16.5461f, 2.602f, 15.7381f, 2.6021f)
                horizontalLineTo(8.2626f)
                close()
                moveTo(5.5469f, 4.6309f)
                curveTo(5.7198f, 4.5429f, 5.9586f, 4.4763f, 6.4032f, 4.4399f)
                curveTo(6.8583f, 4.4028f, 7.4454f, 4.4021f, 8.3003f, 4.4021f)
                horizontalLineTo(15.7003f)
                curveTo(16.5553f, 4.4021f, 17.1423f, 4.4028f, 17.5974f, 4.4399f)
                curveTo(18.042f, 4.4763f, 18.2809f, 4.5429f, 18.4537f, 4.6309f)
                curveTo(18.8488f, 4.8323f, 19.1701f, 5.1535f, 19.3714f, 5.5487f)
                curveTo(19.4595f, 5.7215f, 19.5261f, 5.9604f, 19.5624f, 6.405f)
                curveTo(19.5996f, 6.8601f, 19.6003f, 7.4471f, 19.6003f, 8.302f)
                verticalLineTo(15.7021f)
                curveTo(19.6003f, 16.557f, 19.5996f, 17.1441f, 19.5624f, 17.5992f)
                curveTo(19.5261f, 18.0438f, 19.4595f, 18.2826f, 19.3714f, 18.4554f)
                curveTo(19.1701f, 18.8506f, 18.8488f, 19.1718f, 18.4537f, 19.3732f)
                curveTo(18.2809f, 19.4612f, 18.042f, 19.5278f, 17.5974f, 19.5642f)
                curveTo(17.1423f, 19.6014f, 16.5553f, 19.6021f, 15.7003f, 19.6021f)
                horizontalLineTo(8.3003f)
                curveTo(7.4454f, 19.6021f, 6.8583f, 19.6014f, 6.4032f, 19.5642f)
                curveTo(5.9586f, 19.5278f, 5.7198f, 19.4612f, 5.5469f, 19.3732f)
                curveTo(5.1518f, 19.1718f, 4.8305f, 18.8506f, 4.6292f, 18.4554f)
                curveTo(4.5411f, 18.2826f, 4.4745f, 18.0438f, 4.4382f, 17.5992f)
                curveTo(4.401f, 17.1441f, 4.4003f, 16.557f, 4.4003f, 15.7021f)
                verticalLineTo(8.302f)
                curveTo(4.4003f, 7.4471f, 4.401f, 6.8601f, 4.4382f, 6.405f)
                curveTo(4.4745f, 5.9604f, 4.5411f, 5.7215f, 4.6292f, 5.5487f)
                curveTo(4.8305f, 5.1535f, 5.1518f, 4.8323f, 5.5469f, 4.6309f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun SquarePlusPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.SquarePlus,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
