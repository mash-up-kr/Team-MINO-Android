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

val MinoIcons.Tune: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Tune",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(11.7113f, 8.6496f)
                horizontalLineTo(3.4998f)
                curveTo(3.0027f, 8.6496f, 2.5998f, 8.2467f, 2.5998f, 7.7496f)
                curveTo(2.5998f, 7.2526f, 3.0027f, 6.8496f, 3.4998f, 6.8496f)
                horizontalLineTo(11.7118f)
                curveTo(12.113f, 5.2695f, 13.5449f, 4.1006f, 15.2498f, 4.1006f)
                curveTo(16.9547f, 4.1006f, 18.3865f, 5.2695f, 18.7877f, 6.8496f)
                horizontalLineTo(20.4997f)
                curveTo(20.9968f, 6.8496f, 21.3997f, 7.2526f, 21.3997f, 7.7496f)
                curveTo(21.3997f, 8.2467f, 20.9968f, 8.6496f, 20.4997f, 8.6496f)
                horizontalLineTo(18.7882f)
                curveTo(18.3877f, 10.2307f, 16.9554f, 11.4006f, 15.2498f, 11.4006f)
                curveTo(13.5442f, 11.4006f, 12.1118f, 10.2307f, 11.7113f, 8.6496f)
                close()
                moveTo(13.3998f, 7.7506f)
                curveTo(13.3998f, 6.7289f, 14.228f, 5.9006f, 15.2498f, 5.9006f)
                curveTo(16.2715f, 5.9006f, 17.0998f, 6.7289f, 17.0998f, 7.7506f)
                curveTo(17.0998f, 8.7723f, 16.2715f, 9.6006f, 15.2498f, 9.6006f)
                curveTo(14.228f, 9.6006f, 13.3998f, 8.7723f, 13.3998f, 7.7506f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(5.2114f, 17.1496f)
                horizontalLineTo(3.4998f)
                curveTo(3.0027f, 17.1496f, 2.5998f, 16.7467f, 2.5998f, 16.2496f)
                curveTo(2.5998f, 15.7526f, 3.0027f, 15.3496f, 3.4998f, 15.3496f)
                horizontalLineTo(5.2118f)
                curveTo(5.613f, 13.7695f, 7.0449f, 12.6006f, 8.7498f, 12.6006f)
                curveTo(10.4547f, 12.6006f, 11.8866f, 13.7695f, 12.2877f, 15.3496f)
                horizontalLineTo(20.4997f)
                curveTo(20.9968f, 15.3496f, 21.3997f, 15.7526f, 21.3997f, 16.2496f)
                curveTo(21.3997f, 16.7467f, 20.9968f, 17.1496f, 20.4997f, 17.1496f)
                horizontalLineTo(12.2882f)
                curveTo(11.8877f, 18.7307f, 10.4554f, 19.9006f, 8.7498f, 19.9006f)
                curveTo(7.0442f, 19.9006f, 5.6118f, 18.7307f, 5.2114f, 17.1496f)
                close()
                moveTo(6.8998f, 16.2506f)
                curveTo(6.8998f, 15.2289f, 7.7281f, 14.4006f, 8.7498f, 14.4006f)
                curveTo(9.7715f, 14.4006f, 10.5998f, 15.2289f, 10.5998f, 16.2506f)
                curveTo(10.5998f, 17.2723f, 9.7715f, 18.1006f, 8.7498f, 18.1006f)
                curveTo(7.7281f, 18.1006f, 6.8998f, 17.2723f, 6.8998f, 16.2506f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun TunePreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Tune,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
