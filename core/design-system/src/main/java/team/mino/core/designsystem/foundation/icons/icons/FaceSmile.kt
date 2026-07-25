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

val MinoIcons.FaceSmile: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.FaceSmile",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(8.3466f, 13.489f)
                curveTo(8.7669f, 13.2235f, 9.3227f, 13.349f, 9.5882f, 13.7692f)
                curveTo(10.0945f, 14.5708f, 10.9858f, 15.0999f, 12f, 15.0999f)
                curveTo(13.0142f, 15.0999f, 13.9055f, 14.5708f, 14.4118f, 13.7692f)
                curveTo(14.6773f, 13.349f, 15.2331f, 13.2235f, 15.6534f, 13.489f)
                curveTo(16.0736f, 13.7545f, 16.1991f, 14.3103f, 15.9336f, 14.7306f)
                curveTo(15.1114f, 16.0322f, 13.6573f, 16.8999f, 12f, 16.8999f)
                curveTo(10.3427f, 16.8999f, 8.8886f, 16.0322f, 8.0664f, 14.7306f)
                curveTo(7.8009f, 14.3103f, 7.9264f, 13.7545f, 8.3466f, 13.489f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(10.2499f, 10.2499f)
                curveTo(10.2499f, 10.9403f, 9.6903f, 11.4999f, 8.9999f, 11.4999f)
                curveTo(8.3095f, 11.4999f, 7.7499f, 10.9403f, 7.7499f, 10.2499f)
                curveTo(7.7499f, 9.5595f, 8.3095f, 8.9999f, 8.9999f, 8.9999f)
                curveTo(9.6903f, 8.9999f, 10.2499f, 9.5595f, 10.2499f, 10.2499f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(16.2499f, 10.2499f)
                curveTo(16.2499f, 10.9403f, 15.6902f, 11.4999f, 14.9999f, 11.4999f)
                curveTo(14.3095f, 11.4999f, 13.7499f, 10.9403f, 13.7499f, 10.2499f)
                curveTo(13.7499f, 9.5595f, 14.3095f, 8.9999f, 14.9999f, 8.9999f)
                curveTo(15.6902f, 8.9999f, 16.2499f, 9.5595f, 16.2499f, 10.2499f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(2.1f, 12f)
                curveTo(2.1f, 6.5324f, 6.5323f, 2.1f, 11.9999f, 2.1f)
                curveTo(17.4675f, 2.1f, 21.8999f, 6.5324f, 21.8999f, 12f)
                curveTo(21.8999f, 17.4676f, 17.4675f, 21.9f, 11.9999f, 21.9f)
                curveTo(6.5323f, 21.9f, 2.1f, 17.4676f, 2.1f, 12f)
                close()
                moveTo(11.9999f, 3.9f)
                curveTo(7.5264f, 3.9f, 3.9f, 7.5265f, 3.9f, 12f)
                curveTo(3.9f, 16.4735f, 7.5264f, 20.1f, 11.9999f, 20.1f)
                curveTo(16.4734f, 20.1f, 20.0999f, 16.4735f, 20.0999f, 12f)
                curveTo(20.0999f, 7.5265f, 16.4734f, 3.9f, 11.9999f, 3.9f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun FaceSmilePreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.FaceSmile,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
