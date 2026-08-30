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

val MinoIcons.ListCategory: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.ListCategory",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(3.1f, 5.7501f)
                curveTo(3.1f, 5.253f, 3.5029f, 4.8501f, 4f, 4.8501f)
                horizontalLineTo(19.9999f)
                curveTo(20.497f, 4.8501f, 20.8999f, 5.253f, 20.8999f, 5.7501f)
                curveTo(20.8999f, 6.2472f, 20.497f, 6.6501f, 19.9999f, 6.6501f)
                horizontalLineTo(4f)
                curveTo(3.5029f, 6.6501f, 3.1f, 6.2472f, 3.1f, 5.7501f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(3.1f, 12.0001f)
                curveTo(3.1f, 11.503f, 3.5029f, 11.1001f, 4f, 11.1001f)
                horizontalLineTo(19.9999f)
                curveTo(20.497f, 11.1001f, 20.8999f, 11.503f, 20.8999f, 12.0001f)
                curveTo(20.8999f, 12.4972f, 20.497f, 12.9001f, 19.9999f, 12.9001f)
                horizontalLineTo(4f)
                curveTo(3.5029f, 12.9001f, 3.1f, 12.4972f, 3.1f, 12.0001f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(3.1f, 18.2501f)
                curveTo(3.1f, 17.753f, 3.5029f, 17.3501f, 4f, 17.3501f)
                horizontalLineTo(13.7499f)
                curveTo(14.247f, 17.3501f, 14.6499f, 17.753f, 14.6499f, 18.2501f)
                curveTo(14.6499f, 18.7472f, 14.247f, 19.1501f, 13.7499f, 19.1501f)
                horizontalLineTo(4f)
                curveTo(3.5029f, 19.1501f, 3.1f, 18.7472f, 3.1f, 18.2501f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ListCategoryPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.ListCategory,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
