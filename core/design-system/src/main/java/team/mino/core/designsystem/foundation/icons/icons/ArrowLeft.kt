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

val MinoIcons.ArrowLeft: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.ArrowLeft",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(2.8636f, 11.3635f)
                curveTo(2.5121f, 11.715f, 2.5121f, 12.2848f, 2.8636f, 12.6363f)
                lineTo(9.8636f, 19.6363f)
                curveTo(10.215f, 19.9878f, 10.7849f, 19.9878f, 11.1363f, 19.6363f)
                curveTo(11.4878f, 19.2848f, 11.4878f, 18.715f, 11.1363f, 18.3635f)
                lineTo(5.6728f, 12.8999f)
                horizontalLineTo(20.4999f)
                curveTo(20.997f, 12.8999f, 21.3999f, 12.497f, 21.3999f, 11.9999f)
                curveTo(21.3999f, 11.5029f, 20.997f, 11.0999f, 20.4999f, 11.0999f)
                lineTo(5.6728f, 11.0999f)
                lineTo(11.1363f, 5.6363f)
                curveTo(11.4878f, 5.2848f, 11.4878f, 4.715f, 11.1363f, 4.3635f)
                curveTo(10.7849f, 4.012f, 10.215f, 4.012f, 9.8636f, 4.3635f)
                lineTo(2.8636f, 11.3635f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ArrowLeftPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.ArrowLeft,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
