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

val MinoIcons.ArrowLeftThick: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.ArrowLeftThick",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(2.5809f, 11.0807f)
                curveTo(2.0732f, 11.5884f, 2.0732f, 12.4115f, 2.5809f, 12.9192f)
                lineTo(9.5809f, 19.9192f)
                curveTo(10.0885f, 20.4269f, 10.9117f, 20.4269f, 11.4193f, 19.9192f)
                curveTo(11.927f, 19.4115f, 11.927f, 18.5884f, 11.4193f, 18.0807f)
                lineTo(6.6386f, 13.3f)
                horizontalLineTo(20.5001f)
                curveTo(21.218f, 13.3f, 21.8001f, 12.7179f, 21.8001f, 12f)
                curveTo(21.8001f, 11.282f, 21.218f, 10.7f, 20.5001f, 10.7f)
                lineTo(6.6386f, 10.7f)
                lineTo(11.4193f, 5.9192f)
                curveTo(11.927f, 5.4115f, 11.927f, 4.5884f, 11.4193f, 4.0807f)
                curveTo(10.9117f, 3.573f, 10.0885f, 3.573f, 9.5809f, 4.0807f)
                lineTo(2.5809f, 11.0807f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ArrowLeftThickPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.ArrowLeftThick,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
