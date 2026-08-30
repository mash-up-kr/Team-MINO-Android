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

val MinoIcons.Phone: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Phone",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(17.0084f, 20.7959f)
                curveTo(14.8244f, 20.3535f, 11.4908f, 19.1308f, 8.1818f, 15.8218f)
                curveTo(4.8729f, 12.5129f, 3.6501f, 9.1792f, 3.2077f, 6.9952f)
                curveTo(2.8405f, 5.1823f, 3.6621f, 3.5124f, 4.8262f, 2.3483f)
                lineTo(5.0982f, 2.0763f)
                curveTo(6.3654f, 0.8092f, 8.467f, 0.9831f, 9.5086f, 2.4413f)
                lineTo(10.9333f, 4.4359f)
                curveTo(11.6152f, 5.3906f, 11.507f, 6.6984f, 10.6774f, 7.5279f)
                lineTo(9.8483f, 8.357f)
                curveTo(10.1002f, 8.9631f, 10.7445f, 10.182f, 12.283f, 11.7206f)
                curveTo(13.8216f, 13.2592f, 15.0406f, 13.9035f, 15.6466f, 14.1553f)
                lineTo(16.4757f, 13.3262f)
                curveTo(17.3053f, 12.4966f, 18.613f, 12.3884f, 19.5677f, 13.0703f)
                lineTo(21.5623f, 14.495f)
                curveTo(23.0205f, 15.5366f, 23.1944f, 17.6383f, 21.9273f, 18.9055f)
                lineTo(21.6553f, 19.1775f)
                curveTo(20.4912f, 20.3415f, 18.8213f, 21.1632f, 17.0084f, 20.7959f)
                close()
                moveTo(9.4546f, 14.549f)
                curveTo(12.4769f, 17.5713f, 15.4744f, 18.6486f, 17.3658f, 19.0318f)
                curveTo(18.4018f, 19.2416f, 19.4912f, 18.7959f, 20.3825f, 17.9047f)
                lineTo(20.6545f, 17.6327f)
                curveTo(21.1352f, 17.152f, 21.0692f, 16.3548f, 20.5161f, 15.9597f)
                lineTo(18.5215f, 14.535f)
                curveTo(18.2828f, 14.3646f, 17.9559f, 14.3916f, 17.7485f, 14.599f)
                lineTo(16.9041f, 15.4434f)
                curveTo(16.4619f, 15.8856f, 15.7531f, 16.1356f, 15.0491f, 15.8555f)
                curveTo(14.2382f, 15.5327f, 12.779f, 14.7621f, 11.0103f, 12.9934f)
                curveTo(9.2415f, 11.2247f, 8.4709f, 9.7654f, 8.1482f, 8.9545f)
                curveTo(7.868f, 8.2506f, 8.1181f, 7.5417f, 8.5603f, 7.0995f)
                lineTo(9.4046f, 6.2552f)
                curveTo(9.612f, 6.0477f, 9.6391f, 5.7208f, 9.4686f, 5.4821f)
                lineTo(8.0439f, 3.4876f)
                curveTo(7.6488f, 2.9344f, 6.8516f, 2.8685f, 6.371f, 3.3491f)
                lineTo(6.099f, 3.6211f)
                curveTo(5.2077f, 4.5124f, 4.762f, 5.6018f, 4.9719f, 6.6378f)
                curveTo(5.355f, 8.5292f, 6.4323f, 11.5267f, 9.4546f, 14.549f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun PhonePreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Phone,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
