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

val MinoIcons.EyeFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.EyeFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(9.8997f, 12.0001f)
                curveTo(9.8997f, 10.8403f, 10.8399f, 9.9001f, 11.9997f, 9.9001f)
                curveTo(13.1595f, 9.9001f, 14.0997f, 10.8403f, 14.0997f, 12.0001f)
                curveTo(14.0997f, 13.1599f, 13.1595f, 14.1001f, 11.9997f, 14.1001f)
                curveTo(10.8399f, 14.1001f, 9.8997f, 13.1599f, 9.8997f, 12.0001f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(22.0937f, 12.634f)
                curveTo(22.2606f, 12.2274f, 22.2606f, 11.771f, 22.0937f, 11.3643f)
                curveTo(20.465f, 7.395f, 16.5617f, 4.5995f, 12.0055f, 4.5995f)
                curveTo(7.4493f, 4.5995f, 3.546f, 7.395f, 1.9172f, 11.3643f)
                curveTo(1.7504f, 11.771f, 1.7504f, 12.2274f, 1.9172f, 12.634f)
                curveTo(3.546f, 16.6034f, 7.4493f, 19.3989f, 12.0055f, 19.3989f)
                curveTo(16.5617f, 19.3989f, 20.465f, 16.6034f, 22.0937f, 12.634f)
                close()
                moveTo(11.9997f, 8.1001f)
                curveTo(9.8458f, 8.1001f, 8.0997f, 9.8462f, 8.0997f, 12.0001f)
                curveTo(8.0997f, 14.154f, 9.8458f, 15.9001f, 11.9997f, 15.9001f)
                curveTo(14.1536f, 15.9001f, 15.8997f, 14.154f, 15.8997f, 12.0001f)
                curveTo(15.8997f, 9.8462f, 14.1536f, 8.1001f, 11.9997f, 8.1001f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun EyeFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.EyeFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
