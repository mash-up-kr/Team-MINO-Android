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

val MinoIcons.LogoInstagram: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.LogoInstagram",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(18.3009f, 6.8477f)
                curveTo(18.3009f, 7.4918f, 17.7856f, 8.007f, 17.1416f, 8.007f)
                curveTo(16.4975f, 8.007f, 15.9823f, 7.4918f, 15.9823f, 6.8477f)
                curveTo(15.9823f, 6.2037f, 16.4975f, 5.6884f, 17.1416f, 5.6884f)
                curveTo(17.7856f, 5.6884f, 18.3009f, 6.2037f, 18.3009f, 6.8477f)
                close()
                moveTo(21.6499f, 12.0001f)
                curveTo(21.6499f, 14.6192f, 21.6392f, 14.952f, 21.5962f, 15.9825f)
                curveTo(21.5533f, 17.013f, 21.3816f, 17.7107f, 21.1454f, 18.3225f)
                curveTo(20.8985f, 18.9558f, 20.5658f, 19.4925f, 20.0291f, 20.0293f)
                curveTo(19.4924f, 20.566f, 18.9556f, 20.8987f, 18.3223f, 21.1456f)
                curveTo(17.7105f, 21.3818f, 17.002f, 21.5428f, 15.9823f, 21.5964f)
                curveTo(14.9518f, 21.6394f, 14.6191f, 21.6501f, 11.9999f, 21.6501f)
                curveTo(9.3808f, 21.6501f, 9.0481f, 21.6394f, 8.0176f, 21.5964f)
                curveTo(6.9871f, 21.5535f, 6.2894f, 21.3818f, 5.6775f, 21.1456f)
                curveTo(5.0442f, 20.8987f, 4.5075f, 20.566f, 3.9708f, 20.0293f)
                curveTo(3.4341f, 19.4925f, 3.1014f, 18.9558f, 2.8545f, 18.3225f)
                curveTo(2.6183f, 17.7107f, 2.4573f, 17.0022f, 2.4036f, 15.9825f)
                curveTo(2.3607f, 14.952f, 2.35f, 14.6192f, 2.35f, 12.0001f)
                curveTo(2.35f, 9.381f, 2.3607f, 9.0482f, 2.4036f, 8.0177f)
                curveTo(2.4466f, 6.9873f, 2.6183f, 6.2895f, 2.8545f, 5.6777f)
                curveTo(3.1014f, 5.0444f, 3.4341f, 4.5077f, 3.9708f, 3.971f)
                curveTo(4.5075f, 3.4343f, 5.0442f, 3.1015f, 5.6775f, 2.8546f)
                curveTo(6.2894f, 2.6185f, 6.9978f, 2.4574f, 8.0176f, 2.4038f)
                curveTo(9.0481f, 2.3608f, 9.3808f, 2.3501f, 11.9999f, 2.3501f)
                curveTo(14.6191f, 2.3501f, 14.9518f, 2.3608f, 15.9823f, 2.4038f)
                curveTo(17.0128f, 2.4467f, 17.7105f, 2.6185f, 18.3223f, 2.8546f)
                curveTo(18.9556f, 3.1015f, 19.4924f, 3.4343f, 20.0291f, 3.971f)
                curveTo(20.5658f, 4.5077f, 20.8985f, 5.0444f, 21.1454f, 5.6777f)
                curveTo(21.3816f, 6.2895f, 21.5426f, 6.998f, 21.5962f, 8.0177f)
                curveTo(21.6392f, 9.0482f, 21.6499f, 9.381f, 21.6499f, 12.0001f)
                close()
                moveTo(19.911f, 12.0001f)
                curveTo(19.911f, 9.4239f, 19.911f, 9.1126f, 19.8573f, 8.0929f)
                curveTo(19.8144f, 7.1483f, 19.6534f, 6.6438f, 19.5246f, 6.3003f)
                curveTo(19.3528f, 5.8494f, 19.1381f, 5.5274f, 18.8054f, 5.1947f)
                curveTo(18.4726f, 4.8619f, 18.1506f, 4.6472f, 17.6998f, 4.4755f)
                curveTo(17.3563f, 4.3467f, 16.8518f, 4.1856f, 15.9072f, 4.1427f)
                curveTo(14.8874f, 4.0998f, 14.5869f, 4.089f, 11.9999f, 4.089f)
                curveTo(9.413f, 4.089f, 9.1125f, 4.089f, 8.0927f, 4.1427f)
                curveTo(7.1481f, 4.1856f, 6.6436f, 4.3467f, 6.3001f, 4.4755f)
                curveTo(5.8493f, 4.6472f, 5.5273f, 4.8619f, 5.1945f, 5.1947f)
                curveTo(4.8617f, 5.5274f, 4.6471f, 5.8494f, 4.4753f, 6.3003f)
                curveTo(4.3465f, 6.6438f, 4.1855f, 7.1483f, 4.1426f, 8.0929f)
                curveTo(4.0996f, 9.1126f, 4.0889f, 9.4132f, 4.0889f, 12.0001f)
                curveTo(4.0889f, 14.587f, 4.0889f, 14.8876f, 4.1426f, 15.9073f)
                curveTo(4.1855f, 16.8519f, 4.3465f, 17.3565f, 4.4753f, 17.6999f)
                curveTo(4.6471f, 18.1508f, 4.8617f, 18.4728f, 5.1945f, 18.8056f)
                curveTo(5.5273f, 19.1383f, 5.8493f, 19.353f, 6.3001f, 19.5248f)
                curveTo(6.6436f, 19.6536f, 7.1481f, 19.8146f, 8.0927f, 19.8575f)
                curveTo(9.1125f, 19.9004f, 9.413f, 19.9112f, 11.9999f, 19.9112f)
                curveTo(14.5869f, 19.9112f, 14.8874f, 19.9112f, 15.9072f, 19.8575f)
                curveTo(16.8518f, 19.8146f, 17.3563f, 19.6536f, 17.6998f, 19.5248f)
                curveTo(18.1506f, 19.353f, 18.4726f, 19.1383f, 18.8054f, 18.8056f)
                curveTo(19.1381f, 18.4728f, 19.3528f, 18.1508f, 19.5246f, 17.6999f)
                curveTo(19.6534f, 17.3565f, 19.8144f, 16.8519f, 19.8573f, 15.9073f)
                curveTo(19.9002f, 14.8876f, 19.911f, 14.587f, 19.911f, 12.0001f)
                close()
                moveTo(16.9484f, 12.0001f)
                curveTo(16.9484f, 14.7373f, 14.7264f, 16.9593f, 11.9892f, 16.9593f)
                curveTo(9.252f, 16.9593f, 7.03f, 14.7373f, 7.03f, 12.0001f)
                curveTo(7.03f, 9.2629f, 9.252f, 7.0409f, 11.9892f, 7.0409f)
                curveTo(14.7264f, 7.0409f, 16.9484f, 9.2629f, 16.9484f, 12.0001f)
                close()
                moveTo(15.2094f, 12.0001f)
                curveTo(15.2094f, 10.2182f, 13.7711f, 8.7799f, 11.9892f, 8.7799f)
                curveTo(10.2073f, 8.7799f, 8.769f, 10.2182f, 8.769f, 12.0001f)
                curveTo(8.769f, 13.782f, 10.2073f, 15.2204f, 11.9892f, 15.2204f)
                curveTo(13.7711f, 15.2204f, 15.2094f, 13.782f, 15.2094f, 12.0001f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun LogoInstagramPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.LogoInstagram,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
