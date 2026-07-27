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

val MinoIcons.CircleQuestion: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.CircleQuestion",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.1897f, 13.0147f)
                curveTo(11.1028f, 13.4916f, 11.5093f, 13.8907f, 11.9941f, 13.8907f)
                curveTo(12.4782f, 13.8907f, 12.8454f, 13.4844f, 13.0071f, 13.0282f)
                curveTo(13.2204f, 12.4263f, 13.6531f, 12.0646f, 14.0839f, 11.7045f)
                curveTo(14.6812f, 11.2052f, 15.275f, 10.7089f, 15.275f, 9.5793f)
                curveTo(15.275f, 7.8313f, 13.8586f, 6.8019f, 12.0553f, 6.8019f)
                curveTo(10.4165f, 6.8019f, 9.1805f, 7.6658f, 8.8193f, 9.0682f)
                curveTo(8.6983f, 9.5379f, 9.1053f, 9.9386f, 9.5903f, 9.9386f)
                curveTo(10.0741f, 9.9386f, 10.4294f, 9.5201f, 10.6537f, 9.0915f)
                curveTo(10.8948f, 8.6308f, 11.3584f, 8.3679f, 11.9862f, 8.3633f)
                curveTo(12.8499f, 8.3702f, 13.451f, 8.8677f, 13.451f, 9.6484f)
                curveTo(13.451f, 10.2807f, 13.0553f, 10.5817f, 12.5898f, 10.9359f)
                curveTo(12.0449f, 11.3504f, 11.4041f, 11.8379f, 11.1897f, 13.0147f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(10.8807f, 16.0602f)
                curveTo(10.8738f, 16.682f, 11.3437f, 17.138f, 12f, 17.138f)
                curveTo(12.6426f, 17.138f, 13.1124f, 16.682f, 13.1193f, 16.0602f)
                curveTo(13.1124f, 15.4383f, 12.6426f, 14.9823f, 12f, 14.9823f)
                curveTo(11.3437f, 14.9823f, 10.8738f, 15.4383f, 10.8807f, 16.0602f)
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
private fun CircleQuestionPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.CircleQuestion,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
