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

val MinoIcons.EyeSlash: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.EyeSlash",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(15.8189f, 12.7955f)
                curveTo(15.8721f, 12.5387f, 15.9001f, 12.2727f, 15.9001f, 12.0001f)
                curveTo(15.9001f, 9.8462f, 14.154f, 8.1001f, 12.0001f, 8.1001f)
                curveTo(11.7275f, 8.1001f, 11.4615f, 8.1281f, 11.2047f, 8.1813f)
                lineTo(15.8189f, 12.7955f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(3.3638f, 4.1132f)
                curveTo(3.7152f, 3.7618f, 4.2851f, 3.7618f, 4.6365f, 4.1132f)
                lineTo(6.5721f, 6.0488f)
                curveTo(8.1716f, 5.1271f, 10.0272f, 4.5999f, 12.0058f, 4.5999f)
                curveTo(16.562f, 4.5999f, 20.4653f, 7.3954f, 22.0941f, 11.3647f)
                curveTo(22.2609f, 11.7714f, 22.2609f, 12.2278f, 22.0941f, 12.6345f)
                curveTo(21.2687f, 14.6459f, 19.8593f, 16.3559f, 18.0768f, 17.5535f)
                lineTo(19.6365f, 19.1132f)
                curveTo(19.988f, 19.4647f, 19.988f, 20.0346f, 19.6365f, 20.386f)
                curveTo(19.2851f, 20.7375f, 18.7152f, 20.7375f, 18.3638f, 20.386f)
                lineTo(16.4381f, 18.4604f)
                curveTo(15.084f, 19.0639f, 13.5841f, 19.3993f, 12.0058f, 19.3993f)
                curveTo(7.4496f, 19.3993f, 3.5463f, 16.6038f, 1.9176f, 12.6345f)
                curveTo(1.7507f, 12.2278f, 1.7507f, 11.7714f, 1.9176f, 11.3647f)
                curveTo(2.6043f, 9.6911f, 3.6954f, 8.2262f, 5.0693f, 7.0915f)
                lineTo(3.3638f, 5.386f)
                curveTo(3.0123f, 5.0346f, 3.0123f, 4.4647f, 3.3638f, 4.1132f)
                close()
                moveTo(6.349f, 8.3713f)
                curveTo(5.1521f, 9.3223f, 4.2003f, 10.5684f, 3.6034f, 11.9996f)
                curveTo(4.9748f, 15.288f, 8.2204f, 17.5993f, 12.0058f, 17.5993f)
                curveTo(13.0749f, 17.5993f, 14.101f, 17.4149f, 15.054f, 17.0762f)
                lineTo(13.5554f, 15.5777f)
                curveTo(13.0789f, 15.7851f, 12.5529f, 15.9001f, 12.0001f, 15.9001f)
                curveTo(9.8462f, 15.9001f, 8.1001f, 14.154f, 8.1001f, 12.0001f)
                curveTo(8.1001f, 11.4473f, 8.2151f, 10.9213f, 8.4226f, 10.4448f)
                lineTo(6.349f, 8.3713f)
                close()
                moveTo(16.7745f, 16.2512f)
                curveTo(18.3898f, 15.2555f, 19.6703f, 13.769f, 20.4083f, 11.9996f)
                curveTo(19.0368f, 8.7112f, 15.7912f, 6.3999f, 12.0058f, 6.3999f)
                curveTo(10.5285f, 6.3999f, 9.1335f, 6.7519f, 7.9f, 7.3766f)
                lineTo(16.7745f, 16.2512f)
                close()
                moveTo(9.9015f, 11.9237f)
                lineTo(12.0765f, 14.0988f)
                curveTo(12.0511f, 14.0997f, 12.0257f, 14.1001f, 12.0001f, 14.1001f)
                curveTo(10.8403f, 14.1001f, 9.9001f, 13.1599f, 9.9001f, 12.0001f)
                curveTo(9.9001f, 11.9746f, 9.9006f, 11.9491f, 9.9015f, 11.9237f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun EyeSlashPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.EyeSlash,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
