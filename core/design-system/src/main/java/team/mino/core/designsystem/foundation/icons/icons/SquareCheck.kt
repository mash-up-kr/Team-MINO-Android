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

val MinoIcons.SquareCheck: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.SquareCheck",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(16.6463f, 9.8759f)
                curveTo(16.992f, 9.5188f, 16.9827f, 8.949f, 16.6255f, 8.6033f)
                curveTo(16.2684f, 8.2576f, 15.6986f, 8.2669f, 15.3529f, 8.6241f)
                lineTo(10.677f, 13.455f)
                lineTo(8.6471f, 11.3522f)
                curveTo(8.3019f, 10.9946f, 7.7322f, 10.9845f, 7.3746f, 11.3297f)
                curveTo(7.0169f, 11.675f, 7.0069f, 12.2447f, 7.3521f, 12.6023f)
                lineTo(10.0285f, 15.3751f)
                curveTo(10.198f, 15.5506f, 10.4315f, 15.6498f, 10.6755f, 15.65f)
                curveTo(10.9195f, 15.6502f, 11.1531f, 15.5513f, 11.3228f, 15.3759f)
                lineTo(16.6463f, 9.8759f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(8.2624f, 2.6f)
                curveTo(7.4543f, 2.6f, 6.7935f, 2.6f, 6.2564f, 2.6439f)
                curveTo(5.7005f, 2.6893f, 5.1984f, 2.7862f, 4.7295f, 3.0251f)
                curveTo(3.9957f, 3.399f, 3.3991f, 3.9956f, 3.0252f, 4.7294f)
                curveTo(2.7863f, 5.1983f, 2.6894f, 5.7004f, 2.644f, 6.2563f)
                curveTo(2.6001f, 6.7934f, 2.6001f, 7.4542f, 2.6001f, 8.2623f)
                verticalLineTo(15.7377f)
                curveTo(2.6001f, 16.5458f, 2.6001f, 17.2066f, 2.644f, 17.7437f)
                curveTo(2.6894f, 18.2996f, 2.7863f, 18.8017f, 3.0252f, 19.2706f)
                curveTo(3.3991f, 20.0044f, 3.9957f, 20.601f, 4.7295f, 20.9749f)
                curveTo(5.1984f, 21.2138f, 5.7005f, 21.3107f, 6.2564f, 21.3561f)
                curveTo(6.7935f, 21.4f, 7.4543f, 21.4f, 8.2624f, 21.4f)
                horizontalLineTo(15.7378f)
                curveTo(16.5459f, 21.4f, 17.2067f, 21.4f, 17.7438f, 21.3561f)
                curveTo(18.2997f, 21.3107f, 18.8018f, 21.2138f, 19.2707f, 20.9749f)
                curveTo(20.0045f, 20.601f, 20.6011f, 20.0044f, 20.975f, 19.2706f)
                curveTo(21.2139f, 18.8017f, 21.3108f, 18.2996f, 21.3562f, 17.7437f)
                curveTo(21.4001f, 17.2066f, 21.4001f, 16.5458f, 21.4001f, 15.7377f)
                verticalLineTo(8.2623f)
                curveTo(21.4001f, 7.4543f, 21.4001f, 6.7934f, 21.3562f, 6.2563f)
                curveTo(21.3108f, 5.7004f, 21.2139f, 5.1983f, 20.975f, 4.7294f)
                curveTo(20.6011f, 3.9956f, 20.0045f, 3.399f, 19.2707f, 3.0251f)
                curveTo(18.8018f, 2.7862f, 18.2997f, 2.6893f, 17.7438f, 2.6439f)
                curveTo(17.2067f, 2.6f, 16.5459f, 2.6f, 15.7378f, 2.6f)
                horizontalLineTo(8.2624f)
                close()
                moveTo(5.5467f, 4.6289f)
                curveTo(5.7196f, 4.5408f, 5.9584f, 4.4742f, 6.403f, 4.4379f)
                curveTo(6.8581f, 4.4007f, 7.4452f, 4.4f, 8.3001f, 4.4f)
                horizontalLineTo(15.7001f)
                curveTo(16.555f, 4.4f, 17.1421f, 4.4007f, 17.5972f, 4.4379f)
                curveTo(18.0418f, 4.4742f, 18.2806f, 4.5408f, 18.4535f, 4.6289f)
                curveTo(18.8486f, 4.8302f, 19.1699f, 5.1515f, 19.3712f, 5.5466f)
                curveTo(19.4593f, 5.7195f, 19.5259f, 5.9583f, 19.5622f, 6.4029f)
                curveTo(19.5994f, 6.858f, 19.6001f, 7.4451f, 19.6001f, 8.3f)
                verticalLineTo(15.7f)
                curveTo(19.6001f, 16.5549f, 19.5994f, 17.142f, 19.5622f, 17.5971f)
                curveTo(19.5259f, 18.0417f, 19.4593f, 18.2805f, 19.3712f, 18.4534f)
                curveTo(19.1699f, 18.8485f, 18.8486f, 19.1698f, 18.4535f, 19.3711f)
                curveTo(18.2806f, 19.4592f, 18.0418f, 19.5258f, 17.5972f, 19.5621f)
                curveTo(17.1421f, 19.5993f, 16.555f, 19.6f, 15.7001f, 19.6f)
                horizontalLineTo(8.3001f)
                curveTo(7.4452f, 19.6f, 6.8581f, 19.5993f, 6.403f, 19.5621f)
                curveTo(5.9584f, 19.5258f, 5.7196f, 19.4592f, 5.5467f, 19.3711f)
                curveTo(5.1516f, 19.1698f, 4.8303f, 18.8485f, 4.629f, 18.4534f)
                curveTo(4.5409f, 18.2805f, 4.4743f, 18.0417f, 4.438f, 17.5971f)
                curveTo(4.4008f, 17.142f, 4.4001f, 16.5549f, 4.4001f, 15.7f)
                verticalLineTo(8.3f)
                curveTo(4.4001f, 7.4451f, 4.4008f, 6.858f, 4.438f, 6.4029f)
                curveTo(4.4743f, 5.9583f, 4.5409f, 5.7195f, 4.629f, 5.5466f)
                curveTo(4.8303f, 5.1515f, 5.1516f, 4.8302f, 5.5467f, 4.6289f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun SquareCheckPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.SquareCheck,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
