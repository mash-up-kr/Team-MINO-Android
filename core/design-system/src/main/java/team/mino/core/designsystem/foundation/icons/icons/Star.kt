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

val MinoIcons.Star: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Star",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(11.1382f, 1.4082f)
                curveTo(11.6796f, 1.1327f, 12.32f, 1.1327f, 12.8614f, 1.4082f)
                curveTo(13.2958f, 1.6292f, 13.5453f, 2.0394f, 13.7116f, 2.3569f)
                curveTo(13.8873f, 2.6921f, 14.0739f, 3.1408f, 14.2919f, 3.6651f)
                lineTo(15.4972f, 6.563f)
                curveTo(15.5695f, 6.7369f, 15.6087f, 6.8299f, 15.6414f, 6.8967f)
                curveTo(15.6513f, 6.9168f, 15.6581f, 6.9292f, 15.662f, 6.936f)
                curveTo(15.668f, 6.9417f, 15.6747f, 6.9465f, 15.6819f, 6.9505f)
                curveTo(15.6896f, 6.9521f, 15.7036f, 6.9548f, 15.7258f, 6.9579f)
                curveTo(15.7994f, 6.9684f, 15.8999f, 6.9769f, 16.0876f, 6.992f)
                lineTo(19.2161f, 7.2428f)
                curveTo(19.7821f, 7.2881f, 20.2666f, 7.3269f, 20.6397f, 7.3904f)
                curveTo(20.993f, 7.4505f, 21.4602f, 7.561f, 21.8047f, 7.9059f)
                curveTo(22.2339f, 8.3356f, 22.4318f, 8.9448f, 22.3372f, 9.5447f)
                curveTo(22.2612f, 10.0262f, 21.9482f, 10.3902f, 21.6977f, 10.6466f)
                curveTo(21.4331f, 10.9172f, 21.064f, 11.2334f, 20.6327f, 11.6028f)
                lineTo(18.2492f, 13.6445f)
                curveTo(18.1062f, 13.767f, 18.0298f, 13.833f, 17.9764f, 13.8848f)
                curveTo(17.9603f, 13.9004f, 17.9506f, 13.9107f, 17.9453f, 13.9165f)
                curveTo(17.9418f, 13.924f, 17.9393f, 13.9319f, 17.9377f, 13.94f)
                curveTo(17.9385f, 13.9478f, 17.9403f, 13.9618f, 17.9442f, 13.9839f)
                curveTo(17.9569f, 14.0572f, 17.98f, 14.1554f, 18.0237f, 14.3386f)
                lineTo(18.7519f, 17.3915f)
                curveTo(18.8837f, 17.9438f, 18.9964f, 18.4165f, 19.0514f, 18.791f)
                curveTo(19.1034f, 19.1456f, 19.1427f, 19.6241f, 18.9211f, 20.0583f)
                curveTo(18.645f, 20.5993f, 18.1269f, 20.9757f, 17.527f, 21.0711f)
                curveTo(17.0456f, 21.1477f, 16.6027f, 20.9625f, 16.2815f, 20.8034f)
                curveTo(15.9424f, 20.6355f, 15.5277f, 20.3821f, 15.0432f, 20.0862f)
                lineTo(12.3647f, 18.4501f)
                curveTo(12.204f, 18.352f, 12.1176f, 18.2997f, 12.0519f, 18.2649f)
                curveTo(12.0321f, 18.2544f, 12.0193f, 18.2484f, 12.0121f, 18.2452f)
                curveTo(12.0039f, 18.2442f, 11.9956f, 18.2442f, 11.9875f, 18.2452f)
                curveTo(11.9803f, 18.2484f, 11.9675f, 18.2544f, 11.9476f, 18.2649f)
                curveTo(11.8819f, 18.2997f, 11.7956f, 18.352f, 11.6349f, 18.4501f)
                lineTo(8.9565f, 20.0861f)
                curveTo(8.4719f, 20.3821f, 8.0572f, 20.6354f, 7.718f, 20.8034f)
                curveTo(7.3968f, 20.9625f, 6.9539f, 21.1477f, 6.4725f, 21.0711f)
                curveTo(5.8726f, 20.9757f, 5.3545f, 20.5993f, 5.0784f, 20.0583f)
                curveTo(4.8569f, 19.6241f, 4.8961f, 19.1456f, 4.9482f, 18.791f)
                curveTo(5.0031f, 18.4165f, 5.1159f, 17.9437f, 5.2477f, 17.3914f)
                lineTo(5.9759f, 14.3386f)
                curveTo(6.0196f, 14.1554f, 6.0426f, 14.0572f, 6.0554f, 13.9839f)
                curveTo(6.0592f, 13.9618f, 6.061f, 13.9478f, 6.0618f, 13.94f)
                curveTo(6.0603f, 13.9319f, 6.0577f, 13.924f, 6.0542f, 13.9165f)
                curveTo(6.049f, 13.9107f, 6.0392f, 13.9004f, 6.0232f, 13.8848f)
                curveTo(5.9698f, 13.833f, 5.8934f, 13.767f, 5.7504f, 13.6445f)
                lineTo(3.3668f, 11.6028f)
                curveTo(2.9356f, 11.2334f, 2.5664f, 10.9172f, 2.3019f, 10.6466f)
                curveTo(2.0513f, 10.3902f, 1.7384f, 10.0262f, 1.6624f, 9.5447f)
                curveTo(1.5677f, 8.9448f, 1.7656f, 8.3356f, 2.1949f, 7.9059f)
                curveTo(2.5394f, 7.561f, 3.0065f, 7.4505f, 3.3599f, 7.3904f)
                curveTo(3.733f, 7.3269f, 4.2175f, 7.2881f, 4.7835f, 7.2428f)
                lineTo(7.9119f, 6.992f)
                curveTo(8.0996f, 6.9769f, 8.2002f, 6.9684f, 8.2738f, 6.9579f)
                curveTo(8.296f, 6.9548f, 8.3099f, 6.9521f, 8.3176f, 6.9505f)
                curveTo(8.3248f, 6.9465f, 8.3315f, 6.9417f, 8.3375f, 6.936f)
                curveTo(8.3415f, 6.9292f, 8.3483f, 6.9168f, 8.3581f, 6.8967f)
                curveTo(8.3909f, 6.8299f, 8.43f, 6.7369f, 8.5023f, 6.563f)
                lineTo(9.7076f, 3.6652f)
                curveTo(9.9256f, 3.1409f, 10.1123f, 2.6921f, 10.2879f, 2.3569f)
                curveTo(10.4543f, 2.0394f, 10.7037f, 1.6292f, 11.1382f, 1.4082f)
                close()
                moveTo(11.9418f, 3.018f)
                curveTo(11.9342f, 3.0228f, 11.9208f, 3.0326f, 11.9074f, 3.0648f)
                lineTo(10.0235f, 7.5942f)
                curveTo(9.7499f, 8.2521f, 9.1312f, 8.7015f, 8.421f, 8.7585f)
                lineTo(3.5311f, 9.1505f)
                curveTo(3.4964f, 9.1533f, 3.4829f, 9.163f, 3.476f, 9.1688f)
                curveTo(3.4653f, 9.1777f, 3.4521f, 9.1944f, 3.444f, 9.2193f)
                curveTo(3.436f, 9.2441f, 3.4368f, 9.2654f, 3.4402f, 9.2789f)
                curveTo(3.4424f, 9.2876f, 3.4476f, 9.3035f, 3.4741f, 9.3261f)
                lineTo(7.1997f, 12.5175f)
                curveTo(7.7408f, 12.981f, 7.9771f, 13.7083f, 7.8118f, 14.4013f)
                lineTo(6.6735f, 19.173f)
                curveTo(6.6654f, 19.2069f, 6.6706f, 19.2228f, 6.6739f, 19.2311f)
                curveTo(6.679f, 19.2441f, 6.6909f, 19.2618f, 6.712f, 19.2772f)
                curveTo(6.7332f, 19.2925f, 6.7537f, 19.2983f, 6.7676f, 19.2992f)
                curveTo(6.7766f, 19.2998f, 6.7932f, 19.2997f, 6.8229f, 19.2816f)
                lineTo(11.0094f, 16.7245f)
                curveTo(11.6174f, 16.3531f, 12.3821f, 16.3531f, 12.9901f, 16.7245f)
                lineTo(17.1766f, 19.2816f)
                curveTo(17.2063f, 19.2997f, 17.2229f, 19.2998f, 17.2319f, 19.2992f)
                curveTo(17.2459f, 19.2983f, 17.2663f, 19.2925f, 17.2875f, 19.2772f)
                curveTo(17.3086f, 19.2618f, 17.3205f, 19.2441f, 17.3256f, 19.2311f)
                curveTo(17.3289f, 19.2228f, 17.334f, 19.2069f, 17.326f, 19.173f)
                lineTo(16.1877f, 14.4013f)
                curveTo(16.0224f, 13.7083f, 16.2587f, 12.981f, 16.7998f, 12.5175f)
                lineTo(20.5254f, 9.3261f)
                curveTo(20.5519f, 9.3035f, 20.5571f, 9.2876f, 20.5593f, 9.2789f)
                curveTo(20.5627f, 9.2654f, 20.5635f, 9.2441f, 20.5555f, 9.2193f)
                curveTo(20.5474f, 9.1944f, 20.5342f, 9.1777f, 20.5235f, 9.1688f)
                curveTo(20.5166f, 9.163f, 20.5031f, 9.1533f, 20.4683f, 9.1505f)
                lineTo(15.5784f, 8.7585f)
                curveTo(14.8682f, 8.7015f, 14.2496f, 8.2521f, 13.976f, 7.5942f)
                lineTo(12.0921f, 3.0648f)
                curveTo(12.0787f, 3.0326f, 12.0653f, 3.0228f, 12.0577f, 3.018f)
                curveTo(12.0459f, 3.0105f, 12.0259f, 3.0032f, 11.9997f, 3.0032f)
                curveTo(11.9736f, 3.0032f, 11.9536f, 3.0105f, 11.9418f, 3.018f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun StarPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Star,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
