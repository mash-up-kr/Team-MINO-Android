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

val MinoIcons.Bubble: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Bubble",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(12.0002f, 4.1501f)
                curveTo(7.6647f, 4.1501f, 4.1502f, 7.6646f, 4.1502f, 12.0001f)
                curveTo(4.1502f, 16.3355f, 7.6647f, 19.8501f, 12.0002f, 19.8501f)
                curveTo(13.3614f, 19.8501f, 14.6393f, 19.5043f, 15.7534f, 18.8966f)
                curveTo(15.9575f, 18.7852f, 16.1968f, 18.7572f, 16.4212f, 18.8184f)
                lineTo(18.5356f, 19.395f)
                curveTo(18.9954f, 19.5204f, 19.2802f, 19.5972f, 19.4893f, 19.6341f)
                curveTo(19.5637f, 19.6472f, 19.6108f, 19.6516f, 19.6372f, 19.6531f)
                curveTo(19.6431f, 19.6484f, 19.6485f, 19.643f, 19.6532f, 19.6371f)
                curveTo(19.6517f, 19.6107f, 19.6473f, 19.5636f, 19.6342f, 19.4892f)
                curveTo(19.5973f, 19.2801f, 19.5205f, 18.9953f, 19.3951f, 18.5355f)
                lineTo(18.8185f, 16.4211f)
                curveTo(18.7573f, 16.1967f, 18.7853f, 15.9574f, 18.8967f, 15.7533f)
                curveTo(19.5044f, 14.6392f, 19.8501f, 13.3613f, 19.8501f, 12.0001f)
                curveTo(19.8501f, 7.6646f, 16.3356f, 4.1501f, 12.0002f, 4.1501f)
                close()
                moveTo(2.3502f, 12.0001f)
                curveTo(2.3502f, 6.6705f, 6.6707f, 2.35f, 12.0002f, 2.35f)
                curveTo(17.3297f, 2.35f, 21.6501f, 6.6705f, 21.6501f, 12.0001f)
                curveTo(21.6501f, 13.5384f, 21.2895f, 14.9948f, 20.6477f, 16.2872f)
                lineTo(21.142f, 18.0998f)
                curveTo(21.2539f, 18.5099f, 21.3537f, 18.8757f, 21.4068f, 19.1763f)
                curveTo(21.4605f, 19.4808f, 21.4972f, 19.8669f, 21.3499f, 20.2526f)
                curveTo(21.157f, 20.7578f, 20.7579f, 21.1569f, 20.2527f, 21.3498f)
                curveTo(19.867f, 21.4971f, 19.4809f, 21.4605f, 19.1764f, 21.4067f)
                curveTo(18.8758f, 21.3536f, 18.51f, 21.2538f, 18.0999f, 21.1419f)
                lineTo(16.2873f, 20.6476f)
                curveTo(14.9949f, 21.2894f, 13.5385f, 21.6501f, 12.0002f, 21.6501f)
                curveTo(6.6707f, 21.6501f, 2.3502f, 17.3296f, 2.3502f, 12.0001f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun BubblePreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Bubble,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
