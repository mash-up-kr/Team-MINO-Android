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

val MinoIcons.CircleQuestionFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.CircleQuestionFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(2.0999f, 12.0001f)
                curveTo(2.0999f, 6.5325f, 6.5323f, 2.1001f, 11.9999f, 2.1001f)
                curveTo(17.4675f, 2.1001f, 21.8998f, 6.5325f, 21.8998f, 12.0001f)
                curveTo(21.8998f, 17.4677f, 17.4675f, 21.9001f, 11.9999f, 21.9001f)
                curveTo(6.5323f, 21.9001f, 2.0999f, 17.4677f, 2.0999f, 12.0001f)
                close()
                moveTo(11.1098f, 13.1165f)
                curveTo(11.0128f, 13.6409f, 11.4601f, 14.08f, 11.9934f, 14.08f)
                curveTo(12.5259f, 14.08f, 12.9298f, 13.6328f, 13.1103f, 13.1319f)
                curveTo(13.3451f, 12.4807f, 13.8168f, 12.0841f, 14.2873f, 11.6886f)
                curveTo(14.9461f, 11.1348f, 15.6024f, 10.5831f, 15.6024f, 9.3375f)
                curveTo(15.6024f, 7.4147f, 14.0444f, 6.2823f, 12.0608f, 6.2823f)
                curveTo(10.2581f, 6.2823f, 8.8985f, 7.2326f, 8.5011f, 8.7753f)
                curveTo(8.3681f, 9.2919f, 8.8158f, 9.7327f, 9.3493f, 9.7327f)
                curveTo(9.8814f, 9.7327f, 10.2723f, 9.2724f, 10.519f, 8.8009f)
                curveTo(10.7842f, 8.2941f, 11.2942f, 8.0049f, 11.9848f, 7.9999f)
                curveTo(12.9348f, 8.0075f, 13.596f, 8.5547f, 13.596f, 9.4135f)
                curveTo(13.596f, 10.1105f, 13.1589f, 10.4457f, 12.6454f, 10.8397f)
                curveTo(12.0476f, 11.2982f, 11.3463f, 11.8362f, 11.1098f, 13.1165f)
                close()
                moveTo(10.7688f, 16.4664f)
                curveTo(10.7611f, 17.1504f, 11.278f, 17.652f, 12f, 17.652f)
                curveTo(12.7068f, 17.652f, 13.2236f, 17.1504f, 13.2312f, 16.4664f)
                curveTo(13.2236f, 15.7824f, 12.7068f, 15.2808f, 12f, 15.2808f)
                curveTo(11.278f, 15.2808f, 10.7611f, 15.7824f, 10.7688f, 16.4664f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun CircleQuestionFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.CircleQuestionFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
