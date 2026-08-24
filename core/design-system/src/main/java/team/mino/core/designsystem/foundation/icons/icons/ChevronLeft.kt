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

val MinoIcons.ChevronLeft: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.ChevronLeft",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(16.1363f, 3.3629f)
                curveTo(16.4878f, 3.7143f, 16.4878f, 4.2842f, 16.1363f, 4.6357f)
                lineTo(8.7727f, 11.9993f)
                lineTo(16.1363f, 19.3629f)
                curveTo(16.4878f, 19.7143f, 16.4878f, 20.2842f, 16.1363f, 20.6357f)
                curveTo(15.7848f, 20.9871f, 15.215f, 20.9871f, 14.8635f, 20.6357f)
                lineTo(6.8635f, 12.6357f)
                curveTo(6.512f, 12.2842f, 6.512f, 11.7143f, 6.8635f, 11.3629f)
                lineTo(14.8635f, 3.3629f)
                curveTo(15.215f, 3.0114f, 15.7848f, 3.0114f, 16.1363f, 3.3629f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ChevronLeftPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.ChevronLeft,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
