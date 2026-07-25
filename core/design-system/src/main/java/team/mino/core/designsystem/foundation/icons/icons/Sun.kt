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

val MinoIcons.Sun: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Sun",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.1002f, 22f)
                curveTo(11.1002f, 22.497f, 11.5031f, 22.9f, 12.0002f, 22.9f)
                curveTo(12.4972f, 22.9f, 12.9002f, 22.497f, 12.9002f, 22f)
                verticalLineTo(20f)
                curveTo(12.9002f, 19.5029f, 12.4972f, 19.1f, 12.0002f, 19.1f)
                curveTo(11.5031f, 19.1f, 11.1002f, 19.5029f, 11.1002f, 20f)
                verticalLineTo(22f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.1002f, 4f)
                curveTo(11.1002f, 4.497f, 11.5031f, 4.9f, 12.0002f, 4.9f)
                curveTo(12.4972f, 4.9f, 12.9002f, 4.497f, 12.9002f, 4f)
                verticalLineTo(2f)
                curveTo(12.9002f, 1.5029f, 12.4972f, 1.1f, 12.0002f, 1.1f)
                curveTo(11.5031f, 1.1f, 11.1002f, 1.5029f, 11.1002f, 2f)
                verticalLineTo(4f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(6.6001f, 11.9971f)
                curveTo(6.6001f, 9.0148f, 9.0177f, 6.5971f, 12.0001f, 6.5971f)
                curveTo(14.9824f, 6.5971f, 17.4001f, 9.0148f, 17.4001f, 11.9971f)
                curveTo(17.4001f, 14.9795f, 14.9824f, 17.3971f, 12.0001f, 17.3971f)
                curveTo(9.0177f, 17.3971f, 6.6001f, 14.9795f, 6.6001f, 11.9971f)
                close()
                moveTo(12.0001f, 8.3971f)
                curveTo(10.0118f, 8.3971f, 8.4001f, 10.0089f, 8.4001f, 11.9971f)
                curveTo(8.4001f, 13.9854f, 10.0118f, 15.5971f, 12.0001f, 15.5971f)
                curveTo(13.9883f, 15.5971f, 15.6001f, 13.9854f, 15.6001f, 11.9971f)
                curveTo(15.6001f, 10.0089f, 13.9883f, 8.3971f, 12.0001f, 8.3971f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(1.1002f, 11.9998f)
                curveTo(1.1002f, 11.5028f, 1.5031f, 11.0998f, 2.0002f, 11.0998f)
                horizontalLineTo(4.0002f)
                curveTo(4.4972f, 11.0998f, 4.9002f, 11.5028f, 4.9002f, 11.9998f)
                curveTo(4.9002f, 12.4969f, 4.4972f, 12.8998f, 4.0002f, 12.8998f)
                horizontalLineTo(2.0002f)
                curveTo(1.5031f, 12.8998f, 1.1002f, 12.4969f, 1.1002f, 11.9998f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(19.1002f, 11.9998f)
                curveTo(19.1002f, 11.5028f, 19.5031f, 11.0998f, 20.0002f, 11.0998f)
                horizontalLineTo(22.0002f)
                curveTo(22.4972f, 11.0998f, 22.9002f, 11.5028f, 22.9002f, 11.9998f)
                curveTo(22.9002f, 12.4969f, 22.4972f, 12.8998f, 22.0002f, 12.8998f)
                horizontalLineTo(20.0002f)
                curveTo(19.5031f, 12.8998f, 19.1002f, 12.4969f, 19.1002f, 11.9998f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(4.2927f, 19.7073f)
                curveTo(3.9413f, 19.3559f, 3.9413f, 18.786f, 4.2927f, 18.4345f)
                lineTo(5.7069f, 17.0203f)
                curveTo(6.0584f, 16.6689f, 6.6283f, 16.6689f, 6.9797f, 17.0203f)
                curveTo(7.3312f, 17.3718f, 7.3312f, 17.9417f, 6.9797f, 18.2931f)
                lineTo(5.5655f, 19.7073f)
                curveTo(5.214f, 20.0588f, 4.6442f, 20.0588f, 4.2927f, 19.7073f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(17.0206f, 6.9794f)
                curveTo(16.6692f, 6.628f, 16.6692f, 6.0581f, 17.0206f, 5.7066f)
                lineTo(18.4349f, 4.2924f)
                curveTo(18.7863f, 3.9409f, 19.3562f, 3.9409f, 19.7076f, 4.2924f)
                curveTo(20.0591f, 4.6439f, 20.0591f, 5.2137f, 19.7076f, 5.5652f)
                lineTo(18.2934f, 6.9794f)
                curveTo(17.942f, 7.3309f, 17.3721f, 7.3309f, 17.0206f, 6.9794f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(18.4347f, 19.7073f)
                curveTo(18.7862f, 20.0588f, 19.3561f, 20.0588f, 19.7075f, 19.7073f)
                curveTo(20.059f, 19.3559f, 20.059f, 18.786f, 19.7075f, 18.4345f)
                lineTo(18.2933f, 17.0203f)
                curveTo(17.9418f, 16.6689f, 17.372f, 16.6689f, 17.0205f, 17.0203f)
                curveTo(16.669f, 17.3718f, 16.669f, 17.9417f, 17.0205f, 18.2931f)
                lineTo(18.4347f, 19.7073f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(5.7068f, 6.9794f)
                curveTo(6.0583f, 7.3309f, 6.6281f, 7.3309f, 6.9796f, 6.9794f)
                curveTo(7.3311f, 6.628f, 7.3311f, 6.0581f, 6.9796f, 5.7066f)
                lineTo(5.5654f, 4.2924f)
                curveTo(5.2139f, 3.9409f, 4.6441f, 3.9409f, 4.2926f, 4.2924f)
                curveTo(3.9411f, 4.6439f, 3.9411f, 5.2137f, 4.2926f, 5.5652f)
                lineTo(5.7068f, 6.9794f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun SunPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Sun,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
