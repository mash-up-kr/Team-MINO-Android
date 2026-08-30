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

val MinoIcons.Search: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(9.9997f, 2.1f)
                curveTo(5.6367f, 2.1f, 2.0997f, 5.637f, 2.0997f, 10f)
                curveTo(2.0997f, 14.3631f, 5.6367f, 17.9f, 9.9997f, 17.9f)
                curveTo(11.857f, 17.9f, 13.5646f, 17.2591f, 14.9134f, 16.1863f)
                lineTo(19.8634f, 21.1364f)
                curveTo(20.2149f, 21.4879f, 20.7848f, 21.4879f, 21.1362f, 21.1364f)
                curveTo(21.4877f, 20.7849f, 21.4877f, 20.2151f, 21.1362f, 19.8636f)
                lineTo(16.1862f, 14.9135f)
                curveTo(17.2588f, 13.5648f, 17.8997f, 11.8572f, 17.8997f, 10f)
                curveTo(17.8997f, 5.637f, 14.3628f, 2.1f, 9.9997f, 2.1f)
                close()
                moveTo(3.8998f, 10f)
                curveTo(3.8998f, 6.6311f, 6.6308f, 3.9f, 9.9997f, 3.9f)
                curveTo(13.3686f, 3.9f, 16.0997f, 6.6311f, 16.0997f, 10f)
                curveTo(16.0997f, 13.369f, 13.3686f, 16.1f, 9.9997f, 16.1f)
                curveTo(6.6308f, 16.1f, 3.8998f, 13.369f, 3.8998f, 10f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun SearchPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Search,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
