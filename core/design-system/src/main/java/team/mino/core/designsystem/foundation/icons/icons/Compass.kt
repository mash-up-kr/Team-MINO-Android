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

val MinoIcons.Compass: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Compass",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(9.9224f, 9.6704f)
                curveTo(9.8259f, 9.7407f, 9.7411f, 9.8256f, 9.6708f, 9.922f)
                curveTo(9.5843f, 10.0405f, 9.5314f, 10.1724f, 9.5182f, 10.2053f)
                lineTo(9.5157f, 10.2114f)
                lineTo(7.6491f, 14.6625f)
                curveTo(7.5988f, 14.7822f, 7.5404f, 14.9214f, 7.5019f, 15.042f)
                curveTo(7.4703f, 15.1412f, 7.375f, 15.4399f, 7.4833f, 15.7753f)
                curveTo(7.5969f, 16.1269f, 7.8726f, 16.4026f, 8.2242f, 16.5162f)
                curveTo(8.5596f, 16.6245f, 8.8583f, 16.5293f, 8.9575f, 16.4976f)
                curveTo(9.0781f, 16.4591f, 9.2173f, 16.4007f, 9.337f, 16.3504f)
                lineTo(13.7881f, 14.4838f)
                lineTo(13.7942f, 14.4813f)
                curveTo(13.8271f, 14.4681f, 13.959f, 14.4152f, 14.0775f, 14.3288f)
                curveTo(14.1739f, 14.2585f, 14.2588f, 14.1736f, 14.3291f, 14.0772f)
                curveTo(14.4155f, 13.9586f, 14.4685f, 13.8268f, 14.4817f, 13.7939f)
                lineTo(14.4842f, 13.7877f)
                lineTo(16.3507f, 9.3367f)
                curveTo(16.401f, 9.217f, 16.4594f, 9.0778f, 16.4979f, 8.9572f)
                curveTo(16.5296f, 8.858f, 16.6248f, 8.5593f, 16.5165f, 8.2239f)
                curveTo(16.4029f, 7.8723f, 16.1272f, 7.5966f, 15.7756f, 7.4831f)
                curveTo(15.4402f, 7.3747f, 15.1415f, 7.4699f, 15.0423f, 7.5016f)
                curveTo(14.9217f, 7.5401f, 14.7826f, 7.5985f, 14.6628f, 7.6488f)
                lineTo(10.2118f, 9.5153f)
                lineTo(10.2057f, 9.5178f)
                curveTo(10.1727f, 9.531f, 10.0409f, 9.584f, 9.9224f, 9.6704f)
                close()
                moveTo(13f, 12.0002f)
                curveTo(13f, 12.5525f, 12.5522f, 13.0002f, 12f, 13.0002f)
                curveTo(11.4477f, 13.0002f, 11f, 12.5525f, 11f, 12.0002f)
                curveTo(11f, 11.4479f, 11.4477f, 11.0002f, 12f, 11.0002f)
                curveTo(12.5522f, 11.0002f, 13f, 11.4479f, 13f, 12.0002f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(12.0001f, 2.0996f)
                curveTo(6.5325f, 2.0996f, 2.1001f, 6.532f, 2.1001f, 11.9996f)
                curveTo(2.1001f, 17.4672f, 6.5325f, 21.8996f, 12.0001f, 21.8996f)
                curveTo(17.4677f, 21.8996f, 21.9001f, 17.4672f, 21.9001f, 11.9996f)
                curveTo(21.9001f, 6.532f, 17.4677f, 2.0996f, 12.0001f, 2.0996f)
                close()
                moveTo(3.9001f, 11.9996f)
                curveTo(3.9001f, 7.5261f, 7.5266f, 3.8996f, 12.0001f, 3.8996f)
                curveTo(16.4736f, 3.8996f, 20.1001f, 7.5261f, 20.1001f, 11.9996f)
                curveTo(20.1001f, 16.4731f, 16.4736f, 20.0996f, 12.0001f, 20.0996f)
                curveTo(7.5266f, 20.0996f, 3.9001f, 16.4731f, 3.9001f, 11.9996f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun CompassPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Compass,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
