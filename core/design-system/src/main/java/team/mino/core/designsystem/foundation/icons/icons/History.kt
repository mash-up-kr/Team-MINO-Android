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

val MinoIcons.History: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.History",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.9997f, 2.1f)
                curveTo(11.5027f, 2.1f, 11.0997f, 2.5029f, 11.0997f, 3f)
                curveTo(11.0997f, 3.4971f, 11.5027f, 3.9f, 11.9997f, 3.9f)
                curveTo(16.4732f, 3.9f, 20.0997f, 7.5265f, 20.0997f, 12f)
                curveTo(20.0997f, 16.4735f, 16.4732f, 20.1f, 11.9997f, 20.1f)
                curveTo(7.5263f, 20.1f, 3.8998f, 16.4735f, 3.8998f, 12f)
                curveTo(3.8998f, 11.5029f, 3.4968f, 11.1f, 2.9998f, 11.1f)
                curveTo(2.5027f, 11.1f, 2.0998f, 11.5029f, 2.0998f, 12f)
                curveTo(2.0998f, 17.4676f, 6.5321f, 21.9f, 11.9997f, 21.9f)
                curveTo(17.4673f, 21.9f, 21.8997f, 17.4676f, 21.8997f, 12f)
                curveTo(21.8997f, 6.5324f, 17.4673f, 2.1f, 11.9997f, 2.1f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(9.4784f, 3.2998f)
                curveTo(9.6899f, 3.81f, 9.4479f, 4.3951f, 8.9377f, 4.6066f)
                curveTo(8.4276f, 4.8182f, 7.8425f, 4.5761f, 7.6309f, 4.0659f)
                curveTo(7.4194f, 3.5558f, 7.6614f, 2.9707f, 8.1716f, 2.7592f)
                curveTo(8.6818f, 2.5476f, 9.2668f, 2.7897f, 9.4784f, 3.2998f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(6.3429f, 4.9289f)
                curveTo(6.7334f, 5.3194f, 6.7334f, 5.9526f, 6.3429f, 6.3431f)
                curveTo(5.9524f, 6.7336f, 5.3192f, 6.7336f, 4.9287f, 6.3431f)
                curveTo(4.5382f, 5.9526f, 4.5382f, 5.3194f, 4.9287f, 4.9289f)
                curveTo(5.3192f, 4.5384f, 5.9524f, 4.5384f, 6.3429f, 4.9289f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(4.0657f, 7.6311f)
                curveTo(4.5759f, 7.8427f, 4.818f, 8.4277f, 4.6064f, 8.9379f)
                curveTo(4.3949f, 9.4481f, 3.8098f, 9.6901f, 3.2996f, 9.4786f)
                curveTo(2.7895f, 9.267f, 2.5474f, 8.682f, 2.759f, 8.1718f)
                curveTo(2.9705f, 7.6617f, 3.5556f, 7.4196f, 4.0657f, 7.6311f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.4997f, 6.6f)
                curveTo(11.9968f, 6.6f, 12.3997f, 7.0029f, 12.3997f, 7.5f)
                verticalLineTo(12.1273f)
                lineTo(14.6109f, 14.3385f)
                curveTo(14.9624f, 14.6899f, 14.9624f, 15.2598f, 14.6109f, 15.6113f)
                curveTo(14.2594f, 15.9627f, 13.6896f, 15.9627f, 13.3381f, 15.6113f)
                lineTo(10.8632f, 13.1364f)
                curveTo(10.6837f, 12.9569f, 10.5959f, 12.7204f, 10.5998f, 12.4851f)
                verticalLineTo(7.5f)
                curveTo(10.5998f, 7.0029f, 11.0027f, 6.6f, 11.4997f, 6.6f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun HistoryPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.History,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
