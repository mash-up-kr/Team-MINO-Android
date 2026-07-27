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

val MinoIcons.ArrowRight: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.ArrowRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(21.1365f, 12.6362f)
                curveTo(21.4879f, 12.2848f, 21.4879f, 11.7149f, 21.1365f, 11.3635f)
                lineTo(14.1365f, 4.3635f)
                curveTo(13.785f, 4.012f, 13.2152f, 4.012f, 12.8637f, 4.3635f)
                curveTo(12.5122f, 4.7149f, 12.5122f, 5.2848f, 12.8637f, 5.6363f)
                lineTo(18.3273f, 11.0999f)
                horizontalLineTo(3.5001f)
                curveTo(3.0031f, 11.0999f, 2.6001f, 11.5028f, 2.6001f, 11.9999f)
                curveTo(2.6001f, 12.4969f, 3.0031f, 12.8999f, 3.5001f, 12.8999f)
                horizontalLineTo(18.3273f)
                lineTo(12.8637f, 18.3635f)
                curveTo(12.5122f, 18.7149f, 12.5122f, 19.2848f, 12.8637f, 19.6363f)
                curveTo(13.2152f, 19.9877f, 13.785f, 19.9877f, 14.1365f, 19.6362f)
                lineTo(21.1365f, 12.6362f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ArrowRightPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.ArrowRight,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
