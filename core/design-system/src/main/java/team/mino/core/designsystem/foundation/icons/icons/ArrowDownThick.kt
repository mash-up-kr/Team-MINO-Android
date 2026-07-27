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

val MinoIcons.ArrowDownThick: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.ArrowDownThick",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.0808f, 21.4192f)
                curveTo(11.5885f, 21.9269f, 12.4116f, 21.9269f, 12.9193f, 21.4192f)
                lineTo(19.9193f, 14.4192f)
                curveTo(20.427f, 13.9115f, 20.427f, 13.0884f, 19.9193f, 12.5807f)
                curveTo(19.4116f, 12.0731f, 18.5885f, 12.0731f, 18.0808f, 12.5807f)
                lineTo(13.3001f, 17.3615f)
                verticalLineTo(3.5f)
                curveTo(13.3001f, 2.782f, 12.718f, 2.2f, 12.0001f, 2.2f)
                curveTo(11.2821f, 2.2f, 10.7001f, 2.782f, 10.7001f, 3.5f)
                verticalLineTo(17.3615f)
                lineTo(5.9193f, 12.5807f)
                curveTo(5.4117f, 12.0731f, 4.5885f, 12.0731f, 4.0809f, 12.5807f)
                curveTo(3.5732f, 13.0884f, 3.5732f, 13.9115f, 4.0809f, 14.4192f)
                lineTo(11.0808f, 21.4192f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ArrowDownThickPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.ArrowDownThick,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
