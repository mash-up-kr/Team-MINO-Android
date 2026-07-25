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

val MinoIcons.ArrowDown: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.ArrowDown",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(11.3637f, 21.1363f)
                curveTo(11.7152f, 21.4878f, 12.285f, 21.4878f, 12.6365f, 21.1363f)
                lineTo(19.6365f, 14.1363f)
                curveTo(19.9879f, 13.7848f, 19.9879f, 13.215f, 19.6365f, 12.8635f)
                curveTo(19.285f, 12.512f, 18.7152f, 12.512f, 18.3637f, 12.8635f)
                lineTo(12.9001f, 18.3271f)
                lineTo(12.9001f, 3.4999f)
                curveTo(12.9001f, 3.0029f, 12.4972f, 2.5999f, 12.0001f, 2.5999f)
                curveTo(11.503f, 2.5999f, 11.1001f, 3.0029f, 11.1001f, 3.4999f)
                verticalLineTo(18.3271f)
                lineTo(5.6365f, 12.8635f)
                curveTo(5.2851f, 12.512f, 4.7152f, 12.512f, 4.3637f, 12.8635f)
                curveTo(4.0123f, 13.215f, 4.0123f, 13.7848f, 4.3637f, 14.1363f)
                lineTo(11.3637f, 21.1363f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ArrowDownPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.ArrowDown,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
