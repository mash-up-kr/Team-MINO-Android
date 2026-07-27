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

val MinoIcons.CheckThick: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.CheckThick",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(19.6697f, 6.581f)
                curveTo(20.1774f, 7.0886f, 20.1774f, 7.9117f, 19.6697f, 8.4194f)
                lineTo(10.6697f, 17.4194f)
                curveTo(10.1621f, 17.9271f, 9.3389f, 17.9271f, 8.8313f, 17.4194f)
                lineTo(4.3313f, 12.9194f)
                curveTo(3.8236f, 12.4118f, 3.8236f, 11.5886f, 4.3313f, 11.081f)
                curveTo(4.8389f, 10.5733f, 5.6621f, 10.5733f, 6.1697f, 11.081f)
                lineTo(9.7505f, 14.6617f)
                lineTo(17.8313f, 6.581f)
                curveTo(18.3389f, 6.0733f, 19.1621f, 6.0733f, 19.6697f, 6.581f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun CheckThickPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.CheckThick,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
