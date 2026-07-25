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

val MinoIcons.CircleExclamation: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.CircleExclamation",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(12.9999f, 15.9999f)
                curveTo(12.9999f, 16.5522f, 12.5522f, 16.9999f, 11.9999f, 16.9999f)
                curveTo(11.4476f, 16.9999f, 10.9999f, 16.5522f, 10.9999f, 15.9999f)
                curveTo(10.9999f, 15.4476f, 11.4476f, 14.9999f, 11.9999f, 14.9999f)
                curveTo(12.5522f, 14.9999f, 12.9999f, 15.4476f, 12.9999f, 15.9999f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(12.9f, 7.9999f)
                curveTo(12.9f, 7.5028f, 12.4971f, 7.0999f, 12f, 7.0999f)
                curveTo(11.5029f, 7.0999f, 11.1f, 7.5028f, 11.1f, 7.9999f)
                verticalLineTo(12.4999f)
                curveTo(11.1f, 12.997f, 11.5029f, 13.3999f, 12f, 13.3999f)
                curveTo(12.4971f, 13.3999f, 12.9f, 12.997f, 12.9f, 12.4999f)
                verticalLineTo(7.9999f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(2.1f, 12f)
                curveTo(2.1f, 6.5324f, 6.5323f, 2.1f, 11.9999f, 2.1f)
                curveTo(17.4675f, 2.1f, 21.8999f, 6.5324f, 21.8999f, 12f)
                curveTo(21.8999f, 17.4676f, 17.4675f, 21.9f, 11.9999f, 21.9f)
                curveTo(6.5323f, 21.9f, 2.1f, 17.4676f, 2.1f, 12f)
                close()
                moveTo(11.9999f, 3.9f)
                curveTo(7.5264f, 3.9f, 3.8999f, 7.5265f, 3.8999f, 12f)
                curveTo(3.8999f, 16.4735f, 7.5264f, 20.1f, 11.9999f, 20.1f)
                curveTo(16.4734f, 20.1f, 20.0999f, 16.4735f, 20.0999f, 12f)
                curveTo(20.0999f, 7.5265f, 16.4734f, 3.9f, 11.9999f, 3.9f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun CircleExclamationPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.CircleExclamation,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
