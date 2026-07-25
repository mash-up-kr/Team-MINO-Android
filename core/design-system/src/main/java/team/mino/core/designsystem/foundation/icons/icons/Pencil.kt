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

val MinoIcons.Pencil: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Pencil",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(15.9888f, 3.8215f)
                curveTo(17.1456f, 2.6646f, 19.0213f, 2.6646f, 20.1782f, 3.8215f)
                curveTo(21.3351f, 4.9784f, 21.3351f, 6.8541f, 20.1782f, 8.011f)
                lineTo(7.5533f, 20.636f)
                curveTo(7.3845f, 20.8048f, 7.1555f, 20.8996f, 6.9169f, 20.8996f)
                horizontalLineTo(4.0002f)
                curveTo(3.5032f, 20.8996f, 3.1002f, 20.4967f, 3.1002f, 19.9996f)
                verticalLineTo(17.0829f)
                curveTo(3.1002f, 16.8442f, 3.195f, 16.6153f, 3.3638f, 16.4465f)
                lineTo(15.9888f, 3.8215f)
                close()
                moveTo(18.9054f, 5.0943f)
                curveTo(18.4515f, 4.6404f, 17.7155f, 4.6404f, 17.2615f, 5.0943f)
                lineTo(4.9002f, 17.4557f)
                verticalLineTo(19.0996f)
                horizontalLineTo(6.5441f)
                lineTo(18.9054f, 6.7382f)
                curveTo(19.3594f, 6.2843f, 19.3594f, 5.5483f, 18.9054f, 5.0943f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun PencilPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Pencil,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
