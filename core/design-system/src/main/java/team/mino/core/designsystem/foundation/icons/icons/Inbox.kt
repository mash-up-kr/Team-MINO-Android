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

val MinoIcons.Inbox: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Inbox",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(8.111f, 4.1002f)
                curveTo(7.5863f, 4.0996f, 7.1441f, 4.0992f, 6.7287f, 4.2254f)
                curveTo(6.3639f, 4.3361f, 6.0245f, 4.5178f, 5.73f, 4.7599f)
                curveTo(5.3946f, 5.0356f, 5.1497f, 5.4037f, 4.8591f, 5.8406f)
                lineTo(2.2573f, 9.7436f)
                curveTo(2.0888f, 9.996f, 1.9466f, 10.2092f, 1.8432f, 10.4461f)
                curveTo(1.7521f, 10.6547f, 1.6859f, 10.8733f, 1.646f, 11.0973f)
                curveTo(1.6006f, 11.3517f, 1.6008f, 11.608f, 1.601f, 11.9115f)
                lineTo(1.601f, 15.8356f)
                curveTo(1.601f, 16.3655f, 1.601f, 16.8167f, 1.6312f, 17.1871f)
                curveTo(1.663f, 17.5763f, 1.7327f, 17.9549f, 1.9171f, 18.3168f)
                curveTo(2.1951f, 18.8625f, 2.6387f, 19.3061f, 3.1844f, 19.5841f)
                curveTo(3.5464f, 19.7685f, 3.9249f, 19.8382f, 4.3141f, 19.87f)
                curveTo(4.6845f, 19.9002f, 5.1357f, 19.9002f, 5.6656f, 19.9002f)
                horizontalLineTo(18.3364f)
                curveTo(18.8663f, 19.9002f, 19.3174f, 19.9002f, 19.6879f, 19.87f)
                curveTo(20.077f, 19.8382f, 20.4556f, 19.7685f, 20.8176f, 19.5841f)
                curveTo(21.3632f, 19.3061f, 21.8069f, 18.8625f, 22.0849f, 18.3168f)
                curveTo(22.2693f, 17.9549f, 22.339f, 17.5763f, 22.3708f, 17.1871f)
                curveTo(22.401f, 16.8167f, 22.401f, 16.3655f, 22.401f, 15.8357f)
                lineTo(22.401f, 11.9115f)
                curveTo(22.4012f, 11.6081f, 22.4013f, 11.3517f, 22.356f, 11.0973f)
                curveTo(22.3161f, 10.8733f, 22.2499f, 10.6547f, 22.1588f, 10.4461f)
                curveTo(22.0554f, 10.2092f, 21.9131f, 9.996f, 21.7447f, 9.7436f)
                lineTo(19.1429f, 5.8406f)
                curveTo(18.8523f, 5.4037f, 18.6074f, 5.0356f, 18.272f, 4.7599f)
                curveTo(17.9775f, 4.5178f, 17.6381f, 4.3361f, 17.2733f, 4.2254f)
                curveTo(16.8579f, 4.0992f, 16.4157f, 4.0996f, 15.891f, 4.1002f)
                horizontalLineTo(8.111f)
                close()
                moveTo(7.3566f, 5.9213f)
                curveTo(7.4424f, 5.9042f, 7.5458f, 5.9001f, 7.9996f, 5.9001f)
                horizontalLineTo(16.0024f)
                curveTo(16.4562f, 5.9001f, 16.5596f, 5.9042f, 16.6454f, 5.9213f)
                curveTo(16.859f, 5.9638f, 17.0552f, 6.0688f, 17.2091f, 6.223f)
                curveTo(17.2709f, 6.2849f, 17.3317f, 6.3687f, 17.5834f, 6.7463f)
                lineTo(19.8194f, 10.1006f)
                horizontalLineTo(16.2503f)
                curveTo(15.02f, 10.1006f, 14.2522f, 11.1041f, 13.9215f, 11.8504f)
                curveTo(13.5943f, 12.5887f, 12.8561f, 13.1008f, 12.0003f, 13.1008f)
                curveTo(11.1446f, 13.1008f, 10.4063f, 12.5887f, 10.079f, 11.8503f)
                curveTo(9.7483f, 11.1041f, 8.9805f, 10.1006f, 7.7503f, 10.1006f)
                horizontalLineTo(4.1826f)
                lineTo(6.4187f, 6.7463f)
                curveTo(6.6703f, 6.3687f, 6.7311f, 6.2849f, 6.7929f, 6.223f)
                curveTo(6.9468f, 6.0688f, 7.143f, 5.9638f, 7.3566f, 5.9213f)
                close()
                moveTo(3.401f, 11.9006f)
                verticalLineTo(16.2001f)
                curveTo(3.401f, 17.0147f, 3.4121f, 17.2085f, 3.4548f, 17.3401f)
                curveTo(3.5636f, 17.6749f, 3.8262f, 17.9375f, 4.1611f, 18.0463f)
                curveTo(4.2926f, 18.089f, 4.4864f, 18.1001f, 5.301f, 18.1001f)
                horizontalLineTo(18.701f)
                curveTo(19.5156f, 18.1001f, 19.7094f, 18.089f, 19.8409f, 18.0463f)
                curveTo(20.1758f, 17.9375f, 20.4384f, 17.6749f, 20.5472f, 17.3401f)
                curveTo(20.5899f, 17.2085f, 20.601f, 17.0147f, 20.601f, 16.2001f)
                verticalLineTo(11.9006f)
                horizontalLineTo(16.2503f)
                curveTo(15.9229f, 11.9006f, 15.6793f, 12.3266f, 15.5671f, 12.5797f)
                curveTo(14.962f, 13.9452f, 13.5938f, 14.9008f, 12.0003f, 14.9008f)
                curveTo(10.4069f, 14.9008f, 9.0386f, 13.9452f, 8.4334f, 12.5797f)
                curveTo(8.3212f, 12.3266f, 8.0777f, 11.9006f, 7.7503f, 11.9006f)
                horizontalLineTo(3.401f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun InboxPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Inbox,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
