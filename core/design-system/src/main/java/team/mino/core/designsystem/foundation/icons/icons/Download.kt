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

val MinoIcons.Download: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Download",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(12f, 2.5996f)
                curveTo(12.497f, 2.5996f, 12.9f, 3.0025f, 12.9f, 3.4996f)
                verticalLineTo(12.8273f)
                lineTo(15.8636f, 9.8637f)
                curveTo(16.215f, 9.5122f, 16.7849f, 9.5122f, 17.1363f, 9.8637f)
                curveTo(17.4878f, 10.2152f, 17.4878f, 10.785f, 17.1363f, 11.1365f)
                lineTo(12.6364f, 15.6365f)
                curveTo(12.2849f, 15.988f, 11.715f, 15.988f, 11.3636f, 15.6365f)
                lineTo(6.8636f, 11.1365f)
                curveTo(6.5121f, 10.785f, 6.5121f, 10.2152f, 6.8636f, 9.8637f)
                curveTo(7.215f, 9.5122f, 7.7849f, 9.5122f, 8.1364f, 9.8637f)
                lineTo(11.1f, 12.8273f)
                verticalLineTo(3.4996f)
                curveTo(11.1f, 3.0025f, 11.5029f, 2.5996f, 12f, 2.5996f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(21.1502f, 14.8997f)
                curveTo(21.1502f, 14.4026f, 20.7472f, 13.9997f, 20.2502f, 13.9997f)
                curveTo(19.7531f, 13.9997f, 19.3502f, 14.4026f, 19.3502f, 14.8997f)
                verticalLineTo(17.4498f)
                curveTo(19.3502f, 18.2644f, 19.3391f, 18.4582f, 19.2963f, 18.5897f)
                curveTo(19.1875f, 18.9246f, 18.925f, 19.1872f, 18.5901f, 19.296f)
                curveTo(18.4586f, 19.3387f, 18.2647f, 19.3498f, 17.4502f, 19.3498f)
                horizontalLineTo(6.5502f)
                curveTo(5.7357f, 19.3498f, 5.5418f, 19.3387f, 5.4103f, 19.296f)
                curveTo(5.0754f, 19.1872f, 4.8129f, 18.9246f, 4.7041f, 18.5897f)
                curveTo(4.6613f, 18.4582f, 4.6502f, 18.2644f, 4.6502f, 17.4498f)
                verticalLineTo(14.8997f)
                curveTo(4.6502f, 14.4027f, 4.2473f, 13.9997f, 3.7502f, 13.9997f)
                curveTo(3.2532f, 13.9997f, 2.8502f, 14.4027f, 2.8502f, 14.8997f)
                verticalLineTo(17.0852f)
                curveTo(2.8502f, 17.615f, 2.8502f, 18.0663f, 2.8805f, 18.4367f)
                curveTo(2.9122f, 18.8259f, 2.9819f, 19.2044f, 3.1663f, 19.5664f)
                curveTo(3.4443f, 20.112f, 3.888f, 20.5557f, 4.4336f, 20.8337f)
                curveTo(4.7956f, 21.0181f, 5.1742f, 21.0878f, 5.5633f, 21.1196f)
                curveTo(5.9337f, 21.1498f, 6.3849f, 21.1498f, 6.9147f, 21.1498f)
                horizontalLineTo(17.0856f)
                curveTo(17.6154f, 21.1498f, 18.0666f, 21.1498f, 18.4371f, 21.1196f)
                curveTo(18.8262f, 21.0878f, 19.2048f, 21.0181f, 19.5667f, 20.8337f)
                curveTo(20.1124f, 20.5557f, 20.556f, 20.112f, 20.8341f, 19.5664f)
                curveTo(21.0185f, 19.2044f, 21.0881f, 18.8259f, 21.1199f, 18.4367f)
                curveTo(21.1502f, 18.0663f, 21.1502f, 17.6151f, 21.1502f, 17.0853f)
                verticalLineTo(14.8997f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun DownloadPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Download,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
