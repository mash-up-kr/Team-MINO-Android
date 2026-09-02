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

val MinoIcons.ChevronRight: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.ChevronRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(7.8634f, 3.3627f)
                curveTo(7.5119f, 3.7142f, 7.5119f, 4.284f, 7.8634f, 4.6355f)
                lineTo(15.227f, 11.9991f)
                lineTo(7.8634f, 19.3627f)
                curveTo(7.5119f, 19.7142f, 7.5119f, 20.284f, 7.8634f, 20.6355f)
                curveTo(8.2148f, 20.987f, 8.7847f, 20.987f, 9.1362f, 20.6355f)
                lineTo(17.1362f, 12.6355f)
                curveTo(17.4876f, 12.284f, 17.4876f, 11.7142f, 17.1362f, 11.3627f)
                lineTo(9.1362f, 3.3627f)
                curveTo(8.7847f, 3.0112f, 8.2148f, 3.0112f, 7.8634f, 3.3627f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ChevronRightPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.ChevronRight,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
