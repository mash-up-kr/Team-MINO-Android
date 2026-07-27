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

val MinoIcons.Link: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Link",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(16.0707f, 2.7665f)
                curveTo(17.4102f, 2.7738f, 18.7543f, 3.3392f, 19.7074f, 4.2923f)
                curveTo(20.6661f, 5.2511f, 21.2258f, 6.577f, 21.2331f, 7.929f)
                lineTo(21.2332f, 7.9339f)
                curveTo(21.2332f, 9.0821f, 20.837f, 10.0801f, 20.0939f, 11.0572f)
                lineTo(20.0857f, 11.068f)
                curveTo(19.7438f, 11.5037f, 19.2726f, 12.0103f, 18.5053f, 12.7776f)
                lineTo(17.7628f, 13.52f)
                curveTo(17.4114f, 13.8715f, 16.8415f, 13.8715f, 16.49f, 13.52f)
                curveTo(16.1386f, 13.1686f, 16.1386f, 12.5987f, 16.49f, 12.2472f)
                lineTo(17.2325f, 11.5048f)
                curveTo(17.988f, 10.7493f, 18.3941f, 10.307f, 18.6653f, 9.9621f)
                curveTo(19.2167f, 9.2355f, 19.4326f, 8.6127f, 19.4332f, 7.9364f)
                curveTo(19.4278f, 7.0653f, 19.062f, 6.1925f, 18.4346f, 5.5651f)
                curveTo(17.8135f, 4.944f, 16.9228f, 4.5719f, 16.0633f, 4.5665f)
                curveTo(15.3867f, 4.567f, 14.7878f, 4.7711f, 14.0425f, 5.3412f)
                curveTo(13.6633f, 5.6352f, 13.2158f, 6.0463f, 12.4949f, 6.7671f)
                lineTo(11.7523f, 7.5096f)
                curveTo(11.4008f, 7.8611f, 10.8309f, 7.861f, 10.4795f, 7.5095f)
                curveTo(10.1281f, 7.158f, 10.1281f, 6.5881f, 10.4796f, 6.2367f)
                lineTo(11.2221f, 5.4944f)
                curveTo(11.9545f, 4.762f, 12.4684f, 4.2837f, 12.9422f, 3.9167f)
                lineTo(12.9463f, 3.9135f)
                curveTo(13.9421f, 3.1512f, 14.9168f, 2.7665f, 16.0658f, 2.7665f)
                lineTo(16.0707f, 2.7665f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(15.5001f, 8.4995f)
                curveTo(15.8516f, 8.851f, 15.8516f, 9.4209f, 15.5001f, 9.7723f)
                lineTo(9.7726f, 15.5f)
                curveTo(9.4212f, 15.8515f, 8.8513f, 15.8515f, 8.4998f, 15.5001f)
                curveTo(8.1484f, 15.1486f, 8.1484f, 14.5787f, 8.4998f, 14.2273f)
                lineTo(14.2273f, 8.4996f)
                curveTo(14.5788f, 8.1481f, 15.1486f, 8.1481f, 15.5001f, 8.4995f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(7.5098f, 10.4794f)
                curveTo(7.8613f, 10.8308f, 7.8613f, 11.4007f, 7.5099f, 11.7522f)
                lineTo(6.7675f, 12.4948f)
                curveTo(6.0466f, 13.2156f, 5.6355f, 13.6632f, 5.3415f, 14.0424f)
                curveTo(4.7714f, 14.7877f, 4.5673f, 15.3866f, 4.5668f, 16.0631f)
                curveTo(4.5721f, 16.9226f, 4.9442f, 17.8134f, 5.5654f, 18.4345f)
                curveTo(6.1928f, 19.062f, 7.0656f, 19.4277f, 7.9367f, 19.4331f)
                curveTo(8.613f, 19.4326f, 9.2357f, 19.2166f, 9.9624f, 18.6652f)
                curveTo(10.3072f, 18.394f, 10.7495f, 17.9879f, 11.505f, 17.2325f)
                lineTo(12.2475f, 16.49f)
                curveTo(12.599f, 16.1385f, 13.1688f, 16.1385f, 13.5203f, 16.49f)
                curveTo(13.8718f, 16.8414f, 13.8718f, 17.4113f, 13.5203f, 17.7628f)
                lineTo(12.7778f, 18.5052f)
                curveTo(12.0105f, 19.2725f, 11.5039f, 19.7436f, 11.0682f, 20.0855f)
                lineTo(11.0575f, 20.0939f)
                curveTo(10.0804f, 20.837f, 9.0823f, 21.2331f, 7.9341f, 21.2331f)
                lineTo(7.9293f, 21.2331f)
                curveTo(6.5773f, 21.2258f, 5.2513f, 20.6661f, 4.2926f, 19.7073f)
                curveTo(3.3395f, 18.7542f, 2.7741f, 17.4102f, 2.7668f, 16.0706f)
                lineTo(2.7668f, 16.0657f)
                curveTo(2.7668f, 14.9168f, 3.1515f, 13.942f, 3.9138f, 12.9462f)
                lineTo(3.9169f, 12.9421f)
                curveTo(4.284f, 12.4683f, 4.7622f, 11.9545f, 5.4946f, 11.2221f)
                lineTo(6.237f, 10.4796f)
                curveTo(6.5884f, 10.128f, 7.1582f, 10.128f, 7.5098f, 10.4794f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun LinkPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Link,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
