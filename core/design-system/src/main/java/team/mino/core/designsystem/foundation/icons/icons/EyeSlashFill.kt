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

val MinoIcons.EyeSlashFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.EyeSlashFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(4.6365f, 4.1132f)
                curveTo(4.2851f, 3.7617f, 3.7152f, 3.7617f, 3.3637f, 4.1132f)
                curveTo(3.0122f, 4.4647f, 3.0122f, 5.0345f, 3.3637f, 5.386f)
                lineTo(5.069f, 7.0913f)
                curveTo(3.6952f, 8.2259f, 2.6042f, 9.6908f, 1.9175f, 11.3643f)
                curveTo(1.7506f, 11.7709f, 1.7506f, 12.2274f, 1.9175f, 12.634f)
                curveTo(3.5463f, 16.6034f, 7.4496f, 19.3988f, 12.0058f, 19.3988f)
                curveTo(13.5839f, 19.3988f, 15.0837f, 19.0634f, 16.4378f, 18.4601f)
                lineTo(18.3637f, 20.386f)
                curveTo(18.7152f, 20.7375f, 19.285f, 20.7375f, 19.6365f, 20.386f)
                curveTo(19.988f, 20.0345f, 19.988f, 19.4647f, 19.6365f, 19.1132f)
                lineTo(4.6365f, 4.1132f)
                close()
                moveTo(13.5553f, 15.5775f)
                lineTo(12.0763f, 14.0986f)
                curveTo(12.051f, 14.0995f, 12.0256f, 14.1f, 12f, 14.1f)
                curveTo(10.8402f, 14.1f, 9.9f, 13.1598f, 9.9f, 12f)
                curveTo(9.9f, 11.9744f, 9.9005f, 11.949f, 9.9014f, 11.9237f)
                lineTo(8.4225f, 10.4447f)
                curveTo(8.215f, 10.9212f, 8.1f, 11.4472f, 8.1f, 12f)
                curveTo(8.1f, 14.1539f, 9.8461f, 15.9f, 12f, 15.9f)
                curveTo(12.5528f, 15.9f, 13.0788f, 15.785f, 13.5553f, 15.5775f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(15.8189f, 12.7951f)
                lineTo(19.4679f, 16.4441f)
                curveTo(20.5954f, 15.3848f, 21.4973f, 14.0882f, 22.094f, 12.634f)
                curveTo(22.2609f, 12.2274f, 22.2609f, 11.7709f, 22.094f, 11.3643f)
                curveTo(20.4652f, 7.3949f, 16.562f, 4.5995f, 12.0058f, 4.5995f)
                curveTo(10.6962f, 4.5995f, 9.4407f, 4.8304f, 8.2775f, 5.2537f)
                lineTo(11.2049f, 8.1811f)
                curveTo(11.4616f, 8.1279f, 11.7275f, 8.1f, 12f, 8.1f)
                curveTo(14.1539f, 8.1f, 15.9f, 9.8461f, 15.9f, 12f)
                curveTo(15.9f, 12.2725f, 15.8721f, 12.5384f, 15.8189f, 12.7951f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun EyeSlashFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.EyeSlashFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
