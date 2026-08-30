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

val MinoIcons.SquarePlusFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.SquarePlusFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(8.2625f, 2.6029f)
                horizontalLineTo(15.7379f)
                curveTo(16.546f, 2.6029f, 17.2068f, 2.6029f, 17.7439f, 2.6468f)
                curveTo(18.2998f, 2.6922f, 18.8019f, 2.7891f, 19.2708f, 3.028f)
                curveTo(20.0046f, 3.4019f, 20.6012f, 3.9985f, 20.9751f, 4.7324f)
                curveTo(21.2141f, 5.2013f, 21.3109f, 5.7033f, 21.3564f, 6.2592f)
                curveTo(21.4002f, 6.7963f, 21.4002f, 7.4572f, 21.4002f, 8.2652f)
                verticalLineTo(15.7406f)
                curveTo(21.4002f, 16.5487f, 21.4002f, 17.2095f, 21.3564f, 17.7466f)
                curveTo(21.3109f, 18.3025f, 21.2141f, 18.8046f, 20.9751f, 19.2735f)
                curveTo(20.6012f, 20.0073f, 20.0046f, 20.604f, 19.2708f, 20.9779f)
                curveTo(18.8019f, 21.2168f, 18.2998f, 21.3136f, 17.7439f, 21.3591f)
                curveTo(17.2068f, 21.4029f, 16.546f, 21.4029f, 15.7379f, 21.4029f)
                horizontalLineTo(8.2625f)
                curveTo(7.4545f, 21.4029f, 6.7936f, 21.4029f, 6.2565f, 21.3591f)
                curveTo(5.7006f, 21.3136f, 5.1986f, 21.2168f, 4.7297f, 20.9779f)
                curveTo(3.9958f, 20.604f, 3.3992f, 20.0073f, 3.0253f, 19.2735f)
                curveTo(2.7864f, 18.8046f, 2.6895f, 18.3025f, 2.6441f, 17.7466f)
                curveTo(2.6002f, 17.2095f, 2.6002f, 16.5487f, 2.6002f, 15.7407f)
                verticalLineTo(8.2652f)
                curveTo(2.6002f, 7.4572f, 2.6002f, 6.7963f, 2.6441f, 6.2592f)
                curveTo(2.6895f, 5.7033f, 2.7864f, 5.2013f, 3.0253f, 4.7324f)
                curveTo(3.3992f, 3.9985f, 3.9958f, 3.4019f, 4.7297f, 3.028f)
                curveTo(5.1986f, 2.7891f, 5.7006f, 2.6922f, 6.2565f, 2.6468f)
                curveTo(6.7936f, 2.6029f, 7.4545f, 2.6029f, 8.2625f, 2.6029f)
                close()
                moveTo(12.8999f, 8.0004f)
                curveTo(12.8999f, 7.5033f, 12.497f, 7.1004f, 11.9999f, 7.1004f)
                curveTo(11.5029f, 7.1004f, 11.0999f, 7.5033f, 11.0999f, 8.0004f)
                verticalLineTo(11.1004f)
                horizontalLineTo(7.9999f)
                curveTo(7.5029f, 11.1004f, 7.0999f, 11.5033f, 7.0999f, 12.0004f)
                curveTo(7.0999f, 12.4975f, 7.5029f, 12.9004f, 7.9999f, 12.9004f)
                horizontalLineTo(11.0999f)
                verticalLineTo(16.0004f)
                curveTo(11.0999f, 16.4975f, 11.5029f, 16.9004f, 11.9999f, 16.9004f)
                curveTo(12.497f, 16.9004f, 12.8999f, 16.4975f, 12.8999f, 16.0004f)
                verticalLineTo(12.9004f)
                horizontalLineTo(15.9999f)
                curveTo(16.497f, 12.9004f, 16.8999f, 12.4975f, 16.8999f, 12.0004f)
                curveTo(16.8999f, 11.5033f, 16.497f, 11.1004f, 15.9999f, 11.1004f)
                horizontalLineTo(12.8999f)
                verticalLineTo(8.0004f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun SquarePlusFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.SquarePlusFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
