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

val MinoIcons.List: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.List",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(3.7497f, 7f)
                curveTo(4.44f, 7f, 4.9997f, 6.4404f, 4.9997f, 5.75f)
                curveTo(4.9997f, 5.0596f, 4.44f, 4.5f, 3.7497f, 4.5f)
                curveTo(3.0593f, 4.5f, 2.4997f, 5.0596f, 2.4997f, 5.75f)
                curveTo(2.4997f, 6.4404f, 3.0593f, 7f, 3.7497f, 7f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(7.3498f, 5.75f)
                curveTo(7.3498f, 5.2529f, 7.7527f, 4.85f, 8.2498f, 4.85f)
                horizontalLineTo(20.2498f)
                curveTo(20.7468f, 4.85f, 21.1498f, 5.2529f, 21.1498f, 5.75f)
                curveTo(21.1498f, 6.2471f, 20.7468f, 6.65f, 20.2498f, 6.65f)
                horizontalLineTo(8.2498f)
                curveTo(7.7527f, 6.65f, 7.3498f, 6.2471f, 7.3498f, 5.75f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(3.7497f, 13.25f)
                curveTo(4.44f, 13.25f, 4.9997f, 12.6904f, 4.9997f, 12f)
                curveTo(4.9997f, 11.3096f, 4.44f, 10.75f, 3.7497f, 10.75f)
                curveTo(3.0593f, 10.75f, 2.4997f, 11.3096f, 2.4997f, 12f)
                curveTo(2.4997f, 12.6904f, 3.0593f, 13.25f, 3.7497f, 13.25f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(7.3498f, 12f)
                curveTo(7.3498f, 11.5029f, 7.7527f, 11.1f, 8.2498f, 11.1f)
                horizontalLineTo(20.2498f)
                curveTo(20.7468f, 11.1f, 21.1498f, 11.5029f, 21.1498f, 12f)
                curveTo(21.1498f, 12.4971f, 20.7468f, 12.9f, 20.2498f, 12.9f)
                horizontalLineTo(8.2498f)
                curveTo(7.7527f, 12.9f, 7.3498f, 12.4971f, 7.3498f, 12f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(3.7497f, 19.5f)
                curveTo(4.44f, 19.5f, 4.9997f, 18.9404f, 4.9997f, 18.25f)
                curveTo(4.9997f, 17.5596f, 4.44f, 17f, 3.7497f, 17f)
                curveTo(3.0593f, 17f, 2.4997f, 17.5596f, 2.4997f, 18.25f)
                curveTo(2.4997f, 18.9404f, 3.0593f, 19.5f, 3.7497f, 19.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(8.2498f, 17.35f)
                curveTo(7.7527f, 17.35f, 7.3498f, 17.7529f, 7.3498f, 18.25f)
                curveTo(7.3498f, 18.7471f, 7.7527f, 19.15f, 8.2498f, 19.15f)
                horizontalLineTo(20.2498f)
                curveTo(20.7468f, 19.15f, 21.1498f, 18.7471f, 21.1498f, 18.25f)
                curveTo(21.1498f, 17.7529f, 20.7468f, 17.35f, 20.2498f, 17.35f)
                horizontalLineTo(8.2498f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ListPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.List,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
