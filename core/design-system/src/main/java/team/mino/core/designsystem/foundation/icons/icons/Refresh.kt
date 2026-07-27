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

val MinoIcons.Refresh: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Refresh",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(2.6003f, 11.998f)
                curveTo(2.6003f, 6.8065f, 6.8088f, 2.598f, 12.0003f, 2.598f)
                curveTo(13.8175f, 2.598f, 15.5149f, 3.1142f, 16.953f, 4.0073f)
                lineTo(17.9024f, 3.0579f)
                curveTo(18.1598f, 2.8005f, 18.5469f, 2.7235f, 18.8832f, 2.8628f)
                curveTo(19.2195f, 3.0021f, 19.4387f, 3.3302f, 19.4387f, 3.6943f)
                verticalLineTo(6.8762f)
                curveTo(19.4387f, 7.1149f, 19.3439f, 7.3439f, 19.1751f, 7.5126f)
                curveTo(19.0064f, 7.6814f, 18.7774f, 7.7762f, 18.5387f, 7.7762f)
                horizontalLineTo(15.3568f)
                curveTo(14.9928f, 7.7762f, 14.6646f, 7.557f, 14.5253f, 7.2207f)
                curveTo(14.386f, 6.8844f, 14.463f, 6.4973f, 14.7204f, 6.2398f)
                lineTo(15.6373f, 5.3229f)
                curveTo(14.5571f, 4.733f, 13.3181f, 4.398f, 12.0003f, 4.398f)
                curveTo(7.803f, 4.398f, 4.4003f, 7.8007f, 4.4003f, 11.998f)
                curveTo(4.4003f, 12.5075f, 4.4503f, 13.0045f, 4.5454f, 13.4845f)
                curveTo(4.6421f, 13.972f, 4.3251f, 14.4456f, 3.8375f, 14.5422f)
                curveTo(3.35f, 14.6388f, 2.8764f, 14.3219f, 2.7798f, 13.8343f)
                curveTo(2.662f, 13.2397f, 2.6003f, 12.6256f, 2.6003f, 11.998f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(20.1632f, 9.4572f)
                curveTo(20.6508f, 9.3606f, 21.1243f, 9.6775f, 21.2209f, 10.1651f)
                curveTo(21.3387f, 10.7596f, 21.4003f, 11.3735f, 21.4003f, 12.0009f)
                curveTo(21.4003f, 17.1924f, 17.1918f, 21.4009f, 12.0003f, 21.4009f)
                curveTo(10.1837f, 21.4009f, 8.4867f, 20.885f, 7.0489f, 19.9925f)
                lineTo(6.0958f, 20.9456f)
                curveTo(5.8384f, 21.203f, 5.4513f, 21.28f, 5.115f, 21.1407f)
                curveTo(4.7787f, 21.0014f, 4.5594f, 20.6732f, 4.5594f, 20.3092f)
                lineTo(4.5594f, 17.1272f)
                curveTo(4.5594f, 16.8885f, 4.6542f, 16.6596f, 4.823f, 16.4908f)
                curveTo(4.9918f, 16.322f, 5.2207f, 16.2272f, 5.4594f, 16.2272f)
                horizontalLineTo(8.6414f)
                curveTo(9.0054f, 16.2272f, 9.3336f, 16.4465f, 9.4728f, 16.7828f)
                curveTo(9.6121f, 17.1191f, 9.5351f, 17.5062f, 9.2777f, 17.7636f)
                lineTo(8.3646f, 18.6768f)
                curveTo(9.4446f, 19.2662f, 10.683f, 19.6009f, 12.0003f, 19.6009f)
                curveTo(16.1977f, 19.6009f, 19.6003f, 16.1983f, 19.6003f, 12.0009f)
                curveTo(19.6003f, 11.4916f, 19.5503f, 10.9948f, 19.4552f, 10.5149f)
                curveTo(19.3587f, 10.0273f, 19.6756f, 9.5537f, 20.1632f, 9.4572f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun RefreshPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Refresh,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
