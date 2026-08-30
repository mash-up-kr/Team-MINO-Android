package team.mino.core.designsystem.foundation.icons.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews

val MinoIcons.ShareIos: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.ShareIos",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(12.0002f, 14.9016f)
                curveTo(11.5031f, 14.9016f, 11.1002f, 14.4986f, 11.1002f, 14.0016f)
                verticalLineTo(3.6216f)
                lineTo(9.0003f, 5.649f)
                curveTo(8.6427f, 5.9943f, 8.073f, 5.9843f, 7.7277f, 5.6267f)
                curveTo(7.3825f, 5.2691f, 7.3925f, 4.6993f, 7.7501f, 4.3541f)
                lineTo(11.35f, 0.8783f)
                curveTo(11.5138f, 0.7072f, 11.7445f, 0.6006f, 12.0002f, 0.6006f)
                curveTo(12.2559f, 0.6006f, 12.4866f, 0.7072f, 12.6505f, 0.8784f)
                lineTo(16.2503f, 4.3541f)
                curveTo(16.6079f, 4.6993f, 16.6179f, 5.2691f, 16.2727f, 5.6267f)
                curveTo(15.9274f, 5.9843f, 15.3576f, 5.9943f, 15.0001f, 5.649f)
                lineTo(12.9002f, 3.6216f)
                verticalLineTo(14.0016f)
                curveTo(12.9002f, 14.4986f, 12.4972f, 14.9016f, 12.0002f, 14.9016f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(7.1005f, 7.6003f)
                curveTo(7.5974f, 7.6003f, 8.0002f, 8.0031f, 8.0002f, 8.5f)
                curveTo(8.0002f, 8.9968f, 7.5974f, 9.3996f, 7.1005f, 9.3996f)
                horizontalLineTo(6.5504f)
                curveTo(5.7359f, 9.3996f, 5.5421f, 9.4107f, 5.4105f, 9.4535f)
                curveTo(5.0757f, 9.5623f, 4.8131f, 9.8248f, 4.7043f, 10.1597f)
                curveTo(4.6616f, 10.2912f, 4.6505f, 10.4851f, 4.6505f, 11.2996f)
                verticalLineTo(18.6996f)
                curveTo(4.6505f, 19.5142f, 4.6616f, 19.708f, 4.7043f, 19.8395f)
                curveTo(4.8131f, 20.1744f, 5.0757f, 20.437f, 5.4105f, 20.5458f)
                curveTo(5.5421f, 20.5885f, 5.7359f, 20.5996f, 6.5504f, 20.5996f)
                horizontalLineTo(17.4505f)
                curveTo(18.265f, 20.5996f, 18.4589f, 20.5885f, 18.5904f, 20.5458f)
                curveTo(18.9253f, 20.437f, 19.1878f, 20.1744f, 19.2966f, 19.8395f)
                curveTo(19.3393f, 19.708f, 19.3505f, 19.5142f, 19.3505f, 18.6996f)
                verticalLineTo(11.2996f)
                curveTo(19.3505f, 10.4851f, 19.3393f, 10.2912f, 19.2966f, 10.1597f)
                curveTo(19.1878f, 9.8248f, 18.9253f, 9.5623f, 18.5904f, 9.4535f)
                curveTo(18.4589f, 9.4107f, 18.265f, 9.3996f, 17.4505f, 9.3996f)
                horizontalLineTo(16.8998f)
                curveTo(16.403f, 9.3996f, 16.0002f, 8.9968f, 16.0002f, 8.5f)
                curveTo(16.0002f, 8.0031f, 16.403f, 7.6003f, 16.8998f, 7.6003f)
                horizontalLineTo(17.0853f)
                curveTo(17.6152f, 7.6003f, 18.0664f, 7.6003f, 18.4368f, 7.6306f)
                curveTo(18.826f, 7.6624f, 19.2046f, 7.732f, 19.5665f, 7.9164f)
                curveTo(20.1122f, 8.1944f, 20.5558f, 8.6381f, 20.8338f, 9.1838f)
                curveTo(21.0182f, 9.5457f, 21.0879f, 9.9243f, 21.1197f, 10.3135f)
                curveTo(21.1499f, 10.6839f, 21.1499f, 11.135f, 21.1499f, 11.6649f)
                verticalLineTo(18.3357f)
                curveTo(21.1499f, 18.8656f, 21.1499f, 19.3168f, 21.1197f, 19.6872f)
                curveTo(21.0879f, 20.0764f, 21.0182f, 20.455f, 20.8338f, 20.8169f)
                curveTo(20.5558f, 21.3626f, 20.1122f, 21.8062f, 19.5665f, 22.0843f)
                curveTo(19.2046f, 22.2687f, 18.826f, 22.3383f, 18.4368f, 22.3701f)
                curveTo(18.0664f, 22.4004f, 17.6152f, 22.4004f, 17.0853f, 22.4003f)
                horizontalLineTo(6.9145f)
                curveTo(6.3846f, 22.4004f, 5.9335f, 22.4004f, 5.563f, 22.3701f)
                curveTo(5.1739f, 22.3383f, 4.7953f, 22.2687f, 4.4333f, 22.0843f)
                curveTo(3.8877f, 21.8062f, 3.444f, 21.3626f, 3.166f, 20.8169f)
                curveTo(2.9816f, 20.455f, 2.912f, 20.0764f, 2.8802f, 19.6872f)
                curveTo(2.8499f, 19.3168f, 2.8499f, 18.8657f, 2.8499f, 18.3358f)
                verticalLineTo(11.6649f)
                curveTo(2.8499f, 11.1351f, 2.8499f, 10.6839f, 2.8802f, 10.3135f)
                curveTo(2.912f, 9.9243f, 2.9816f, 9.5457f, 3.166f, 9.1838f)
                curveTo(3.444f, 8.6381f, 3.8877f, 8.1944f, 4.4333f, 7.9164f)
                curveTo(4.7953f, 7.732f, 5.1739f, 7.6624f, 5.563f, 7.6306f)
                curveTo(5.9334f, 7.6003f, 6.3846f, 7.6003f, 6.9144f, 7.6003f)
                horizontalLineTo(7.1005f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ShareIosPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.ShareIos,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
