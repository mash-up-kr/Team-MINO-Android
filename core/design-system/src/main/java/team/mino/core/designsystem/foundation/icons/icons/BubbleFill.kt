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

val MinoIcons.BubbleFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.BubbleFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(12.0001f, 2.35f)
                curveTo(6.6706f, 2.35f, 2.3502f, 6.6705f, 2.3502f, 12f)
                curveTo(2.3502f, 17.3296f, 6.6706f, 21.65f, 12.0001f, 21.65f)
                curveTo(13.5385f, 21.65f, 14.9948f, 21.2894f, 16.2873f, 20.6476f)
                lineTo(18.0997f, 21.1419f)
                curveTo(18.5099f, 21.2538f, 18.8757f, 21.3536f, 19.1763f, 21.4067f)
                curveTo(19.4809f, 21.4604f, 19.8669f, 21.4971f, 20.2526f, 21.3498f)
                curveTo(20.7579f, 21.1569f, 21.157f, 20.7578f, 21.3499f, 20.2525f)
                curveTo(21.4971f, 19.8669f, 21.4605f, 19.4808f, 21.4067f, 19.1762f)
                curveTo(21.3536f, 18.8757f, 21.2538f, 18.5098f, 21.142f, 18.0997f)
                lineTo(20.6476f, 16.2872f)
                curveTo(21.2895f, 14.9948f, 21.6501f, 13.5384f, 21.6501f, 12f)
                curveTo(21.6501f, 6.6705f, 17.3296f, 2.35f, 12.0001f, 2.35f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun BubbleFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.BubbleFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
