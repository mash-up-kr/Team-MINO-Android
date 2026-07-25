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

val MinoIcons.Eye: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Eye",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(11.9998f, 8.0993f)
                curveTo(9.8459f, 8.0993f, 8.0998f, 9.8454f, 8.0998f, 11.9993f)
                curveTo(8.0998f, 14.1533f, 9.8459f, 15.8993f, 11.9998f, 15.8993f)
                curveTo(14.1537f, 15.8993f, 15.8998f, 14.1533f, 15.8998f, 11.9993f)
                curveTo(15.8998f, 9.8454f, 14.1537f, 8.0993f, 11.9998f, 8.0993f)
                close()
                moveTo(9.8998f, 11.9993f)
                curveTo(9.8998f, 10.8395f, 10.84f, 9.8993f, 11.9998f, 9.8993f)
                curveTo(13.1596f, 9.8993f, 14.0998f, 10.8395f, 14.0998f, 11.9993f)
                curveTo(14.0998f, 13.1591f, 13.1596f, 14.0993f, 11.9998f, 14.0993f)
                curveTo(10.84f, 14.0993f, 9.8998f, 13.1591f, 9.8998f, 11.9993f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(11.9999f, 19.3989f)
                curveTo(16.5561f, 19.3989f, 20.4594f, 16.6034f, 22.0882f, 12.634f)
                curveTo(22.2551f, 12.2274f, 22.2551f, 11.771f, 22.0882f, 11.3643f)
                curveTo(20.4594f, 7.395f, 16.5561f, 4.5995f, 11.9999f, 4.5995f)
                curveTo(7.4437f, 4.5995f, 3.5404f, 7.395f, 1.9117f, 11.3643f)
                curveTo(1.7448f, 11.771f, 1.7448f, 12.2274f, 1.9117f, 12.634f)
                curveTo(3.5404f, 16.6034f, 7.4437f, 19.3989f, 11.9999f, 19.3989f)
                close()
                moveTo(3.5975f, 11.9992f)
                curveTo(4.9689f, 8.7108f, 8.2145f, 6.3995f, 11.9999f, 6.3995f)
                curveTo(15.7853f, 6.3995f, 19.0309f, 8.7108f, 20.4024f, 11.9992f)
                curveTo(19.0309f, 15.2875f, 15.7853f, 17.5989f, 11.9999f, 17.5989f)
                curveTo(8.2145f, 17.5989f, 4.9689f, 15.2875f, 3.5975f, 11.9992f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun EyePreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Eye,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
