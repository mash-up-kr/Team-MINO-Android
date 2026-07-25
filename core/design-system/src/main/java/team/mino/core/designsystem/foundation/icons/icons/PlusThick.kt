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

val MinoIcons.PlusThick: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.PlusThick",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.9997f, 2.7019f)
                curveTo(12.7177f, 2.7019f, 13.2997f, 3.284f, 13.2997f, 4.0019f)
                verticalLineTo(10.7019f)
                horizontalLineTo(19.9997f)
                curveTo(20.7177f, 10.7019f, 21.2997f, 11.284f, 21.2997f, 12.0019f)
                curveTo(21.2997f, 12.7199f, 20.7177f, 13.3019f, 19.9997f, 13.3019f)
                horizontalLineTo(13.2997f)
                verticalLineTo(20.0019f)
                curveTo(13.2997f, 20.7199f, 12.7177f, 21.3019f, 11.9997f, 21.3019f)
                curveTo(11.2817f, 21.3019f, 10.6997f, 20.7199f, 10.6997f, 20.0019f)
                verticalLineTo(13.3019f)
                horizontalLineTo(3.9997f)
                curveTo(3.2817f, 13.3019f, 2.6997f, 12.7199f, 2.6997f, 12.0019f)
                curveTo(2.6997f, 11.284f, 3.2817f, 10.7019f, 3.9997f, 10.7019f)
                horizontalLineTo(10.6997f)
                verticalLineTo(4.0019f)
                curveTo(10.6997f, 3.284f, 11.2817f, 2.7019f, 11.9997f, 2.7019f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun PlusThickPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.PlusThick,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
