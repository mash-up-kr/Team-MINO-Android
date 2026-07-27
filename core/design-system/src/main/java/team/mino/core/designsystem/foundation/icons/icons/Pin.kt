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

val MinoIcons.Pin: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Pin",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(8.9474f, 2.6019f)
                curveTo(8.5312f, 2.6019f, 8.1587f, 2.6019f, 7.8595f, 2.6294f)
                curveTo(7.5551f, 2.6573f, 7.179f, 2.7228f, 6.8486f, 2.9611f)
                curveTo(6.4143f, 3.2744f, 6.1324f, 3.7565f, 6.0723f, 4.2887f)
                curveTo(6.0266f, 4.6935f, 6.1539f, 5.0533f, 6.2789f, 5.3324f)
                curveTo(6.4017f, 5.6066f, 6.5843f, 5.9311f, 6.7885f, 6.2939f)
                lineTo(7.6002f, 7.7368f)
                verticalLineTo(10.6853f)
                lineTo(5.8514f, 12.8713f)
                curveTo(5.5405f, 13.2598f, 5.2693f, 13.5988f, 5.0801f, 13.8851f)
                curveTo(4.8948f, 14.1653f, 4.6813f, 14.5453f, 4.6808f, 14.9989f)
                curveTo(4.6802f, 15.5775f, 4.9433f, 16.1249f, 5.3955f, 16.4859f)
                curveTo(5.75f, 16.7689f, 6.1802f, 16.8396f, 6.5147f, 16.87f)
                curveTo(6.8564f, 16.901f, 7.2905f, 16.901f, 7.7882f, 16.901f)
                lineTo(11.0998f, 16.901f)
                verticalLineTo(22.001f)
                curveTo(11.0998f, 22.498f, 11.5028f, 22.901f, 11.9998f, 22.901f)
                curveTo(12.4969f, 22.901f, 12.8998f, 22.498f, 12.8998f, 22.001f)
                verticalLineTo(16.901f)
                lineTo(16.2122f, 16.901f)
                curveTo(16.7099f, 16.901f, 17.144f, 16.901f, 17.4857f, 16.87f)
                curveTo(17.8202f, 16.8396f, 18.2504f, 16.7689f, 18.6048f, 16.4859f)
                curveTo(19.0571f, 16.1249f, 19.3201f, 15.5776f, 19.3195f, 14.9989f)
                curveTo(19.319f, 14.5454f, 19.1056f, 14.1653f, 18.9203f, 13.8851f)
                curveTo(18.7311f, 13.5989f, 18.4599f, 13.26f, 18.149f, 12.8713f)
                lineTo(16.4002f, 10.6853f)
                verticalLineTo(7.7368f)
                lineTo(17.212f, 6.294f)
                curveTo(17.4162f, 5.9312f, 17.5988f, 5.6066f, 17.7216f, 5.3324f)
                curveTo(17.8466f, 5.0533f, 17.9739f, 4.6935f, 17.9282f, 4.2887f)
                curveTo(17.868f, 3.7565f, 17.5861f, 3.2744f, 17.1518f, 2.9611f)
                curveTo(16.8214f, 2.7228f, 16.4454f, 2.6573f, 16.1409f, 2.6294f)
                curveTo(15.8418f, 2.6019f, 15.4694f, 2.6019f, 15.0532f, 2.6019f)
                horizontalLineTo(8.9474f)
                close()
                moveTo(7.8731f, 4.5508f)
                curveTo(7.8604f, 4.5282f, 7.8586f, 4.5126f, 7.8589f, 4.501f)
                curveTo(7.8592f, 4.4868f, 7.8636f, 4.4691f, 7.874f, 4.4513f)
                curveTo(7.8844f, 4.4335f, 7.8977f, 4.421f, 7.9099f, 4.4137f)
                curveTo(7.9198f, 4.4078f, 7.9344f, 4.4018f, 7.9603f, 4.4018f)
                horizontalLineTo(16.0402f)
                curveTo(16.0661f, 4.4018f, 16.0806f, 4.4078f, 16.0906f, 4.4137f)
                curveTo(16.1028f, 4.421f, 16.1161f, 4.4335f, 16.1265f, 4.4513f)
                curveTo(16.1369f, 4.4691f, 16.1412f, 4.4868f, 16.1416f, 4.501f)
                curveTo(16.1419f, 4.5126f, 16.14f, 4.5282f, 16.1273f, 4.5508f)
                lineTo(14.7158f, 7.0595f)
                curveTo(14.64f, 7.1942f, 14.6002f, 7.3462f, 14.6002f, 7.5008f)
                verticalLineTo(11.0008f)
                curveTo(14.6002f, 11.2051f, 14.6698f, 11.4034f, 14.7974f, 11.563f)
                lineTo(17.4976f, 14.9383f)
                curveTo(17.5169f, 14.9624f, 17.5201f, 14.9785f, 17.5211f, 14.9887f)
                curveTo(17.5224f, 15.0029f, 17.5199f, 15.0227f, 17.5097f, 15.0441f)
                curveTo(17.4994f, 15.0655f, 17.4854f, 15.0798f, 17.4735f, 15.0876f)
                curveTo(17.465f, 15.0933f, 17.4504f, 15.1008f, 17.4195f, 15.1008f)
                horizontalLineTo(6.5808f)
                curveTo(6.5499f, 15.1008f, 6.5354f, 15.0933f, 6.5268f, 15.0876f)
                curveTo(6.5149f, 15.0798f, 6.501f, 15.0655f, 6.4907f, 15.0441f)
                curveTo(6.4804f, 15.0227f, 6.478f, 15.0029f, 6.4793f, 14.9887f)
                curveTo(6.4802f, 14.9785f, 6.4834f, 14.9624f, 6.5027f, 14.9383f)
                lineTo(9.203f, 11.563f)
                curveTo(9.3307f, 11.4034f, 9.4002f, 11.2052f, 9.4002f, 11.0008f)
                verticalLineTo(7.5008f)
                curveTo(9.4002f, 7.3462f, 9.3604f, 7.1942f, 9.2846f, 7.0595f)
                lineTo(7.8731f, 4.5508f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun PinPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Pin,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
