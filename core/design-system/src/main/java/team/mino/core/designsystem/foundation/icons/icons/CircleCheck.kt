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

val MinoIcons.CircleCheck: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.CircleCheck",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(16.6466f, 9.8759f)
                curveTo(16.9923f, 9.5188f, 16.983f, 8.949f, 16.6258f, 8.6033f)
                curveTo(16.2687f, 8.2576f, 15.6989f, 8.2669f, 15.3532f, 8.6241f)
                lineTo(10.6773f, 13.455f)
                lineTo(8.6475f, 11.3522f)
                curveTo(8.3023f, 10.9946f, 7.7325f, 10.9845f, 7.3749f, 11.3297f)
                curveTo(7.0172f, 11.6749f, 7.0072f, 12.2447f, 7.3524f, 12.6023f)
                lineTo(10.0288f, 15.3751f)
                curveTo(10.1983f, 15.5506f, 10.4318f, 15.6498f, 10.6758f, 15.65f)
                curveTo(10.9198f, 15.6502f, 11.1534f, 15.5513f, 11.3231f, 15.3759f)
                lineTo(16.6466f, 9.8759f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(11.9999f, 2.1f)
                curveTo(6.5323f, 2.1f, 2.1f, 6.5324f, 2.1f, 12f)
                curveTo(2.1f, 17.4676f, 6.5323f, 21.9f, 11.9999f, 21.9f)
                curveTo(17.4675f, 21.9f, 21.8999f, 17.4676f, 21.8999f, 12f)
                curveTo(21.8999f, 6.5324f, 17.4675f, 2.1f, 11.9999f, 2.1f)
                close()
                moveTo(3.8999f, 12f)
                curveTo(3.8999f, 7.5265f, 7.5264f, 3.9f, 11.9999f, 3.9f)
                curveTo(16.4734f, 3.9f, 20.0999f, 7.5265f, 20.0999f, 12f)
                curveTo(20.0999f, 16.4735f, 16.4734f, 20.1f, 11.9999f, 20.1f)
                curveTo(7.5264f, 20.1f, 3.8999f, 16.4735f, 3.8999f, 12f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun CircleCheckPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.CircleCheck,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
