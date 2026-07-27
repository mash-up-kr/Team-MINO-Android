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

val MinoIcons.BellFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.BellFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(6.6878f, 4.3744f)
                curveTo(7.9453f, 2.8879f, 9.7746f, 2.1001f, 11.9999f, 2.1001f)
                curveTo(14.2251f, 2.1001f, 16.0545f, 2.8879f, 17.312f, 4.3744f)
                curveTo(18.5521f, 5.8404f, 19.1498f, 7.8832f, 19.1498f, 10.2501f)
                lineTo(19.1498f, 11.0001f)
                curveTo(19.1498f, 13.4652f, 19.8331f, 14.9249f, 20.8062f, 15.8682f)
                curveTo(21.1877f, 16.2381f, 21.2273f, 16.7499f, 21.091f, 17.1264f)
                curveTo(20.9523f, 17.5094f, 20.5722f, 17.9001f, 20.0003f, 17.9001f)
                horizontalLineTo(3.9995f)
                curveTo(3.4276f, 17.9001f, 3.0475f, 17.5094f, 2.9088f, 17.1264f)
                curveTo(2.7724f, 16.7499f, 2.812f, 16.2381f, 3.1936f, 15.8682f)
                curveTo(4.1667f, 14.9249f, 4.8499f, 13.4652f, 4.8499f, 11.0001f)
                lineTo(4.8499f, 10.2501f)
                curveTo(4.8499f, 7.8832f, 5.4477f, 5.8404f, 6.6878f, 4.3744f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(9.0999f, 20.9999f)
                curveTo(9.0999f, 20.5029f, 9.5028f, 20.0999f, 9.9999f, 20.0999f)
                horizontalLineTo(13.9999f)
                curveTo(14.4969f, 20.0999f, 14.8999f, 20.5029f, 14.8999f, 20.9999f)
                curveTo(14.8999f, 21.497f, 14.4969f, 21.8999f, 13.9999f, 21.8999f)
                horizontalLineTo(9.9999f)
                curveTo(9.5028f, 21.8999f, 9.0999f, 21.497f, 9.0999f, 20.9999f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun BellFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.BellFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
