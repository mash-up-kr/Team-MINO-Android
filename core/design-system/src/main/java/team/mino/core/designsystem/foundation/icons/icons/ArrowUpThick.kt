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

val MinoIcons.ArrowUpThick: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.ArrowUpThick",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.0808f, 2.5806f)
                curveTo(11.5885f, 2.073f, 12.4116f, 2.073f, 12.9193f, 2.5806f)
                lineTo(19.9193f, 9.5806f)
                curveTo(20.427f, 10.0883f, 20.427f, 10.9114f, 19.9193f, 11.4191f)
                curveTo(19.4116f, 11.9268f, 18.5885f, 11.9268f, 18.0808f, 11.4191f)
                lineTo(13.3001f, 6.6384f)
                verticalLineTo(20.4999f)
                curveTo(13.3001f, 21.2179f, 12.718f, 21.7999f, 12.0001f, 21.7999f)
                curveTo(11.2821f, 21.7999f, 10.7001f, 21.2179f, 10.7001f, 20.4999f)
                verticalLineTo(6.6384f)
                lineTo(5.9193f, 11.4191f)
                curveTo(5.4117f, 11.9268f, 4.5885f, 11.9268f, 4.0809f, 11.4191f)
                curveTo(3.5732f, 10.9114f, 3.5732f, 10.0883f, 4.0809f, 9.5806f)
                lineTo(11.0808f, 2.5806f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ArrowUpThickPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.ArrowUpThick,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
