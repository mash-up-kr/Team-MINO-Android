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

val MinoIcons.Close: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Close",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(4.8635f, 4.8635f)
                curveTo(5.215f, 4.512f, 5.7848f, 4.512f, 6.1363f, 4.8635f)
                lineTo(11.9999f, 10.7271f)
                lineTo(17.8635f, 4.8635f)
                curveTo(18.2149f, 4.512f, 18.7848f, 4.512f, 19.1362f, 4.8635f)
                curveTo(19.4877f, 5.2149f, 19.4877f, 5.7848f, 19.1363f, 6.1363f)
                lineTo(13.2727f, 11.9999f)
                lineTo(19.1363f, 17.8635f)
                curveTo(19.4877f, 18.2149f, 19.4877f, 18.7848f, 19.1362f, 19.1363f)
                curveTo(18.7848f, 19.4877f, 18.2149f, 19.4877f, 17.8635f, 19.1363f)
                lineTo(11.9999f, 13.2727f)
                lineTo(6.1363f, 19.1363f)
                curveTo(5.7848f, 19.4877f, 5.215f, 19.4877f, 4.8635f, 19.1363f)
                curveTo(4.512f, 18.7848f, 4.512f, 18.2149f, 4.8635f, 17.8635f)
                lineTo(10.7271f, 11.9999f)
                lineTo(4.8635f, 6.1363f)
                curveTo(4.512f, 5.7848f, 4.512f, 5.2149f, 4.8635f, 4.8635f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ClosePreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Close,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
