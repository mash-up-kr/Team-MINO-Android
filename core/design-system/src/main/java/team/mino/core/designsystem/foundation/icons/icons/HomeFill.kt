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

val MinoIcons.HomeFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.HomeFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.6386f, 2.2764f)
                curveTo(11.8754f, 2.2131f, 12.1247f, 2.2131f, 12.3615f, 2.2764f)
                curveTo(12.6422f, 2.3514f, 12.875f, 2.5332f, 12.9976f, 2.629f)
                lineTo(13.0327f, 2.6562f)
                lineTo(19.8659f, 7.8814f)
                curveTo(20.2527f, 8.1767f, 20.5789f, 8.4256f, 20.8216f, 8.7499f)
                curveTo(21.0349f, 9.0349f, 21.1939f, 9.3567f, 21.2907f, 9.6992f)
                curveTo(21.4009f, 10.089f, 21.4005f, 10.4994f, 21.4f, 10.986f)
                lineTo(21.4f, 17.3349f)
                curveTo(21.4f, 17.8647f, 21.4f, 18.3159f, 21.3698f, 18.6864f)
                curveTo(21.338f, 19.0755f, 21.2683f, 19.4541f, 21.0839f, 19.816f)
                curveTo(20.8059f, 20.3617f, 20.3622f, 20.8054f, 19.8166f, 21.0834f)
                curveTo(19.4546f, 21.2678f, 19.076f, 21.3374f, 18.6869f, 21.3692f)
                curveTo(18.3165f, 21.3995f, 17.8653f, 21.3995f, 17.3355f, 21.3995f)
                horizontalLineTo(12.9003f)
                verticalLineTo(13.9995f)
                curveTo(12.9003f, 13.5025f, 12.4974f, 13.0995f, 12.0003f, 13.0995f)
                curveTo(11.5033f, 13.0995f, 11.1003f, 13.5025f, 11.1003f, 13.9995f)
                verticalLineTo(21.3995f)
                horizontalLineTo(6.6646f)
                curveTo(6.1348f, 21.3995f, 5.6836f, 21.3995f, 5.3132f, 21.3692f)
                curveTo(4.924f, 21.3374f, 4.5454f, 21.2678f, 4.1835f, 21.0834f)
                curveTo(3.6378f, 20.8054f, 3.1942f, 20.3617f, 2.9161f, 19.816f)
                curveTo(2.7317f, 19.4541f, 2.6621f, 19.0755f, 2.6303f, 18.6864f)
                curveTo(2.6f, 18.3159f, 2.6f, 17.8647f, 2.6001f, 17.3349f)
                lineTo(2.6f, 10.986f)
                curveTo(2.5996f, 10.4994f, 2.5992f, 10.089f, 2.7093f, 9.6992f)
                curveTo(2.8061f, 9.3567f, 2.9651f, 9.0349f, 3.1784f, 8.7499f)
                curveTo(3.4212f, 8.4256f, 3.7474f, 8.1767f, 4.1342f, 7.8814f)
                lineTo(10.9674f, 2.6562f)
                lineTo(11.0024f, 2.629f)
                curveTo(11.125f, 2.5332f, 11.3578f, 2.3514f, 11.6386f, 2.2764f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun HomeFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.HomeFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
