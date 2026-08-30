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

val MinoIcons.FaceSmileFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.FaceSmileFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(2.1f, 12f)
                curveTo(2.1f, 6.5324f, 6.5324f, 2.1f, 12f, 2.1f)
                curveTo(17.4676f, 2.1f, 21.8999f, 6.5324f, 21.8999f, 12f)
                curveTo(21.8999f, 17.4676f, 17.4676f, 21.9f, 12f, 21.9f)
                curveTo(6.5324f, 21.9f, 2.1f, 17.4676f, 2.1f, 12f)
                close()
                moveTo(9.9999f, 10.2499f)
                curveTo(9.9999f, 10.9402f, 9.4402f, 11.4999f, 8.7499f, 11.4999f)
                curveTo(8.0595f, 11.4999f, 7.4999f, 10.9402f, 7.4999f, 10.2499f)
                curveTo(7.4999f, 9.5595f, 8.0595f, 8.9999f, 8.7499f, 8.9999f)
                curveTo(9.4402f, 8.9999f, 9.9999f, 9.5595f, 9.9999f, 10.2499f)
                close()
                moveTo(16.4998f, 10.2499f)
                curveTo(16.4998f, 10.9402f, 15.9402f, 11.4999f, 15.2498f, 11.4999f)
                curveTo(14.5595f, 11.4999f, 13.9999f, 10.9402f, 13.9999f, 10.2499f)
                curveTo(13.9999f, 9.5595f, 14.5595f, 8.9999f, 15.2498f, 8.9999f)
                curveTo(15.9402f, 8.9999f, 16.4998f, 9.5595f, 16.4998f, 10.2499f)
                close()
                moveTo(9.3142f, 13.7992f)
                curveTo(9.0653f, 13.369f, 8.5147f, 13.2219f, 8.0845f, 13.4708f)
                curveTo(7.6542f, 13.7197f, 7.5072f, 14.2703f, 7.7561f, 14.7005f)
                curveTo(8.602f, 16.1628f, 10.1852f, 17.1499f, 12f, 17.1499f)
                curveTo(13.8148f, 17.1499f, 15.398f, 16.1628f, 16.2439f, 14.7005f)
                curveTo(16.4928f, 14.2703f, 16.3458f, 13.7197f, 15.9155f, 13.4708f)
                curveTo(15.4852f, 13.2219f, 14.9347f, 13.369f, 14.6858f, 13.7992f)
                curveTo(14.1484f, 14.7282f, 13.1463f, 15.3499f, 12f, 15.3499f)
                curveTo(10.8537f, 15.3499f, 9.8515f, 14.7282f, 9.3142f, 13.7992f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun FaceSmileFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.FaceSmileFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
