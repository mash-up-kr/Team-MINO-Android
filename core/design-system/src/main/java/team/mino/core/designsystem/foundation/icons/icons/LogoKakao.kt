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

val MinoIcons.LogoKakao: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.LogoKakao",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(12.0003f, 3.1543f)
                curveTo(6.7957f, 3.1543f, 2.5718f, 6.4354f, 2.5718f, 10.4709f)
                curveTo(2.5718f, 12.9883f, 4.2029f, 15.1852f, 6.692f, 16.524f)
                lineTo(5.6455f, 20.3614f)
                curveTo(5.6257f, 20.4378f, 5.6298f, 20.5183f, 5.6571f, 20.5923f)
                curveTo(5.6845f, 20.6662f, 5.7337f, 20.73f, 5.7984f, 20.7752f)
                curveTo(5.863f, 20.8203f, 5.9399f, 20.8446f, 6.0187f, 20.8448f)
                curveTo(6.0976f, 20.845f, 6.1746f, 20.8211f, 6.2395f, 20.7763f)
                lineTo(10.8217f, 17.7309f)
                curveTo(11.2083f, 17.7309f, 11.6043f, 17.7969f, 12.0003f, 17.7969f)
                curveTo(17.2049f, 17.7969f, 21.4288f, 14.5157f, 21.4288f, 10.4709f)
                curveTo(21.4288f, 6.426f, 17.2049f, 3.1543f, 12.0003f, 3.1543f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun LogoKakaoPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.LogoKakao,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
