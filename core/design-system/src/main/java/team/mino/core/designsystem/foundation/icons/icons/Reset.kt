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

val MinoIcons.Reset: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Reset",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(9.4557f, 6.7713f)
                lineTo(8.1372f, 5.4529f)
                curveTo(11.0518f, 3.7294f, 14.8711f, 4.1211f, 17.3755f, 6.6255f)
                curveTo(20.3435f, 9.5935f, 20.3435f, 14.4056f, 17.3755f, 17.3735f)
                curveTo(14.4075f, 20.3415f, 9.5955f, 20.3415f, 6.6275f, 17.3735f)
                curveTo(5.1431f, 15.8892f, 4.4013f, 13.9454f, 4.4015f, 11.9987f)
                curveTo(4.4016f, 11.5016f, 3.9987f, 11.0986f, 3.5016f, 11.0986f)
                curveTo(3.0046f, 11.0985f, 2.6016f, 11.5014f, 2.6015f, 11.9985f)
                curveTo(2.6012f, 14.4031f, 3.5191f, 16.8108f, 5.3547f, 18.6463f)
                curveTo(9.0256f, 22.3173f, 14.9774f, 22.3173f, 18.6483f, 18.6463f)
                curveTo(22.3192f, 14.9754f, 22.3192f, 9.0236f, 18.6483f, 5.3527f)
                curveTo(15.4363f, 2.1408f, 10.4801f, 1.7398f, 6.8314f, 4.147f)
                lineTo(5.5666f, 2.8822f)
                curveTo(5.3092f, 2.6248f, 4.9221f, 2.5478f, 4.5858f, 2.6871f)
                curveTo(4.2495f, 2.8264f, 4.0302f, 3.1546f, 4.0302f, 3.5186f)
                lineTo(4.0302f, 7.4077f)
                curveTo(4.0302f, 7.6464f, 4.125f, 7.8753f, 4.2938f, 8.0441f)
                curveTo(4.4626f, 8.2129f, 4.6915f, 8.3077f, 4.9302f, 8.3077f)
                horizontalLineTo(8.8193f)
                curveTo(9.1833f, 8.3077f, 9.5114f, 8.0884f, 9.6508f, 7.7521f)
                curveTo(9.7901f, 7.4158f, 9.7131f, 7.0287f, 9.4557f, 6.7713f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ResetPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Reset,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
