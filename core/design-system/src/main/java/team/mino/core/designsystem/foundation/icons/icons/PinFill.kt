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

val MinoIcons.PinFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.PinFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(8.9474f, 2.6019f)
                horizontalLineTo(15.053f)
                curveTo(15.4692f, 2.6019f, 15.8417f, 2.6019f, 16.1409f, 2.6294f)
                curveTo(16.4453f, 2.6573f, 16.8214f, 2.7228f, 17.1518f, 2.9611f)
                curveTo(17.5861f, 3.2744f, 17.868f, 3.7565f, 17.9281f, 4.2887f)
                curveTo(17.9738f, 4.6935f, 17.8465f, 5.0533f, 17.7215f, 5.3324f)
                curveTo(17.5987f, 5.6066f, 17.4161f, 5.9312f, 17.2119f, 6.2939f)
                lineTo(16.4002f, 7.7368f)
                verticalLineTo(10.6853f)
                lineTo(18.149f, 12.8713f)
                curveTo(18.4599f, 13.26f, 18.731f, 13.5989f, 18.9203f, 13.8851f)
                curveTo(19.1055f, 14.1653f, 19.319f, 14.5454f, 19.3195f, 14.9989f)
                curveTo(19.3201f, 15.5776f, 19.057f, 16.1249f, 18.6048f, 16.4859f)
                curveTo(18.2503f, 16.7689f, 17.8201f, 16.8396f, 17.4856f, 16.87f)
                curveTo(17.1439f, 16.901f, 16.7099f, 16.901f, 16.2121f, 16.901f)
                lineTo(12.8998f, 16.901f)
                verticalLineTo(22.001f)
                curveTo(12.8998f, 22.498f, 12.4968f, 22.901f, 11.9998f, 22.901f)
                curveTo(11.5027f, 22.901f, 11.0998f, 22.498f, 11.0998f, 22.001f)
                verticalLineTo(16.901f)
                lineTo(7.7881f, 16.901f)
                curveTo(7.2904f, 16.901f, 6.8563f, 16.901f, 6.5146f, 16.87f)
                curveTo(6.1801f, 16.8396f, 5.7499f, 16.7689f, 5.3954f, 16.4859f)
                curveTo(4.9432f, 16.1249f, 4.6801f, 15.5775f, 4.6808f, 14.9989f)
                curveTo(4.6813f, 14.5453f, 4.8948f, 14.1653f, 5.08f, 13.8851f)
                curveTo(5.2693f, 13.5988f, 5.5404f, 13.2599f, 5.8513f, 12.8713f)
                lineTo(7.6002f, 10.6853f)
                verticalLineTo(7.7368f)
                lineTo(6.7884f, 6.2939f)
                curveTo(6.5842f, 5.9311f, 6.4016f, 5.6066f, 6.2788f, 5.3324f)
                curveTo(6.1539f, 5.0533f, 6.0265f, 4.6935f, 6.0723f, 4.2887f)
                curveTo(6.1324f, 3.7565f, 6.4143f, 3.2744f, 6.8486f, 2.9611f)
                curveTo(7.179f, 2.7228f, 7.555f, 2.6573f, 7.8595f, 2.6294f)
                curveTo(8.1586f, 2.6019f, 8.5311f, 2.6019f, 8.9474f, 2.6019f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun PinFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.PinFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
