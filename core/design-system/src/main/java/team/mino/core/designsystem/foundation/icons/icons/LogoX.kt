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

val MinoIcons.LogoX: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.LogoX",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(17.1761f, 3.9f)
                horizontalLineTo(19.9362f)
                lineTo(13.9061f, 10.7622f)
                lineTo(21f, 20.1f)
                horizontalLineTo(15.4456f)
                lineTo(11.0951f, 14.4366f)
                lineTo(6.1172f, 20.1f)
                horizontalLineTo(3.3554f)
                lineTo(9.8052f, 12.7602f)
                lineTo(3f, 3.9f)
                horizontalLineTo(8.6954f)
                lineTo(12.6279f, 9.0765f)
                lineTo(17.1761f, 3.9f)
                close()
                moveTo(16.2073f, 18.4551f)
                horizontalLineTo(17.7368f)
                lineTo(7.8644f, 5.4585f)
                horizontalLineTo(6.2232f)
                lineTo(16.2073f, 18.4551f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun LogoXPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.LogoX,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
