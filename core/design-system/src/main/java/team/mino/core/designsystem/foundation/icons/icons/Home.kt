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

val MinoIcons.Home: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Home",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(12.3616f, 2.2764f)
                curveTo(12.1248f, 2.2131f, 11.8755f, 2.2131f, 11.6387f, 2.2764f)
                curveTo(11.3579f, 2.3514f, 11.1251f, 2.5332f, 11.0025f, 2.629f)
                lineTo(10.9675f, 2.6562f)
                lineTo(4.1343f, 7.8814f)
                curveTo(3.7475f, 8.1767f, 3.4213f, 8.4256f, 3.1785f, 8.7499f)
                curveTo(2.9652f, 9.0349f, 2.8062f, 9.3567f, 2.7094f, 9.6992f)
                curveTo(2.5993f, 10.089f, 2.5997f, 10.4994f, 2.6001f, 10.986f)
                lineTo(2.6002f, 17.3349f)
                curveTo(2.6001f, 17.8647f, 2.6001f, 18.3159f, 2.6304f, 18.6864f)
                curveTo(2.6622f, 19.0755f, 2.7318f, 19.4541f, 2.9162f, 19.816f)
                curveTo(3.1943f, 20.3617f, 3.6379f, 20.8054f, 4.1836f, 21.0834f)
                curveTo(4.5455f, 21.2678f, 4.9241f, 21.3374f, 5.3133f, 21.3692f)
                curveTo(5.6837f, 21.3995f, 6.1348f, 21.3995f, 6.6647f, 21.3995f)
                horizontalLineTo(11.9898f)
                curveTo(11.9933f, 21.3995f, 11.9969f, 21.3995f, 12.0004f, 21.3995f)
                curveTo(12.0039f, 21.3995f, 12.0075f, 21.3995f, 12.011f, 21.3995f)
                horizontalLineTo(17.3355f)
                curveTo(17.8653f, 21.3995f, 18.3166f, 21.3995f, 18.687f, 21.3692f)
                curveTo(19.0761f, 21.3374f, 19.4547f, 21.2678f, 19.8167f, 21.0834f)
                curveTo(20.3623f, 20.8054f, 20.806f, 20.3617f, 21.084f, 19.816f)
                curveTo(21.2684f, 19.4541f, 21.3381f, 19.0755f, 21.3698f, 18.6864f)
                curveTo(21.4001f, 18.3159f, 21.4001f, 17.8648f, 21.4001f, 17.3349f)
                lineTo(21.4001f, 10.986f)
                curveTo(21.4006f, 10.4994f, 21.401f, 10.089f, 21.2908f, 9.6992f)
                curveTo(21.194f, 9.3567f, 21.035f, 9.0349f, 20.8217f, 8.7499f)
                curveTo(20.579f, 8.4256f, 20.2528f, 8.1767f, 19.866f, 7.8814f)
                lineTo(13.0328f, 2.6562f)
                lineTo(12.9977f, 2.629f)
                curveTo(12.8751f, 2.5332f, 12.6423f, 2.3514f, 12.3616f, 2.2764f)
                close()
                moveTo(12.9004f, 19.5993f)
                horizontalLineTo(17.7001f)
                curveTo(18.5147f, 19.5993f, 18.7085f, 19.5882f, 18.84f, 19.5455f)
                curveTo(19.1749f, 19.4367f, 19.4375f, 19.1741f, 19.5463f, 18.8392f)
                curveTo(19.589f, 18.7077f, 19.6001f, 18.5139f, 19.6001f, 17.6993f)
                verticalLineTo(10.8829f)
                curveTo(19.6001f, 10.4627f, 19.5965f, 10.3672f, 19.5816f, 10.2868f)
                curveTo(19.5445f, 10.087f, 19.4528f, 9.9013f, 19.3166f, 9.7505f)
                curveTo(19.2618f, 9.6898f, 19.1881f, 9.6289f, 18.8543f, 9.3737f)
                lineTo(12.0001f, 4.1323f)
                lineTo(5.146f, 9.3737f)
                curveTo(4.8122f, 9.6289f, 4.7385f, 9.6898f, 4.6837f, 9.7505f)
                curveTo(4.5475f, 9.9013f, 4.4558f, 10.087f, 4.4186f, 10.2868f)
                curveTo(4.4037f, 10.3672f, 4.4002f, 10.4627f, 4.4002f, 10.8829f)
                verticalLineTo(17.6993f)
                curveTo(4.4002f, 18.5139f, 4.4113f, 18.7077f, 4.454f, 18.8392f)
                curveTo(4.5628f, 19.1741f, 4.8254f, 19.4367f, 5.1602f, 19.5455f)
                curveTo(5.2918f, 19.5882f, 5.4856f, 19.5993f, 6.3002f, 19.5993f)
                horizontalLineTo(11.1004f)
                verticalLineTo(13.9995f)
                curveTo(11.1004f, 13.5025f, 11.5034f, 13.0995f, 12.0004f, 13.0995f)
                curveTo(12.4975f, 13.0995f, 12.9004f, 13.5025f, 12.9004f, 13.9995f)
                verticalLineTo(19.5993f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun HomePreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Home,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
