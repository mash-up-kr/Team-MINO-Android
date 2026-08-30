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

val MinoIcons.Bookmark: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Bookmark",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(8.1645f, 2.5998f)
                horizontalLineTo(15.8352f)
                curveTo(16.3651f, 2.5998f, 16.8163f, 2.5998f, 17.1867f, 2.6301f)
                curveTo(17.5759f, 2.6619f, 17.9545f, 2.7315f, 18.3164f, 2.9159f)
                curveTo(18.8621f, 3.194f, 19.3057f, 3.6376f, 19.5837f, 4.1833f)
                curveTo(19.7681f, 4.5452f, 19.8378f, 4.9238f, 19.8696f, 5.313f)
                curveTo(19.8998f, 5.6834f, 19.8998f, 6.1346f, 19.8998f, 6.6645f)
                verticalLineTo(21.4999f)
                curveTo(19.8998f, 21.8249f, 19.7245f, 22.1248f, 19.4412f, 22.2842f)
                curveTo(19.1579f, 22.4436f, 18.8106f, 22.4379f, 18.5327f, 22.2692f)
                lineTo(11.9998f, 18.3028f)
                lineTo(5.4669f, 22.2692f)
                curveTo(5.1891f, 22.4379f, 4.8418f, 22.4436f, 4.5585f, 22.2842f)
                curveTo(4.2752f, 22.1248f, 4.0999f, 21.8249f, 4.0999f, 21.4999f)
                verticalLineTo(6.6645f)
                curveTo(4.0999f, 6.1346f, 4.0998f, 5.6834f, 4.1301f, 5.313f)
                curveTo(4.1619f, 4.9238f, 4.2315f, 4.5452f, 4.4159f, 4.1833f)
                curveTo(4.694f, 3.6376f, 5.1376f, 3.194f, 5.6833f, 2.9159f)
                curveTo(6.0452f, 2.7315f, 6.4238f, 2.6619f, 6.813f, 2.6301f)
                curveTo(7.1834f, 2.5998f, 7.6346f, 2.5998f, 8.1645f, 2.5998f)
                close()
                moveTo(7.7999f, 4.3999f)
                curveTo(6.9853f, 4.3999f, 6.7915f, 4.411f, 6.66f, 4.4538f)
                curveTo(6.3251f, 4.5626f, 6.0625f, 4.8251f, 5.9537f, 5.16f)
                curveTo(5.911f, 5.2915f, 5.8999f, 5.4854f, 5.8999f, 6.2999f)
                verticalLineTo(19.9006f)
                lineTo(11.5328f, 16.4806f)
                curveTo(11.8198f, 16.3064f, 12.1799f, 16.3064f, 12.467f, 16.4806f)
                lineTo(18.0999f, 19.9006f)
                verticalLineTo(6.2999f)
                curveTo(18.0999f, 5.4854f, 18.0887f, 5.2915f, 18.046f, 5.16f)
                curveTo(17.9372f, 4.8251f, 17.6747f, 4.5626f, 17.3398f, 4.4538f)
                curveTo(17.2083f, 4.411f, 17.0144f, 4.3999f, 16.1999f, 4.3999f)
                horizontalLineTo(7.7999f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun BookmarkPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Bookmark,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
