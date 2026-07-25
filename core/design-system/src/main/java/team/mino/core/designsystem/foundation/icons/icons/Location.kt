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

val MinoIcons.Location: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Location",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(16.4026f, 18.8795f)
                curveTo(15.4297f, 19.9048f, 14.4598f, 20.7425f, 13.7342f, 21.3235f)
                curveTo(13.3346f, 21.6434f, 12.9279f, 21.958f, 12.504f, 22.2455f)
                curveTo(12.2035f, 22.4482f, 11.794f, 22.4474f, 11.4938f, 22.2443f)
                curveTo(11.0706f, 21.957f, 10.6644f, 21.643f, 10.2654f, 21.3235f)
                curveTo(9.5398f, 20.7425f, 8.57f, 19.9048f, 7.597f, 18.8795f)
                curveTo(5.6909f, 16.8708f, 3.5998f, 13.9623f, 3.5998f, 10.7495f)
                curveTo(3.5998f, 6.1103f, 7.3606f, 2.3495f, 11.9998f, 2.3495f)
                curveTo(16.639f, 2.3495f, 20.3998f, 6.1103f, 20.3998f, 10.7495f)
                curveTo(20.3998f, 13.9623f, 18.3087f, 16.8708f, 16.4026f, 18.8795f)
                close()
                moveTo(11.9998f, 4.1493f)
                curveTo(8.3547f, 4.1493f, 5.3998f, 7.1042f, 5.3998f, 10.7493f)
                curveTo(5.3998f, 13.2304f, 7.0587f, 15.6971f, 8.9027f, 17.6402f)
                curveTo(10.1141f, 18.9169f, 11.3359f, 19.8934f, 11.9998f, 20.3905f)
                curveTo(12.6638f, 19.8934f, 13.8855f, 18.9169f, 15.097f, 17.6402f)
                curveTo(16.9409f, 15.6971f, 18.5998f, 13.2304f, 18.5998f, 10.7493f)
                curveTo(18.5998f, 7.1042f, 15.6449f, 4.1493f, 11.9998f, 4.1493f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(11.9999f, 7.0995f)
                curveTo(9.9841f, 7.0995f, 8.35f, 8.7337f, 8.35f, 10.7495f)
                curveTo(8.35f, 12.7654f, 9.9841f, 14.3995f, 11.9999f, 14.3995f)
                curveTo(14.0158f, 14.3995f, 15.6499f, 12.7654f, 15.6499f, 10.7495f)
                curveTo(15.6499f, 8.7337f, 14.0158f, 7.0995f, 11.9999f, 7.0995f)
                close()
                moveTo(10.15f, 10.7495f)
                curveTo(10.15f, 9.7278f, 10.9782f, 8.8995f, 11.9999f, 8.8995f)
                curveTo(13.0217f, 8.8995f, 13.8499f, 9.7278f, 13.8499f, 10.7495f)
                curveTo(13.8499f, 11.7712f, 13.0217f, 12.5995f, 11.9999f, 12.5995f)
                curveTo(10.9782f, 12.5995f, 10.15f, 11.7712f, 10.15f, 10.7495f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun LocationPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Location,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
