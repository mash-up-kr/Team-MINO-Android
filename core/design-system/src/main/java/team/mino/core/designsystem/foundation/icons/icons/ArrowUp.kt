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

val MinoIcons.ArrowUp: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.ArrowUp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(12.6365f, 2.8634f)
                curveTo(12.285f, 2.512f, 11.7152f, 2.512f, 11.3637f, 2.8634f)
                lineTo(4.3637f, 9.8634f)
                curveTo(4.0123f, 10.2149f, 4.0123f, 10.7848f, 4.3637f, 11.1362f)
                curveTo(4.7152f, 11.4877f, 5.2851f, 11.4877f, 5.6365f, 11.1362f)
                lineTo(11.1001f, 5.6726f)
                verticalLineTo(20.4998f)
                curveTo(11.1001f, 20.9969f, 11.503f, 21.3998f, 12.0001f, 21.3998f)
                curveTo(12.4972f, 21.3998f, 12.9001f, 20.9969f, 12.9001f, 20.4998f)
                verticalLineTo(5.6726f)
                lineTo(18.3637f, 11.1362f)
                curveTo(18.7152f, 11.4877f, 19.285f, 11.4877f, 19.6365f, 11.1362f)
                curveTo(19.9879f, 10.7848f, 19.9879f, 10.2149f, 19.6365f, 9.8634f)
                lineTo(12.6365f, 2.8634f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ArrowUpPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.ArrowUp,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
