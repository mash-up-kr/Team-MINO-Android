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

val MinoIcons.AiReview: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.AiReview",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(12.0254f, 0.5f)
                curveTo(12.5005f, 0.5001f, 12.8942f, 0.8705f, 12.9238f, 1.3447f)
                lineTo(13.0059f, 2.6709f)
                curveTo(17.4124f, 3.1309f, 20.9131f, 6.635f, 21.3682f, 11.043f)
                lineTo(22.7061f, 11.127f)
                curveTo(23.1804f, 11.1566f, 23.5498f, 11.5501f, 23.5498f, 12.0254f)
                curveTo(23.5496f, 12.5005f, 23.1802f, 12.8932f, 22.7061f, 12.9229f)
                lineTo(21.3662f, 13.0059f)
                curveTo(20.907f, 17.4089f, 17.4096f, 20.9071f, 13.0068f, 21.3672f)
                lineTo(12.9238f, 22.7061f)
                curveTo(12.8941f, 23.1803f, 12.5005f, 23.5497f, 12.0254f, 23.5498f)
                curveTo(11.5502f, 23.5496f, 11.1566f, 23.1802f, 11.127f, 22.7061f)
                lineTo(11.043f, 21.3691f)
                curveTo(6.6342f, 20.9145f, 3.1296f, 17.4132f, 2.6699f, 13.0059f)
                lineTo(1.3447f, 12.9229f)
                curveTo(1.1964f, 12.9135f, 1.0583f, 12.8686f, 0.9385f, 12.7969f)
                curveTo(0.6685f, 12.6352f, 0.4926f, 12.3331f, 0.5003f, 12f)
                curveTo(0.5076f, 11.6836f, 0.6819f, 11.4067f, 0.9385f, 11.2529f)
                curveTo(1.0583f, 11.1811f, 1.1965f, 11.1363f, 1.3447f, 11.127f)
                lineTo(2.668f, 11.0439f)
                curveTo(3.123f, 6.6312f, 6.6311f, 3.1235f, 11.0439f, 2.669f)
                lineTo(11.127f, 1.3447f)
                curveTo(11.1787f, 0.8792f, 11.5389f, 0.5002f, 12.0254f, 0.5f)
                close()
                moveTo(4.499f, 13.1201f)
                curveTo(4.9806f, 16.4399f, 7.6074f, 19.062f, 10.9287f, 19.5391f)
                lineTo(10.8545f, 18.3418f)
                curveTo(10.7913f, 17.3305f, 10.7466f, 16.6227f, 10.6689f, 16.0693f)
                curveTo(10.6119f, 15.6627f, 10.5412f, 15.3781f, 10.4512f, 15.1533f)
                curveTo(10.1449f, 14.3893f, 9.4406f, 13.7416f, 8.6504f, 13.5156f)
                curveTo(7.3366f, 13.1402f, 5.8515f, 13.2053f, 4.499f, 13.1201f)
                close()
                moveTo(16.0693f, 13.3818f)
                curveTo(15.0514f, 13.5248f, 14.2007f, 14.0146f, 13.6963f, 14.9463f)
                curveTo(13.558f, 15.2018f, 13.4579f, 15.5271f, 13.3818f, 16.0693f)
                curveTo(13.3042f, 16.6228f, 13.2595f, 17.3312f, 13.1963f, 18.3428f)
                lineTo(13.1211f, 19.5381f)
                curveTo(16.4361f, 19.0563f, 19.0557f, 16.4362f, 19.5371f, 13.1211f)
                curveTo(18.3827f, 13.1938f, 17.2153f, 13.221f, 16.0693f, 13.3818f)
                close()
                moveTo(10.9297f, 4.4981f)
                curveTo(7.6043f, 4.9751f, 4.9756f, 7.6044f, 4.4981f, 10.9297f)
                lineTo(5.708f, 10.8545f)
                curveTo(6.7191f, 10.7913f, 7.4273f, 10.7465f, 7.9805f, 10.6689f)
                curveTo(8.5226f, 10.5929f, 8.849f, 10.4927f, 9.1045f, 10.3545f)
                curveTo(9.6335f, 10.068f, 10.068f, 9.6334f, 10.3545f, 9.1045f)
                curveTo(10.4928f, 8.8489f, 10.5928f, 8.5227f, 10.6689f, 7.9805f)
                curveTo(10.7466f, 7.4271f, 10.7913f, 6.7193f, 10.8545f, 5.708f)
                lineTo(10.9297f, 4.4981f)
                close()
                moveTo(13.1963f, 5.708f)
                curveTo(13.2595f, 6.7193f, 13.3042f, 7.4271f, 13.3818f, 7.9805f)
                curveTo(13.4351f, 8.3599f, 13.5119f, 8.7639f, 13.6963f, 9.1045f)
                curveTo(13.9828f, 9.6335f, 14.4173f, 10.068f, 14.9463f, 10.3545f)
                curveTo(15.2018f, 10.4928f, 15.5274f, 10.5928f, 16.0693f, 10.6689f)
                curveTo(16.6227f, 10.7466f, 17.3313f, 10.7913f, 18.3428f, 10.8545f)
                lineTo(19.5381f, 10.9287f)
                curveTo(19.0607f, 7.6082f, 16.4392f, 4.9819f, 13.1201f, 4.5f)
                lineTo(13.1963f, 5.708f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun AiReviewPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.AiReview,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
