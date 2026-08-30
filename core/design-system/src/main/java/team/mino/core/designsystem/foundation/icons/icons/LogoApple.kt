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

val MinoIcons.LogoApple: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.LogoApple",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(8.9555f, 21.9477f)
                curveTo(10.2363f, 21.9477f, 10.798f, 21.0602f, 12.3933f, 21.0602f)
                curveTo(13.9886f, 21.0602f, 14.3706f, 21.914f, 15.7861f, 21.914f)
                curveTo(17.2017f, 21.914f, 18.1117f, 20.5883f, 18.9992f, 19.2851f)
                curveTo(19.9878f, 17.7909f, 20.3923f, 16.3304f, 20.426f, 16.263f)
                curveTo(20.3361f, 16.2406f, 17.6623f, 15.1059f, 17.6623f, 11.949f)
                curveTo(17.6623f, 9.2077f, 19.7744f, 7.9719f, 19.8867f, 7.882f)
                curveTo(18.4937f, 5.8149f, 16.3703f, 5.7699f, 15.7974f, 5.7699f)
                curveTo(14.2358f, 5.7699f, 12.955f, 6.7474f, 12.1461f, 6.7474f)
                curveTo(11.2811f, 6.7474f, 10.1352f, 5.8261f, 8.7758f, 5.8261f)
                curveTo(6.1918f, 5.8261f, 3.5742f, 8.0281f, 3.5742f, 12.1737f)
                curveTo(3.5742f, 14.7464f, 4.5516f, 17.4764f, 5.7425f, 19.2402f)
                curveTo(6.7648f, 20.7344f, 7.6636f, 21.959f, 8.9555f, 21.959f)
                verticalLineTo(21.9477f)
                close()
                moveTo(12.2473f, 5.5228f)
                curveTo(13.1797f, 5.5228f, 14.3593f, 4.8712f, 15.0559f, 4.0061f)
                curveTo(15.685f, 3.2197f, 16.1456f, 2.1187f, 16.1456f, 1.0177f)
                curveTo(16.1456f, 0.8717f, 16.1344f, 0.7144f, 16.1007f, 0.6021f)
                curveTo(15.0559f, 0.647f, 13.8089f, 1.3211f, 13.0561f, 2.2311f)
                curveTo(12.4607f, 2.9276f, 11.9215f, 4.0061f, 11.9215f, 5.1184f)
                curveTo(11.9215f, 5.2756f, 11.9439f, 5.4441f, 11.9664f, 5.5003f)
                curveTo(12.0338f, 5.5116f, 12.1349f, 5.5228f, 12.2473f, 5.5228f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun LogoApplePreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.LogoApple,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
