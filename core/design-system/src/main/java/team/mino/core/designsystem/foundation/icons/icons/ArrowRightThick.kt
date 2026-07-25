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

val MinoIcons.ArrowRightThick: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.ArrowRightThick",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(21.4193f, 11.0807f)
                curveTo(21.927f, 11.5884f, 21.927f, 12.4115f, 21.4193f, 12.9192f)
                lineTo(14.4193f, 19.9192f)
                curveTo(13.9116f, 20.4269f, 13.0885f, 20.4269f, 12.5808f, 19.9192f)
                curveTo(12.0732f, 19.4115f, 12.0732f, 18.5884f, 12.5808f, 18.0807f)
                lineTo(17.3616f, 13.3f)
                horizontalLineTo(3.5001f)
                curveTo(2.7821f, 13.3f, 2.2001f, 12.7179f, 2.2001f, 12f)
                curveTo(2.2001f, 11.282f, 2.7821f, 10.7f, 3.5001f, 10.7f)
                horizontalLineTo(17.3616f)
                lineTo(12.5808f, 5.9192f)
                curveTo(12.0732f, 5.4115f, 12.0732f, 4.5884f, 12.5808f, 4.0807f)
                curveTo(13.0885f, 3.573f, 13.9116f, 3.573f, 14.4193f, 4.0807f)
                lineTo(21.4193f, 11.0807f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ArrowRightThickPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.ArrowRightThick,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
