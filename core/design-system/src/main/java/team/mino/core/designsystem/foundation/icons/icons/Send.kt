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

val MinoIcons.Send: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Send",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(9.4992f, 3.9494f)
                curveTo(8.7316f, 3.5344f, 8.1019f, 3.194f, 7.5967f, 2.9761f)
                curveTo(7.1105f, 2.7664f, 6.5446f, 2.5775f, 5.9763f, 2.6997f)
                curveTo(5.2246f, 2.8614f, 4.5959f, 3.3735f, 4.2854f, 4.0769f)
                curveTo(4.0506f, 4.6088f, 4.1211f, 5.2012f, 4.2281f, 5.7198f)
                curveTo(4.3392f, 6.2585f, 4.5451f, 6.9441f, 4.7961f, 7.7799f)
                lineTo(6.0634f, 11.9999f)
                lineTo(4.7961f, 16.2198f)
                curveTo(4.5451f, 17.0556f, 4.3392f, 17.7412f, 4.2281f, 18.28f)
                curveTo(4.1211f, 18.7986f, 4.0506f, 19.391f, 4.2854f, 19.9228f)
                curveTo(4.5959f, 20.6262f, 5.2246f, 21.1383f, 5.9763f, 21.3f)
                curveTo(6.5446f, 21.4223f, 7.1105f, 21.2333f, 7.5967f, 21.0236f)
                curveTo(8.1018f, 20.8057f, 8.7316f, 20.4653f, 9.4992f, 20.0504f)
                lineTo(20.359f, 14.1802f)
                curveTo(20.7525f, 13.9675f, 21.1009f, 13.7792f, 21.3647f, 13.6037f)
                curveTo(21.6294f, 13.4277f, 21.9463f, 13.1792f, 22.1239f, 12.7921f)
                curveTo(22.3546f, 12.2891f, 22.3546f, 11.7106f, 22.1239f, 11.2076f)
                curveTo(21.9463f, 10.8205f, 21.6294f, 10.572f, 21.3647f, 10.396f)
                curveTo(21.1009f, 10.2205f, 20.7525f, 10.0322f, 20.359f, 9.8195f)
                lineTo(9.4992f, 3.9494f)
                close()
                moveTo(5.9063f, 5.2186f)
                curveTo(5.7524f, 4.7061f, 6.2956f, 4.2637f, 6.7663f, 4.5182f)
                lineTo(20.4445f, 11.9118f)
                curveTo(20.469f, 11.9251f, 20.4787f, 11.9378f, 20.4842f, 11.9475f)
                curveTo(20.4913f, 11.9599f, 20.497f, 11.978f, 20.497f, 11.9998f)
                curveTo(20.497f, 12.0217f, 20.4913f, 12.0397f, 20.4842f, 12.0521f)
                curveTo(20.4787f, 12.0618f, 20.469f, 12.0745f, 20.4445f, 12.0878f)
                lineTo(6.7663f, 19.4814f)
                curveTo(6.2956f, 19.7359f, 5.7524f, 19.2935f, 5.9063f, 18.781f)
                lineTo(7.6726f, 12.8999f)
                horizontalLineTo(13f)
                curveTo(13.497f, 12.8999f, 13.9f, 12.4969f, 13.9f, 11.9999f)
                curveTo(13.9f, 11.5028f, 13.497f, 11.0999f, 13f, 11.0999f)
                horizontalLineTo(7.6726f)
                lineTo(5.9063f, 5.2186f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun SendPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Send,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
