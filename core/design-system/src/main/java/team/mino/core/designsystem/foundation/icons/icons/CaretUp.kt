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

val MinoIcons.CaretUp: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.CaretUp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(13.8631f, 9.2932f)
                curveTo(13.2253f, 8.5082f, 12.9063f, 8.1156f, 12.5224f, 7.9731f)
                curveTo(12.1856f, 7.8482f, 11.8152f, 7.8482f, 11.4785f, 7.9731f)
                curveTo(11.0945f, 8.1156f, 10.7756f, 8.5082f, 10.1378f, 9.2932f)
                lineTo(8.6801f, 11.0873f)
                curveTo(7.6636f, 12.3384f, 7.1553f, 12.9639f, 7.1519f, 13.4911f)
                curveTo(7.149f, 13.9495f, 7.3559f, 14.3841f, 7.7135f, 14.6709f)
                curveTo(8.1247f, 15.0007f, 8.9307f, 15.0007f, 10.5428f, 15.0007f)
                horizontalLineTo(13.4581f)
                curveTo(15.0701f, 15.0007f, 15.8761f, 15.0007f, 16.2874f, 14.6709f)
                curveTo(16.645f, 14.3841f, 16.8519f, 13.9495f, 16.8489f, 13.4911f)
                curveTo(16.8456f, 12.9639f, 16.3373f, 12.3384f, 15.3208f, 11.0873f)
                lineTo(13.8631f, 9.2932f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun CaretUpPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.CaretUp,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
