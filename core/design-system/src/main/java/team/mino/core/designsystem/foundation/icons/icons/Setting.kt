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

val MinoIcons.Setting: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Setting",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(8.0498f, 12.0002f)
                curveTo(8.0498f, 9.8187f, 9.8182f, 8.0502f, 11.9998f, 8.0502f)
                curveTo(14.1813f, 8.0502f, 15.9497f, 9.8187f, 15.9497f, 12.0002f)
                curveTo(15.9497f, 14.1817f, 14.1813f, 15.9502f, 11.9998f, 15.9502f)
                curveTo(9.8182f, 15.9502f, 8.0498f, 14.1817f, 8.0498f, 12.0002f)
                close()
                moveTo(11.9998f, 9.9502f)
                curveTo(10.8676f, 9.9502f, 9.9498f, 10.868f, 9.9498f, 12.0002f)
                curveTo(9.9498f, 13.1324f, 10.8676f, 14.0502f, 11.9998f, 14.0502f)
                curveTo(13.1319f, 14.0502f, 14.0498f, 13.1324f, 14.0498f, 12.0002f)
                curveTo(14.0498f, 10.868f, 13.1319f, 9.9502f, 11.9998f, 9.9502f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(10.131f, 1.971f)
                curveTo(10.7375f, 1.8586f, 11.3622f, 1.8f, 11.9998f, 1.8f)
                curveTo(12.6373f, 1.8f, 13.262f, 1.8586f, 13.8685f, 1.971f)
                curveTo(14.91f, 2.1639f, 15.3502f, 3.1134f, 15.4926f, 3.7046f)
                curveTo(15.589f, 4.1049f, 15.8415f, 4.4633f, 16.2246f, 4.6845f)
                curveTo(16.6074f, 4.9055f, 17.0436f, 4.9451f, 17.4382f, 4.8289f)
                curveTo(18.0214f, 4.6569f, 19.0614f, 4.5647f, 19.749f, 5.3674f)
                curveTo(20.5589f, 6.3128f, 21.1993f, 7.409f, 21.6221f, 8.6087f)
                curveTo(21.9736f, 9.6059f, 21.3725f, 10.4601f, 20.9322f, 10.8785f)
                curveTo(20.6337f, 11.1621f, 20.4496f, 11.5599f, 20.4496f, 12.0024f)
                curveTo(20.4496f, 12.4446f, 20.6335f, 12.8422f, 20.9317f, 13.1258f)
                curveTo(21.3718f, 13.5444f, 21.9725f, 14.3988f, 21.6205f, 15.3958f)
                curveTo(21.1964f, 16.5975f, 20.5539f, 17.6952f, 19.7415f, 18.6414f)
                curveTo(19.055f, 19.441f, 18.0185f, 19.3503f, 17.4362f, 19.1797f)
                curveTo(17.0422f, 19.0642f, 16.607f, 19.1041f, 16.2249f, 19.3247f)
                curveTo(15.8429f, 19.5452f, 15.5908f, 19.9021f, 15.4937f, 20.3009f)
                curveTo(15.3502f, 20.8907f, 14.9095f, 21.8359f, 13.8712f, 22.0285f)
                curveTo(13.2638f, 22.1412f, 12.6382f, 22.2f, 11.9998f, 22.2f)
                curveTo(11.3615f, 22.2f, 10.7361f, 22.1413f, 10.1289f, 22.0286f)
                curveTo(9.0902f, 21.8359f, 8.6495f, 20.8902f, 8.5061f, 20.3003f)
                curveTo(8.4091f, 19.9013f, 8.157f, 19.5443f, 7.7748f, 19.3237f)
                curveTo(7.3927f, 19.103f, 6.9573f, 19.0632f, 6.5633f, 19.1788f)
                curveTo(5.9808f, 19.3496f, 4.9439f, 19.4405f, 4.2573f, 18.6405f)
                curveTo(3.4457f, 17.6951f, 2.8038f, 16.5985f, 2.3798f, 15.398f)
                curveTo(2.0277f, 14.4011f, 2.628f, 13.5466f, 3.068f, 13.1279f)
                curveTo(3.366f, 12.8444f, 3.5497f, 12.4469f, 3.5497f, 12.0048f)
                curveTo(3.5497f, 11.5623f, 3.3655f, 11.1643f, 3.0669f, 10.8807f)
                curveTo(2.6266f, 10.4624f, 2.0253f, 9.6085f, 2.3765f, 8.6113f)
                curveTo(2.7991f, 7.4112f, 3.4394f, 6.3147f, 4.2492f, 5.3689f)
                curveTo(4.937f, 4.5657f, 5.9774f, 4.6582f, 6.5608f, 4.8303f)
                curveTo(6.9554f, 4.9466f, 7.3917f, 4.9071f, 7.7747f, 4.686f)
                curveTo(8.158f, 4.4647f, 8.4105f, 4.1061f, 8.5068f, 3.7056f)
                curveTo(8.649f, 3.1141f, 9.0891f, 2.164f, 10.131f, 1.971f)
                close()
                moveTo(10.4986f, 3.8352f)
                curveTo(10.4943f, 3.8397f, 10.4882f, 3.8465f, 10.4804f, 3.8565f)
                curveTo(10.4386f, 3.9102f, 10.3876f, 4.0106f, 10.3542f, 4.1497f)
                curveTo(10.1412f, 5.0356f, 9.5776f, 5.839f, 8.7247f, 6.3315f)
                curveTo(7.8726f, 6.8234f, 6.8963f, 6.9101f, 6.0233f, 6.6526f)
                curveTo(5.8863f, 6.6122f, 5.774f, 6.6062f, 5.7066f, 6.6156f)
                curveTo(5.6937f, 6.6174f, 5.6846f, 6.6194f, 5.6785f, 6.621f)
                curveTo(5.0298f, 7.3824f, 4.5168f, 8.2618f, 4.1759f, 9.2217f)
                curveTo(4.1776f, 9.2277f, 4.1804f, 9.2363f, 4.1852f, 9.248f)
                curveTo(4.2107f, 9.3109f, 4.272f, 9.4048f, 4.3754f, 9.5031f)
                curveTo(5.036f, 10.1305f, 5.4497f, 11.0201f, 5.4497f, 12.0048f)
                curveTo(5.4497f, 12.9883f, 5.037f, 13.877f, 4.3778f, 14.5043f)
                curveTo(4.2745f, 14.6026f, 4.2133f, 14.6967f, 4.1878f, 14.7596f)
                curveTo(4.1831f, 14.7712f, 4.1803f, 14.7799f, 4.1786f, 14.7858f)
                curveTo(4.5207f, 15.7461f, 5.035f, 16.6255f, 5.6851f, 17.3868f)
                curveTo(5.6911f, 17.3883f, 5.7002f, 17.3903f, 5.713f, 17.3921f)
                curveTo(5.7802f, 17.4015f, 5.8919f, 17.3956f, 6.0285f, 17.3556f)
                curveTo(6.9003f, 17.0999f, 7.8745f, 17.1873f, 8.7248f, 17.6782f)
                curveTo(9.5751f, 18.1691f, 10.1378f, 18.969f, 10.3523f, 19.8516f)
                curveTo(10.386f, 19.9901f, 10.4369f, 20.0899f, 10.4787f, 20.1433f)
                curveTo(10.4864f, 20.1533f, 10.4925f, 20.16f, 10.4969f, 20.1644f)
                curveTo(10.9836f, 20.2534f, 11.4858f, 20.3f, 11.9998f, 20.3f)
                curveTo(12.5139f, 20.3f, 13.0162f, 20.2534f, 13.5031f, 20.1644f)
                curveTo(13.5074f, 20.1599f, 13.5135f, 20.1532f, 13.5213f, 20.1433f)
                curveTo(13.563f, 20.0899f, 13.6139f, 19.9902f, 13.6476f, 19.8517f)
                curveTo(13.8623f, 18.9694f, 14.425f, 18.1699f, 15.2749f, 17.6792f)
                curveTo(16.125f, 17.1884f, 17.099f, 17.1009f, 17.9705f, 17.3564f)
                curveTo(18.1071f, 17.3964f, 18.2188f, 17.4022f, 18.2859f, 17.3928f)
                curveTo(18.2987f, 17.391f, 18.3078f, 17.3891f, 18.3138f, 17.3875f)
                curveTo(18.9646f, 16.6256f, 19.4794f, 15.7453f, 19.8216f, 14.7841f)
                curveTo(19.8199f, 14.7781f, 19.8171f, 14.7694f, 19.8123f, 14.7578f)
                curveTo(19.7868f, 14.6949f, 19.7256f, 14.6008f, 19.6222f, 14.5025f)
                curveTo(18.9627f, 13.8752f, 18.5496f, 12.9862f, 18.5496f, 12.0024f)
                curveTo(18.5496f, 11.0179f, 18.9632f, 10.1285f, 19.6234f, 9.5011f)
                curveTo(19.7269f, 9.4028f, 19.7881f, 9.3088f, 19.8136f, 9.2459f)
                curveTo(19.8184f, 9.2343f, 19.8212f, 9.2256f, 19.8229f, 9.2196f)
                curveTo(19.4818f, 8.26f, 18.9687f, 7.3809f, 18.32f, 6.6197f)
                curveTo(18.3139f, 6.6181f, 18.3048f, 6.6162f, 18.2919f, 6.6144f)
                curveTo(18.2245f, 6.605f, 18.1123f, 6.611f, 17.9754f, 6.6513f)
                curveTo(17.1025f, 6.9086f, 16.1264f, 6.8218f, 15.2746f, 6.33f)
                curveTo(14.4221f, 5.8378f, 13.8586f, 5.0349f, 13.6454f, 4.1494f)
                curveTo(13.6119f, 4.0104f, 13.561f, 3.9101f, 13.5191f, 3.8565f)
                curveTo(13.5113f, 3.8465f, 13.5053f, 3.8397f, 13.5009f, 3.8352f)
                curveTo(13.0147f, 3.7465f, 12.5131f, 3.7f, 11.9998f, 3.7f)
                curveTo(11.4865f, 3.7f, 10.9848f, 3.7465f, 10.4986f, 3.8352f)
                close()
                moveTo(19.8252f, 9.2089f)
                lineTo(19.8249f, 9.211f)
                close()
                moveTo(19.8238f, 14.7948f)
                lineTo(19.8236f, 14.7927f)
                close()
                moveTo(4.1764f, 14.7966f)
                lineTo(4.1766f, 14.7945f)
                close()
                moveTo(4.1737f, 9.211f)
                lineTo(4.1739f, 9.2131f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun SettingPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Setting,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
