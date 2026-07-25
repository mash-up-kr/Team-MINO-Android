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

val MinoIcons.PencilFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.PencilFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(20.1781f, 3.8215f)
                curveTo(19.0213f, 2.6646f, 17.1456f, 2.6646f, 15.9887f, 3.8215f)
                lineTo(3.3638f, 16.4465f)
                curveTo(3.195f, 16.6153f, 3.1002f, 16.8442f, 3.1002f, 17.0829f)
                verticalLineTo(19.9996f)
                curveTo(3.1002f, 20.4967f, 3.5031f, 20.8996f, 4.0001f, 20.8996f)
                horizontalLineTo(6.9168f)
                curveTo(7.1555f, 20.8996f, 7.3844f, 20.8048f, 7.5532f, 20.636f)
                lineTo(20.1781f, 8.011f)
                curveTo(21.335f, 6.8541f, 21.335f, 4.9784f, 20.1781f, 3.8215f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun PencilFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.PencilFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
