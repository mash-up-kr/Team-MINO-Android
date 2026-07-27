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

val MinoIcons.MailOpen: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.MailOpen",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(12.6757f, 2.5828f)
                curveTo(12.2315f, 2.4763f, 11.7684f, 2.4763f, 11.3241f, 2.5828f)
                curveTo(10.8172f, 2.7043f, 10.3672f, 3.005f, 9.8322f, 3.3626f)
                lineTo(3.8402f, 7.3573f)
                curveTo(3.4034f, 7.6479f, 3.0353f, 7.8928f, 2.7596f, 8.2282f)
                curveTo(2.5175f, 8.5227f, 2.3359f, 8.8621f, 2.2251f, 9.2269f)
                curveTo(2.099f, 9.6423f, 2.0994f, 10.0845f, 2.0999f, 10.6091f)
                lineTo(2.1f, 16.5846f)
                curveTo(2.1f, 17.1145f, 2.1f, 17.5657f, 2.1302f, 17.9361f)
                curveTo(2.162f, 18.3253f, 2.2316f, 18.7039f, 2.4161f, 19.0658f)
                curveTo(2.6941f, 19.6115f, 3.1377f, 20.0551f, 3.6834f, 20.3331f)
                curveTo(4.0453f, 20.5176f, 4.4239f, 20.5872f, 4.8131f, 20.619f)
                curveTo(5.1835f, 20.6493f, 5.6347f, 20.6492f, 6.1645f, 20.6492f)
                horizontalLineTo(17.8353f)
                curveTo(18.3652f, 20.6492f, 18.8164f, 20.6493f, 19.1868f, 20.619f)
                curveTo(19.576f, 20.5872f, 19.9545f, 20.5176f, 20.3165f, 20.3331f)
                curveTo(20.8621f, 20.0551f, 21.3058f, 19.6115f, 21.5838f, 19.0658f)
                curveTo(21.7682f, 18.7039f, 21.8379f, 18.3253f, 21.8697f, 17.9361f)
                curveTo(21.8999f, 17.5657f, 21.8999f, 17.1145f, 21.8999f, 16.5847f)
                lineTo(21.9f, 10.6091f)
                curveTo(21.9005f, 10.0845f, 21.9009f, 9.6423f, 21.7748f, 9.2269f)
                curveTo(21.664f, 8.8621f, 21.4824f, 8.5227f, 21.2403f, 8.2282f)
                curveTo(20.9646f, 7.8928f, 20.5965f, 7.6479f, 20.1597f, 7.3573f)
                lineTo(14.1677f, 3.3626f)
                curveTo(13.6327f, 3.005f, 13.1827f, 2.7043f, 12.6757f, 2.5828f)
                close()
                moveTo(11.6199f, 4.3705f)
                curveTo(11.8652f, 4.2801f, 12.1347f, 4.2801f, 12.38f, 4.3705f)
                curveTo(12.478f, 4.4066f, 12.5885f, 4.473f, 13.0539f, 4.7833f)
                lineTo(19.3787f, 9.0001f)
                lineTo(13.2805f, 13.0679f)
                curveTo(12.5803f, 13.5349f, 12.4144f, 13.6296f, 12.2612f, 13.6663f)
                curveTo(12.0927f, 13.7068f, 11.917f, 13.7068f, 11.7485f, 13.6665f)
                curveTo(11.5953f, 13.6298f, 11.4294f, 13.5352f, 10.7289f, 13.0685f)
                lineTo(4.622f, 8.9996f)
                lineTo(10.946f, 4.7833f)
                curveTo(11.4114f, 4.473f, 11.5219f, 4.4066f, 11.6199f, 4.3705f)
                close()
                moveTo(3.9f, 10.6816f)
                verticalLineTo(16.949f)
                curveTo(3.9f, 17.7636f, 3.9111f, 17.9574f, 3.9538f, 18.089f)
                curveTo(4.0626f, 18.4238f, 4.3252f, 18.6864f, 4.6601f, 18.7952f)
                curveTo(4.7916f, 18.8379f, 4.9854f, 18.849f, 5.8f, 18.849f)
                horizontalLineTo(18.1999f)
                curveTo(19.0145f, 18.849f, 19.2083f, 18.8379f, 19.3398f, 18.7952f)
                curveTo(19.6747f, 18.6864f, 19.9373f, 18.4238f, 20.0461f, 18.089f)
                curveTo(20.0888f, 17.9574f, 20.0999f, 17.7636f, 20.0999f, 16.949f)
                verticalLineTo(10.6829f)
                lineTo(14.1729f, 14.6366f)
                curveTo(13.638f, 14.9944f, 13.188f, 15.2953f, 12.6811f, 15.4169f)
                curveTo(12.2369f, 15.5234f, 11.7738f, 15.5236f, 11.3295f, 15.4172f)
                curveTo(10.8226f, 15.2959f, 10.3725f, 14.9952f, 9.8373f, 14.6377f)
                lineTo(3.9f, 10.6816f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun MailOpenPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.MailOpen,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
