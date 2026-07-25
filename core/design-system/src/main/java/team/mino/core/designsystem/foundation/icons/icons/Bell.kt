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

val MinoIcons.Bell: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Bell",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(11.9999f, 2.1001f)
                curveTo(9.7747f, 2.1001f, 7.9453f, 2.8879f, 6.6878f, 4.3744f)
                curveTo(5.4477f, 5.8404f, 4.85f, 7.8832f, 4.85f, 10.2501f)
                lineTo(4.85f, 11.0001f)
                curveTo(4.85f, 13.4652f, 4.1667f, 14.9249f, 3.1936f, 15.8682f)
                curveTo(2.8121f, 16.2381f, 2.7725f, 16.7499f, 2.9089f, 17.1264f)
                curveTo(3.0476f, 17.5094f, 3.4277f, 17.9001f, 3.9995f, 17.9001f)
                horizontalLineTo(20.0004f)
                curveTo(20.5722f, 17.9001f, 20.9523f, 17.5094f, 21.091f, 17.1264f)
                curveTo(21.2274f, 16.7499f, 21.1878f, 16.2381f, 20.8063f, 15.8682f)
                curveTo(19.8331f, 14.9249f, 19.1499f, 13.4652f, 19.1499f, 11.0001f)
                lineTo(19.1499f, 10.2501f)
                curveTo(19.1499f, 7.8832f, 18.5522f, 5.8404f, 17.312f, 4.3744f)
                curveTo(16.0546f, 2.8879f, 14.2252f, 2.1001f, 11.9999f, 2.1001f)
                close()
                moveTo(6.65f, 10.2501f)
                curveTo(6.65f, 8.1652f, 7.1772f, 6.583f, 8.0621f, 5.5369f)
                curveTo(8.9296f, 4.5114f, 10.2252f, 3.9001f, 11.9999f, 3.9001f)
                curveTo(13.7747f, 3.9001f, 15.0703f, 4.5114f, 15.9378f, 5.5369f)
                curveTo(16.8227f, 6.583f, 17.3499f, 8.1652f, 17.3499f, 10.2501f)
                lineTo(17.3499f, 11.0001f)
                curveTo(17.3499f, 13.1628f, 17.8181f, 14.8273f, 18.6686f, 16.1001f)
                horizontalLineTo(5.3312f)
                curveTo(6.1818f, 14.8273f, 6.65f, 13.1628f, 6.65f, 11.0001f)
                lineTo(6.65f, 10.2501f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(10f, 20.0999f)
                curveTo(9.5029f, 20.0999f, 9.1f, 20.5029f, 9.1f, 20.9999f)
                curveTo(9.1f, 21.497f, 9.5029f, 21.8999f, 10f, 21.8999f)
                horizontalLineTo(13.9999f)
                curveTo(14.497f, 21.8999f, 14.8999f, 21.497f, 14.8999f, 20.9999f)
                curveTo(14.8999f, 20.5029f, 14.497f, 20.0999f, 13.9999f, 20.0999f)
                horizontalLineTo(10f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun BellPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Bell,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
