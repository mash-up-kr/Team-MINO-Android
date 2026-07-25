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

val MinoIcons.SearchThick: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.SearchThick",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(9.9999f, 1.7f)
                curveTo(5.416f, 1.7f, 1.6999f, 5.4161f, 1.6999f, 10f)
                curveTo(1.6999f, 14.584f, 5.416f, 18.3f, 9.9999f, 18.3f)
                curveTo(11.8228f, 18.3f, 13.5085f, 17.7124f, 14.8777f, 16.7161f)
                lineTo(19.5808f, 21.4192f)
                curveTo(20.0885f, 21.9269f, 20.9116f, 21.9269f, 21.4193f, 21.4192f)
                curveTo(21.9269f, 20.9115f, 21.9269f, 20.0884f, 21.4193f, 19.5807f)
                lineTo(16.7162f, 14.8776f)
                curveTo(17.7123f, 13.5084f, 18.2999f, 11.8228f, 18.2999f, 10f)
                curveTo(18.2999f, 5.4161f, 14.5839f, 1.7f, 9.9999f, 1.7f)
                close()
                moveTo(4.2999f, 10f)
                curveTo(4.2999f, 6.852f, 6.8519f, 4.3f, 9.9999f, 4.3f)
                curveTo(13.1479f, 4.3f, 15.6999f, 6.852f, 15.6999f, 10f)
                curveTo(15.6999f, 13.148f, 13.1479f, 15.7f, 9.9999f, 15.7f)
                curveTo(6.8519f, 15.7f, 4.2999f, 13.148f, 4.2999f, 10f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun SearchThickPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.SearchThick,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
