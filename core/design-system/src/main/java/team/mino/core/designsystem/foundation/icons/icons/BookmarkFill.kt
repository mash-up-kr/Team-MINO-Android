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

val MinoIcons.BookmarkFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.BookmarkFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(8.1645f, 2.5998f)
                curveTo(7.6346f, 2.5998f, 7.1834f, 2.5998f, 6.813f, 2.6301f)
                curveTo(6.4238f, 2.6619f, 6.0452f, 2.7315f, 5.6833f, 2.9159f)
                curveTo(5.1376f, 3.194f, 4.694f, 3.6376f, 4.4159f, 4.1833f)
                curveTo(4.2315f, 4.5452f, 4.1619f, 4.9238f, 4.1301f, 5.313f)
                curveTo(4.0998f, 5.6834f, 4.0998f, 6.1346f, 4.0999f, 6.6644f)
                verticalLineTo(21.4999f)
                curveTo(4.0999f, 21.8249f, 4.2752f, 22.1248f, 4.5585f, 22.2842f)
                curveTo(4.8418f, 22.4436f, 5.1891f, 22.4379f, 5.4669f, 22.2692f)
                lineTo(11.9998f, 18.3027f)
                lineTo(18.5327f, 22.2692f)
                curveTo(18.8106f, 22.4379f, 19.1579f, 22.4436f, 19.4412f, 22.2842f)
                curveTo(19.7245f, 22.1248f, 19.8998f, 21.8249f, 19.8998f, 21.4999f)
                verticalLineTo(6.6645f)
                curveTo(19.8998f, 6.1346f, 19.8998f, 5.6834f, 19.8696f, 5.313f)
                curveTo(19.8378f, 4.9238f, 19.7681f, 4.5452f, 19.5837f, 4.1833f)
                curveTo(19.3057f, 3.6376f, 18.8621f, 3.194f, 18.3164f, 2.9159f)
                curveTo(17.9545f, 2.7315f, 17.5759f, 2.6619f, 17.1867f, 2.6301f)
                curveTo(16.8163f, 2.5998f, 16.3651f, 2.5998f, 15.8353f, 2.5998f)
                horizontalLineTo(8.1645f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun BookmarkFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.BookmarkFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
