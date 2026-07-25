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

val MinoIcons.Coffee: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Coffee",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(6.6654f, 3.0951f)
                curveTo(6.1356f, 3.0951f, 5.6844f, 3.0951f, 5.314f, 3.1254f)
                curveTo(4.9248f, 3.1572f, 4.5462f, 3.2268f, 4.1843f, 3.4112f)
                curveTo(3.6386f, 3.6892f, 3.1949f, 4.1329f, 2.9169f, 4.6786f)
                curveTo(2.7325f, 5.0405f, 2.6629f, 5.4191f, 2.6311f, 5.8082f)
                curveTo(2.6008f, 6.1787f, 2.6008f, 6.6298f, 2.6008f, 7.1597f)
                verticalLineTo(9.4951f)
                curveTo(2.6008f, 14.1342f, 6.3619f, 17.8961f, 11.0012f, 17.8961f)
                curveTo(14.4304f, 17.8961f, 17.3793f, 15.8408f, 18.6844f, 12.8951f)
                horizontalLineTo(20.0007f)
                curveTo(22.1546f, 12.8951f, 23.9007f, 11.149f, 23.9007f, 8.9951f)
                curveTo(23.9007f, 6.8412f, 22.1546f, 5.0951f, 20.0007f, 5.0951f)
                lineTo(19.249f, 5.0964f)
                curveTo(19.2079f, 4.9549f, 19.1546f, 4.8155f, 19.0848f, 4.6786f)
                curveTo(18.8067f, 4.1329f, 18.3631f, 3.6892f, 17.8174f, 3.4112f)
                curveTo(17.4555f, 3.2268f, 17.0769f, 3.1572f, 16.6877f, 3.1254f)
                curveTo(16.3173f, 3.0951f, 15.8661f, 3.0951f, 15.3363f, 3.0951f)
                horizontalLineTo(6.6654f)
                close()
                moveTo(19.4007f, 6.8961f)
                curveTo(19.4008f, 6.9817f, 19.4008f, 7.0696f, 19.4008f, 7.1597f)
                verticalLineTo(9.4951f)
                curveTo(19.4008f, 10.0422f, 19.3485f, 10.5771f, 19.2487f, 11.0951f)
                horizontalLineTo(20.0007f)
                curveTo(21.1605f, 11.0951f, 22.1007f, 10.1549f, 22.1007f, 8.9951f)
                curveTo(22.1007f, 7.8355f, 21.1609f, 6.8955f, 20.0014f, 6.8951f)
                lineTo(19.4007f, 6.8961f)
                close()
                moveTo(5.161f, 4.9488f)
                curveTo(5.2925f, 4.906f, 5.4863f, 4.8949f, 6.3009f, 4.8949f)
                horizontalLineTo(15.7009f)
                curveTo(16.5154f, 4.8949f, 16.7093f, 4.906f, 16.8408f, 4.9488f)
                curveTo(17.1757f, 5.0576f, 17.4382f, 5.3201f, 17.547f, 5.655f)
                curveTo(17.5898f, 5.7865f, 17.6009f, 5.9804f, 17.6009f, 6.7949f)
                verticalLineTo(9.4949f)
                curveTo(17.6009f, 13.1402f, 14.6461f, 16.0959f, 11.0012f, 16.0959f)
                curveTo(7.3563f, 16.0959f, 4.4009f, 13.1401f, 4.4009f, 9.4949f)
                verticalLineTo(6.7949f)
                curveTo(4.4009f, 5.9804f, 4.412f, 5.7865f, 4.4547f, 5.655f)
                curveTo(4.5635f, 5.3201f, 4.8261f, 5.0576f, 5.161f, 4.9488f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(3.8511f, 20.9994f)
                curveTo(3.8511f, 20.5024f, 4.254f, 20.0994f, 4.7511f, 20.0994f)
                horizontalLineTo(17.7511f)
                curveTo(18.2482f, 20.0994f, 18.6511f, 20.5024f, 18.6511f, 20.9994f)
                curveTo(18.6511f, 21.4965f, 18.2482f, 21.8994f, 17.7511f, 21.8994f)
                horizontalLineTo(4.7511f)
                curveTo(4.254f, 21.8994f, 3.8511f, 21.4965f, 3.8511f, 20.9994f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun CoffeePreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Coffee,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
