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

val MinoIcons.LogoYoutube: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.LogoYoutube",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(20.3175f, 4.9925f)
                curveTo(21.2334f, 5.2374f, 21.9576f, 5.9616f, 22.2026f, 6.8775f)
                curveTo(22.6499f, 8.5389f, 22.6499f, 12.0002f, 22.6499f, 12.0002f)
                curveTo(22.6499f, 12.0002f, 22.6499f, 15.4614f, 22.2026f, 17.1228f)
                curveTo(21.9576f, 18.0387f, 21.2334f, 18.7629f, 20.3175f, 19.0079f)
                curveTo(18.6562f, 19.4552f, 11.9999f, 19.4552f, 11.9999f, 19.4552f)
                curveTo(11.9999f, 19.4552f, 5.3437f, 19.4552f, 3.6823f, 19.0079f)
                curveTo(2.7664f, 18.7629f, 2.0422f, 18.0387f, 1.7973f, 17.1228f)
                curveTo(1.35f, 15.4614f, 1.35f, 12.0002f, 1.35f, 12.0002f)
                curveTo(1.35f, 12.0002f, 1.35f, 8.5389f, 1.7973f, 6.8775f)
                curveTo(2.0422f, 5.9616f, 2.7664f, 5.2374f, 3.6823f, 4.9925f)
                curveTo(5.3437f, 4.5452f, 11.9999f, 4.5452f, 11.9999f, 4.5452f)
                curveTo(11.9999f, 4.5452f, 18.6562f, 4.5452f, 20.3175f, 4.9925f)
                close()
                moveTo(9.87f, 8.8054f)
                verticalLineTo(15.1954f)
                lineTo(15.4079f, 12.0004f)
                lineTo(9.87f, 8.8054f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun LogoYoutubePreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.LogoYoutube,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
