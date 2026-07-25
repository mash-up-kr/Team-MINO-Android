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

val MinoIcons.LocationFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.LocationFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(13.7342f, 21.3234f)
                curveTo(14.4597f, 20.7425f, 15.4296f, 19.9048f, 16.4026f, 18.8794f)
                curveTo(18.3086f, 16.8708f, 20.3997f, 13.9623f, 20.3997f, 10.7495f)
                curveTo(20.3997f, 6.1103f, 16.6389f, 2.3495f, 11.9998f, 2.3495f)
                curveTo(7.3606f, 2.3495f, 3.5998f, 6.1103f, 3.5998f, 10.7495f)
                curveTo(3.5998f, 13.9623f, 5.6909f, 16.8708f, 7.5969f, 18.8794f)
                curveTo(8.5699f, 19.9048f, 9.5398f, 20.7425f, 10.2653f, 21.3234f)
                curveTo(10.6644f, 21.643f, 11.0705f, 21.957f, 11.4937f, 22.2443f)
                curveTo(11.7939f, 22.4473f, 12.2035f, 22.4482f, 12.504f, 22.2455f)
                curveTo(12.9279f, 21.9579f, 13.3346f, 21.6434f, 13.7342f, 21.3234f)
                close()
                moveTo(14.7499f, 10.7494f)
                curveTo(14.7499f, 12.2682f, 13.5187f, 13.4994f, 11.9999f, 13.4994f)
                curveTo(10.4811f, 13.4994f, 9.2499f, 12.2682f, 9.2499f, 10.7494f)
                curveTo(9.2499f, 9.2306f, 10.4811f, 7.9994f, 11.9999f, 7.9994f)
                curveTo(13.5187f, 7.9994f, 14.7499f, 9.2306f, 14.7499f, 10.7494f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun LocationFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.LocationFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
