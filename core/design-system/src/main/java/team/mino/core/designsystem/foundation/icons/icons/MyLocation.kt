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

val MinoIcons.MyLocation: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.MyLocation",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(12f, 1f)
                curveTo(18.0751f, 1f, 23f, 5.9249f, 23f, 12f)
                curveTo(23f, 18.0751f, 18.0751f, 23f, 12f, 23f)
                curveTo(5.9249f, 23f, 1f, 18.0751f, 1f, 12f)
                curveTo(1f, 5.9249f, 5.9249f, 1f, 12f, 1f)
                close()
                moveTo(13f, 7f)
                curveTo(13f, 7.5523f, 12.5523f, 8f, 12f, 8f)
                curveTo(11.4477f, 8f, 11f, 7.5523f, 11f, 7f)
                verticalLineTo(2.5518f)
                curveTo(6.552f, 3.0171f, 3.0181f, 6.552f, 2.5527f, 11f)
                horizontalLineTo(7f)
                curveTo(7.5523f, 11f, 8f, 11.4477f, 8f, 12f)
                curveTo(8f, 12.5523f, 7.5523f, 13f, 7f, 13f)
                horizontalLineTo(2.5527f)
                curveTo(3.0181f, 17.448f, 6.552f, 20.9819f, 11f, 21.4473f)
                verticalLineTo(17f)
                curveTo(11f, 16.4477f, 11.4477f, 16f, 12f, 16f)
                curveTo(12.5523f, 16f, 13f, 16.4477f, 13f, 17f)
                verticalLineTo(21.4473f)
                curveTo(17.448f, 20.9819f, 20.9819f, 17.448f, 21.4473f, 13f)
                horizontalLineTo(17f)
                curveTo(16.4477f, 13f, 16f, 12.5523f, 16f, 12f)
                curveTo(16f, 11.4477f, 16.4477f, 11f, 17f, 11f)
                horizontalLineTo(21.4473f)
                curveTo(20.9819f, 6.552f, 17.448f, 3.0171f, 13f, 2.5518f)
                verticalLineTo(7f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun MyLocationPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.MyLocation,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
