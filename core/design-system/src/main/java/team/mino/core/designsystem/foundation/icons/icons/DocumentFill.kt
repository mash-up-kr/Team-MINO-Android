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

val MinoIcons.DocumentFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.DocumentFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(12.6503f, 2.1001f)
                curveTo(12.646f, 2.1001f, 12.6417f, 2.1001f, 12.6374f, 2.1001f)
                lineTo(12.5872f, 2.1002f)
                lineTo(7.9146f, 2.1002f)
                curveTo(7.3847f, 2.1002f, 6.9335f, 2.1002f, 6.5631f, 2.1304f)
                curveTo(6.1739f, 2.1622f, 5.7953f, 2.2319f, 5.4334f, 2.4163f)
                curveTo(4.8877f, 2.6943f, 4.4441f, 3.138f, 4.1661f, 3.6836f)
                curveTo(3.9816f, 4.0456f, 3.912f, 4.4242f, 3.8802f, 4.8133f)
                curveTo(3.8499f, 5.1837f, 3.85f, 5.6349f, 3.85f, 6.1648f)
                verticalLineTo(17.8356f)
                curveTo(3.85f, 18.3655f, 3.8499f, 18.8167f, 3.8802f, 19.1871f)
                curveTo(3.912f, 19.5763f, 3.9816f, 19.9548f, 4.1661f, 20.3168f)
                curveTo(4.4441f, 20.8624f, 4.8877f, 21.3061f, 5.4334f, 21.5841f)
                curveTo(5.7953f, 21.7685f, 6.1739f, 21.8382f, 6.5631f, 21.87f)
                curveTo(6.9335f, 21.9002f, 7.3847f, 21.9002f, 7.9145f, 21.9002f)
                horizontalLineTo(16.0853f)
                curveTo(16.6152f, 21.9002f, 17.0664f, 21.9002f, 17.4368f, 21.87f)
                curveTo(17.826f, 21.8382f, 18.2046f, 21.7685f, 18.5665f, 21.5841f)
                curveTo(19.1122f, 21.3061f, 19.5558f, 20.8624f, 19.8338f, 20.3168f)
                curveTo(20.0183f, 19.9548f, 20.0879f, 19.5763f, 20.1197f, 19.1871f)
                curveTo(20.15f, 18.8167f, 20.1499f, 18.3655f, 20.1499f, 17.8357f)
                verticalLineTo(9.8501f)
                lineTo(15.0717f, 9.8501f)
                curveTo(14.8164f, 9.8501f, 14.5762f, 9.8501f, 14.3743f, 9.8336f)
                curveTo(14.155f, 9.8157f, 13.9072f, 9.7742f, 13.6604f, 9.6485f)
                curveTo(13.3123f, 9.4711f, 13.0293f, 9.1881f, 12.852f, 8.84f)
                curveTo(12.7262f, 8.5932f, 12.6847f, 8.3454f, 12.6668f, 8.1261f)
                curveTo(12.6503f, 7.9243f, 12.6503f, 7.6841f, 12.6503f, 7.4288f)
                lineTo(12.6503f, 2.1001f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(14.3503f, 2.8278f)
                lineTo(14.3549f, 2.8324f)
                lineTo(19.6653f, 8.1505f)
                horizontalLineTo(14.5003f)
                curveTo(14.4175f, 8.1505f, 14.3503f, 8.0833f, 14.3503f, 8.0005f)
                verticalLineTo(2.8278f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun DocumentFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.DocumentFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
