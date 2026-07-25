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

val MinoIcons.Check: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Check",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(19.3863f, 6.8635f)
                curveTo(19.7378f, 7.2149f, 19.7378f, 7.7848f, 19.3863f, 8.1363f)
                lineTo(10.3864f, 17.1362f)
                curveTo(10.0349f, 17.4877f, 9.465f, 17.4877f, 9.1136f, 17.1362f)
                lineTo(4.6136f, 12.6362f)
                curveTo(4.2621f, 12.2848f, 4.2621f, 11.7149f, 4.6136f, 11.3635f)
                curveTo(4.9651f, 11.012f, 5.5349f, 11.012f, 5.8864f, 11.3635f)
                lineTo(9.75f, 15.2271f)
                lineTo(18.1135f, 6.8635f)
                curveTo(18.465f, 6.512f, 19.0349f, 6.512f, 19.3863f, 6.8635f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun CheckPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Check,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
