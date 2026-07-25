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

val MinoIcons.Upload: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Upload",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.9999f, 2.6f)
                curveTo(12.2575f, 2.6f, 12.4897f, 2.7081f, 12.6538f, 2.8815f)
                lineTo(17.1363f, 7.3641f)
                curveTo(17.4878f, 7.7155f, 17.4878f, 8.2854f, 17.1363f, 8.6369f)
                curveTo(16.7848f, 8.9883f, 16.215f, 8.9883f, 15.8635f, 8.6369f)
                lineTo(12.8999f, 5.6733f)
                verticalLineTo(15f)
                curveTo(12.8999f, 15.497f, 12.497f, 15.9f, 11.9999f, 15.9f)
                curveTo(11.5029f, 15.9f, 11.0999f, 15.497f, 11.0999f, 15f)
                verticalLineTo(5.6733f)
                lineTo(8.1364f, 8.6369f)
                curveTo(7.7849f, 8.9883f, 7.215f, 8.9883f, 6.8636f, 8.6369f)
                curveTo(6.5121f, 8.2854f, 6.5121f, 7.7155f, 6.8636f, 7.3641f)
                lineTo(11.3461f, 2.8815f)
                curveTo(11.5102f, 2.7081f, 11.7424f, 2.6f, 11.9999f, 2.6f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(21.1501f, 14.9001f)
                curveTo(21.1501f, 14.403f, 20.7472f, 14.0001f, 20.2501f, 14.0001f)
                curveTo(19.7531f, 14.0001f, 19.3502f, 14.403f, 19.3502f, 14.9001f)
                verticalLineTo(17.4502f)
                curveTo(19.3502f, 18.2647f, 19.3391f, 18.4586f, 19.2963f, 18.5901f)
                curveTo(19.1875f, 18.925f, 18.925f, 19.1875f, 18.5901f, 19.2963f)
                curveTo(18.4586f, 19.3391f, 18.2647f, 19.3502f, 17.4502f, 19.3502f)
                horizontalLineTo(6.5502f)
                curveTo(5.7356f, 19.3502f, 5.5418f, 19.3391f, 5.4103f, 19.2963f)
                curveTo(5.0754f, 19.1875f, 4.8129f, 18.925f, 4.704f, 18.5901f)
                curveTo(4.6613f, 18.4586f, 4.6502f, 18.2647f, 4.6502f, 17.4502f)
                verticalLineTo(14.9001f)
                curveTo(4.6502f, 14.403f, 4.2473f, 14.0001f, 3.7502f, 14.0001f)
                curveTo(3.2532f, 14.0001f, 2.8502f, 14.403f, 2.8502f, 14.9001f)
                verticalLineTo(17.0856f)
                curveTo(2.8502f, 17.6154f, 2.8502f, 18.0667f, 2.8804f, 18.4371f)
                curveTo(2.9122f, 18.8262f, 2.9819f, 19.2048f, 3.1663f, 19.5667f)
                curveTo(3.4443f, 20.1124f, 3.888f, 20.5561f, 4.4336f, 20.8341f)
                curveTo(4.7956f, 21.0185f, 5.1742f, 21.0881f, 5.5633f, 21.1199f)
                curveTo(5.9337f, 21.1502f, 6.3849f, 21.1502f, 6.9147f, 21.1502f)
                horizontalLineTo(17.0856f)
                curveTo(17.6154f, 21.1502f, 18.0666f, 21.1502f, 18.437f, 21.1199f)
                curveTo(18.8262f, 21.0881f, 19.2048f, 21.0185f, 19.5667f, 20.8341f)
                curveTo(20.1124f, 20.5561f, 20.556f, 20.1124f, 20.8341f, 19.5667f)
                curveTo(21.0185f, 19.2048f, 21.0881f, 18.8262f, 21.1199f, 18.4371f)
                curveTo(21.1502f, 18.0666f, 21.1501f, 17.6155f, 21.1501f, 17.0857f)
                verticalLineTo(14.9001f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun UploadPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Upload,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
