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

val MinoIcons.CaretDown: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.CaretDown",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(13.8627f, 14.7075f)
                curveTo(13.2249f, 15.4925f, 12.906f, 15.885f, 12.522f, 16.0276f)
                curveTo(12.1853f, 16.1525f, 11.8149f, 16.1525f, 11.4781f, 16.0276f)
                curveTo(11.0942f, 15.885f, 10.7752f, 15.4925f, 10.1374f, 14.7075f)
                lineTo(8.6797f, 12.9134f)
                curveTo(7.6632f, 11.6623f, 7.1549f, 11.0367f, 7.1516f, 10.5096f)
                curveTo(7.1486f, 10.0512f, 7.3555f, 9.6166f, 7.7131f, 9.3298f)
                curveTo(8.1243f, 9f, 8.9304f, 9f, 10.5424f, 9f)
                horizontalLineTo(13.4577f)
                curveTo(15.0698f, 9f, 15.8758f, 9f, 16.287f, 9.3298f)
                curveTo(16.6446f, 9.6166f, 16.8515f, 10.0512f, 16.8486f, 10.5096f)
                curveTo(16.8452f, 11.0367f, 16.3369f, 11.6623f, 15.3204f, 12.9134f)
                lineTo(13.8627f, 14.7075f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun CaretDownPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.CaretDown,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
