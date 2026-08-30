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

val MinoIcons.CoffeeFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.CoffeeFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(5.8675f, 3.0951f)
                horizontalLineTo(16.1342f)
                curveTo(16.5256f, 3.0951f, 16.8722f, 3.0951f, 17.1593f, 3.1185f)
                curveTo(17.4651f, 3.1435f, 17.782f, 3.1995f, 18.0904f, 3.3567f)
                curveTo(18.542f, 3.5868f, 18.9092f, 3.954f, 19.1392f, 4.4055f)
                curveTo(19.2563f, 4.6352f, 19.3172f, 4.8695f, 19.351f, 5.1004f)
                horizontalLineTo(19.7499f)
                curveTo(22.0419f, 5.1004f, 23.8999f, 6.9584f, 23.8999f, 9.2504f)
                curveTo(23.8999f, 11.5424f, 22.0419f, 13.4004f, 19.7499f, 13.4004f)
                horizontalLineTo(18.4399f)
                curveTo(17.0343f, 16.0731f, 14.2307f, 17.8961f, 11.0012f, 17.8961f)
                curveTo(6.3619f, 17.8961f, 2.6008f, 14.1342f, 2.6008f, 9.4951f)
                verticalLineTo(6.3618f)
                curveTo(2.6008f, 5.9703f, 2.6008f, 5.6238f, 2.6243f, 5.3366f)
                curveTo(2.6492f, 5.0309f, 2.7052f, 4.714f, 2.8624f, 4.4055f)
                curveTo(3.0925f, 3.954f, 3.4597f, 3.5868f, 3.9113f, 3.3567f)
                curveTo(4.2197f, 3.1995f, 4.5366f, 3.1435f, 4.8423f, 3.1185f)
                curveTo(5.1295f, 3.0951f, 5.476f, 3.0951f, 5.8675f, 3.0951f)
                close()
                moveTo(19.7499f, 6.9004f)
                horizontalLineTo(19.4008f)
                verticalLineTo(9.4951f)
                curveTo(19.4008f, 10.2221f, 19.3085f, 10.9276f, 19.1349f, 11.6004f)
                horizontalLineTo(19.7499f)
                curveTo(21.0477f, 11.6004f, 22.0999f, 10.5483f, 22.0999f, 9.2504f)
                curveTo(22.0999f, 7.9525f, 21.0477f, 6.9004f, 19.7499f, 6.9004f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(4.751f, 20.0994f)
                curveTo(4.2539f, 20.0994f, 3.851f, 20.5024f, 3.851f, 20.9994f)
                curveTo(3.851f, 21.4965f, 4.2539f, 21.8994f, 4.751f, 21.8994f)
                horizontalLineTo(17.751f)
                curveTo(18.248f, 21.8994f, 18.651f, 21.4965f, 18.651f, 20.9994f)
                curveTo(18.651f, 20.5024f, 18.248f, 20.0994f, 17.751f, 20.0994f)
                horizontalLineTo(4.751f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun CoffeeFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.CoffeeFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
