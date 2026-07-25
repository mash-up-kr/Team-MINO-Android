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

val MinoIcons.SendFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.SendFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(7.9445f, 3.4082f)
                lineTo(20.2247f, 9.8007f)
                curveTo(20.6352f, 10.0144f, 20.997f, 10.2027f, 21.2706f, 10.3785f)
                curveTo(21.5428f, 10.5534f, 21.8723f, 10.8026f, 22.0561f, 11.1967f)
                curveTo(22.2934f, 11.7057f, 22.2934f, 12.2936f, 22.0561f, 12.8026f)
                curveTo(21.8723f, 13.1967f, 21.5428f, 13.4459f, 21.2706f, 13.6208f)
                curveTo(20.997f, 13.7966f, 20.6352f, 13.9849f, 20.2247f, 14.1986f)
                lineTo(7.9445f, 20.591f)
                curveTo(7.4538f, 20.8465f, 7.0322f, 21.066f, 6.6891f, 21.2074f)
                curveTo(6.3618f, 21.3423f, 5.9155f, 21.4929f, 5.4463f, 21.388f)
                curveTo(4.8589f, 21.2567f, 4.3684f, 20.8548f, 4.1242f, 20.3046f)
                curveTo(3.9291f, 19.8652f, 3.989f, 19.398f, 4.0569f, 19.0506f)
                curveTo(4.1281f, 18.6863f, 4.2604f, 18.2298f, 4.4145f, 17.6985f)
                lineTo(5.8052f, 12.8996f)
                horizontalLineTo(12.9999f)
                curveTo(13.4969f, 12.8996f, 13.8999f, 12.4967f, 13.8999f, 11.9996f)
                curveTo(13.8999f, 11.5026f, 13.4969f, 11.0996f, 12.9999f, 11.0996f)
                horizontalLineTo(5.8052f)
                lineTo(4.4145f, 6.3008f)
                curveTo(4.2604f, 5.7695f, 4.1281f, 5.313f, 4.0569f, 4.9487f)
                curveTo(3.989f, 4.6013f, 3.9291f, 4.1341f, 4.1242f, 3.6947f)
                curveTo(4.3684f, 3.1445f, 4.8589f, 2.7426f, 5.4463f, 2.6113f)
                curveTo(5.9155f, 2.5064f, 6.3618f, 2.657f, 6.6891f, 2.7919f)
                curveTo(7.0323f, 2.9333f, 7.4538f, 3.1528f, 7.9445f, 3.4082f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun SendFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.SendFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
