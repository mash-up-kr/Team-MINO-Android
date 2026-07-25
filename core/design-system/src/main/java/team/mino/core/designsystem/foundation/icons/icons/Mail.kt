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

val MinoIcons.Mail: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Mail",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(6.1646f, 4.349f)
                curveTo(5.6347f, 4.349f, 5.1835f, 4.349f, 4.8131f, 4.3793f)
                curveTo(4.4239f, 4.4111f, 4.0453f, 4.4807f, 3.6834f, 4.6651f)
                curveTo(3.1377f, 4.9431f, 2.6941f, 5.3868f, 2.4161f, 5.9325f)
                curveTo(2.2317f, 6.2944f, 2.162f, 6.673f, 2.1302f, 7.0621f)
                curveTo(2.1f, 7.4325f, 2.1f, 7.8837f, 2.1f, 8.4135f)
                verticalLineTo(15.5844f)
                curveTo(2.1f, 16.1143f, 2.1f, 16.5655f, 2.1302f, 16.9359f)
                curveTo(2.162f, 17.3251f, 2.2317f, 17.7037f, 2.4161f, 18.0656f)
                curveTo(2.6941f, 18.6113f, 3.1377f, 19.0549f, 3.6834f, 19.3329f)
                curveTo(4.0453f, 19.5174f, 4.4239f, 19.587f, 4.8131f, 19.6188f)
                curveTo(5.1835f, 19.6491f, 5.6347f, 19.649f, 6.1646f, 19.649f)
                horizontalLineTo(17.8353f)
                curveTo(18.3652f, 19.649f, 18.8164f, 19.6491f, 19.1868f, 19.6188f)
                curveTo(19.576f, 19.587f, 19.9546f, 19.5174f, 20.3165f, 19.3329f)
                curveTo(20.8622f, 19.0549f, 21.3058f, 18.6113f, 21.5838f, 18.0656f)
                curveTo(21.7682f, 17.7037f, 21.8379f, 17.3251f, 21.8697f, 16.9359f)
                curveTo(21.8999f, 16.5655f, 21.8999f, 16.1143f, 21.8999f, 15.5845f)
                verticalLineTo(8.4136f)
                curveTo(21.8999f, 7.8838f, 21.8999f, 7.4326f, 21.8697f, 7.0621f)
                curveTo(21.8379f, 6.673f, 21.7682f, 6.2944f, 21.5838f, 5.9325f)
                curveTo(21.3058f, 5.3868f, 20.8622f, 4.9431f, 20.3165f, 4.6651f)
                curveTo(19.9546f, 4.4807f, 19.576f, 4.4111f, 19.1868f, 4.3793f)
                curveTo(18.8164f, 4.349f, 18.3652f, 4.349f, 17.8354f, 4.349f)
                horizontalLineTo(6.1646f)
                close()
                moveTo(4.6601f, 6.2042f)
                curveTo(4.7916f, 6.1614f, 4.9854f, 6.1503f, 5.8f, 6.1503f)
                horizontalLineTo(18.1999f)
                curveTo(19.0145f, 6.1503f, 19.2083f, 6.1614f, 19.3398f, 6.2042f)
                curveTo(19.6747f, 6.313f, 19.9373f, 6.5755f, 20.0461f, 6.9104f)
                curveTo(20.0774f, 7.0069f, 20.0917f, 7.1368f, 20.0972f, 7.5204f)
                lineTo(13.2803f, 12.0675f)
                curveTo(12.5801f, 12.5346f, 12.4142f, 12.6293f, 12.261f, 12.666f)
                curveTo(12.0925f, 12.7065f, 11.9169f, 12.7065f, 11.7483f, 12.6662f)
                curveTo(11.5951f, 12.6295f, 11.4292f, 12.5349f, 10.7287f, 12.0682f)
                lineTo(3.9028f, 7.5201f)
                curveTo(3.9082f, 7.1367f, 3.9225f, 7.0068f, 3.9538f, 6.9104f)
                curveTo(4.0626f, 6.5755f, 4.3252f, 6.313f, 4.6601f, 6.2042f)
                close()
                moveTo(3.9f, 9.6814f)
                verticalLineTo(15.9503f)
                curveTo(3.9f, 16.7649f, 3.9111f, 16.9587f, 3.9538f, 17.0902f)
                curveTo(4.0626f, 17.4251f, 4.3252f, 17.6877f, 4.6601f, 17.7965f)
                curveTo(4.7916f, 17.8392f, 4.9854f, 17.8503f, 5.8f, 17.8503f)
                horizontalLineTo(18.1999f)
                curveTo(19.0145f, 17.8503f, 19.2083f, 17.8392f, 19.3398f, 17.7965f)
                curveTo(19.6747f, 17.6877f, 19.9373f, 17.4251f, 20.0461f, 17.0902f)
                curveTo(20.0888f, 16.9587f, 20.0999f, 16.7649f, 20.0999f, 15.9503f)
                verticalLineTo(9.6824f)
                lineTo(14.1727f, 13.6363f)
                curveTo(13.6378f, 13.994f, 13.1878f, 14.2949f, 12.6809f, 14.4166f)
                curveTo(12.2367f, 14.5231f, 11.7736f, 14.5232f, 11.3293f, 14.4169f)
                curveTo(10.8224f, 14.2956f, 10.3723f, 13.9949f, 9.8371f, 13.6374f)
                lineTo(3.9f, 9.6814f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun MailPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Mail,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
