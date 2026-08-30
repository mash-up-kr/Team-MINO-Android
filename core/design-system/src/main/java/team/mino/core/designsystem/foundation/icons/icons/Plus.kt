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

val MinoIcons.Plus: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Plus",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.9998f, 3.1029f)
                curveTo(12.4968f, 3.1029f, 12.8998f, 3.5059f, 12.8998f, 4.0029f)
                verticalLineTo(11.1029f)
                horizontalLineTo(19.9997f)
                curveTo(20.4968f, 11.1029f, 20.8997f, 11.5059f, 20.8997f, 12.0029f)
                curveTo(20.8997f, 12.5f, 20.4968f, 12.9029f, 19.9997f, 12.9029f)
                horizontalLineTo(12.8998f)
                verticalLineTo(20.0029f)
                curveTo(12.8998f, 20.5f, 12.4968f, 20.9029f, 11.9998f, 20.9029f)
                curveTo(11.5027f, 20.9029f, 11.0998f, 20.5f, 11.0998f, 20.0029f)
                verticalLineTo(12.9029f)
                horizontalLineTo(3.9998f)
                curveTo(3.5027f, 12.9029f, 3.0998f, 12.5f, 3.0998f, 12.0029f)
                curveTo(3.0998f, 11.5059f, 3.5027f, 11.1029f, 3.9998f, 11.1029f)
                horizontalLineTo(11.0998f)
                verticalLineTo(4.0029f)
                curveTo(11.0998f, 3.5059f, 11.5027f, 3.1029f, 11.9998f, 3.1029f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun PlusPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Plus,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
