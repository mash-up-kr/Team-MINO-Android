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

val MinoIcons.DocumentTextFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.DocumentTextFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(12.6503f, 2.1001f)
                lineTo(12.6503f, 7.4288f)
                curveTo(12.6503f, 7.6841f, 12.6503f, 7.9243f, 12.6668f, 8.1261f)
                curveTo(12.6847f, 8.3454f, 12.7262f, 8.5932f, 12.8519f, 8.84f)
                curveTo(13.0293f, 9.1881f, 13.3123f, 9.4711f, 13.6604f, 9.6485f)
                curveTo(13.9072f, 9.7742f, 14.155f, 9.8157f, 14.3743f, 9.8336f)
                curveTo(14.5762f, 9.8501f, 14.8163f, 9.8501f, 15.0717f, 9.8501f)
                lineTo(20.1499f, 9.8501f)
                verticalLineTo(17.8357f)
                curveTo(20.1499f, 18.3655f, 20.1499f, 18.8167f, 20.1197f, 19.1871f)
                curveTo(20.0879f, 19.5763f, 20.0182f, 19.9548f, 19.8338f, 20.3168f)
                curveTo(19.5558f, 20.8624f, 19.1121f, 21.3061f, 18.5665f, 21.5841f)
                curveTo(18.2045f, 21.7685f, 17.826f, 21.8382f, 17.4368f, 21.87f)
                curveTo(17.0664f, 21.9002f, 16.6152f, 21.9002f, 16.0854f, 21.9002f)
                horizontalLineTo(7.9145f)
                curveTo(7.3847f, 21.9002f, 6.9335f, 21.9002f, 6.5631f, 21.87f)
                curveTo(6.1739f, 21.8382f, 5.7953f, 21.7685f, 5.4334f, 21.5841f)
                curveTo(4.8877f, 21.3061f, 4.4441f, 20.8624f, 4.166f, 20.3168f)
                curveTo(3.9816f, 19.9548f, 3.912f, 19.5763f, 3.8802f, 19.1871f)
                curveTo(3.8499f, 18.8167f, 3.85f, 18.3655f, 3.85f, 17.8356f)
                verticalLineTo(6.1648f)
                curveTo(3.85f, 5.6349f, 3.8499f, 5.1837f, 3.8802f, 4.8133f)
                curveTo(3.912f, 4.4242f, 3.9816f, 4.0456f, 4.166f, 3.6836f)
                curveTo(4.4441f, 3.138f, 4.8877f, 2.6943f, 5.4334f, 2.4163f)
                curveTo(5.7953f, 2.2319f, 6.1739f, 2.1622f, 6.5631f, 2.1304f)
                curveTo(6.9335f, 2.1002f, 7.3847f, 2.1002f, 7.9146f, 2.1002f)
                lineTo(12.5872f, 2.1002f)
                lineTo(12.6503f, 2.1001f)
                close()
                moveTo(7.1004f, 13.998f)
                curveTo(7.1004f, 13.5009f, 7.5034f, 13.098f, 8.0004f, 13.098f)
                horizontalLineTo(12.0004f)
                curveTo(12.4974f, 13.098f, 12.9004f, 13.5009f, 12.9004f, 13.998f)
                curveTo(12.9004f, 14.495f, 12.4974f, 14.898f, 12.0004f, 14.898f)
                horizontalLineTo(8.0004f)
                curveTo(7.5034f, 14.898f, 7.1004f, 14.495f, 7.1004f, 13.998f)
                close()
                moveTo(7.1004f, 17.7494f)
                curveTo(7.1004f, 17.2524f, 7.5034f, 16.8494f, 8.0004f, 16.8494f)
                horizontalLineTo(12.0004f)
                curveTo(12.4974f, 16.8494f, 12.9004f, 17.2524f, 12.9004f, 17.7494f)
                curveTo(12.9004f, 18.2465f, 12.4974f, 18.6494f, 12.0004f, 18.6494f)
                horizontalLineTo(8.0004f)
                curveTo(7.5034f, 18.6494f, 7.1004f, 18.2465f, 7.1004f, 17.7494f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(14.3503f, 2.8278f)
                verticalLineTo(8.0005f)
                curveTo(14.3503f, 8.0833f, 14.4175f, 8.1505f, 14.5003f, 8.1505f)
                horizontalLineTo(19.6653f)
                lineTo(14.3503f, 2.8278f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun DocumentTextFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.DocumentTextFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
