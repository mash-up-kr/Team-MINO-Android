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

val MinoIcons.LogoMicrosoft: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.LogoMicrosoft",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.3571f, 3f)
                horizontalLineTo(3f)
                verticalLineTo(11.3571f)
                horizontalLineTo(11.3571f)
                verticalLineTo(3f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(21f, 3f)
                horizontalLineTo(12.6429f)
                verticalLineTo(11.3571f)
                horizontalLineTo(21f)
                verticalLineTo(3f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(12.6429f, 12.6429f)
                horizontalLineTo(21f)
                verticalLineTo(21f)
                horizontalLineTo(12.6429f)
                verticalLineTo(12.6429f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.3571f, 12.6429f)
                horizontalLineTo(3f)
                verticalLineTo(21f)
                horizontalLineTo(11.3571f)
                verticalLineTo(12.6429f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun LogoMicrosoftPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.LogoMicrosoft,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
