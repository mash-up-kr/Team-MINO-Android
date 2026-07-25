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

val MinoIcons.Share: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Share",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(16.7497f, 2.3506f)
                curveTo(14.8719f, 2.3506f, 13.3497f, 3.8728f, 13.3497f, 5.7506f)
                curveTo(13.3497f, 6.0173f, 13.3804f, 6.2768f, 13.4385f, 6.5258f)
                lineTo(7.7014f, 9.6439f)
                curveTo(7.0827f, 9.0002f, 6.213f, 8.5996f, 5.2497f, 8.5996f)
                curveTo(3.372f, 8.5996f, 1.8498f, 10.1218f, 1.8498f, 11.9996f)
                curveTo(1.8498f, 13.8774f, 3.372f, 15.3996f, 5.2497f, 15.3996f)
                curveTo(6.2124f, 15.3996f, 7.0816f, 14.9996f, 7.7001f, 14.3566f)
                lineTo(13.4383f, 17.4752f)
                curveTo(13.3803f, 17.724f, 13.3497f, 17.9832f, 13.3497f, 18.2496f)
                curveTo(13.3497f, 20.1274f, 14.8719f, 21.6496f, 16.7497f, 21.6496f)
                curveTo(18.6275f, 21.6496f, 20.1497f, 20.1274f, 20.1497f, 18.2496f)
                curveTo(20.1497f, 16.3718f, 18.6275f, 14.8496f, 16.7497f, 14.8496f)
                curveTo(15.7864f, 14.8496f, 14.9167f, 15.2502f, 14.2981f, 15.8939f)
                lineTo(8.5608f, 12.7757f)
                curveTo(8.6189f, 12.5264f, 8.6497f, 12.2666f, 8.6497f, 11.9996f)
                curveTo(8.6497f, 11.7332f, 8.6191f, 11.474f, 8.5611f, 11.2252f)
                lineTo(14.2987f, 8.107f)
                curveTo(14.9173f, 8.7503f, 15.7868f, 9.1506f, 16.7497f, 9.1506f)
                curveTo(18.6275f, 9.1506f, 20.1497f, 7.6284f, 20.1497f, 5.7506f)
                curveTo(20.1497f, 3.8728f, 18.6275f, 2.3506f, 16.7497f, 2.3506f)
                close()
                moveTo(15.1497f, 5.7506f)
                curveTo(15.1497f, 4.8669f, 15.866f, 4.1506f, 16.7497f, 4.1506f)
                curveTo(17.6333f, 4.1506f, 18.3497f, 4.8669f, 18.3497f, 5.7506f)
                curveTo(18.3497f, 6.6342f, 17.6333f, 7.3506f, 16.7497f, 7.3506f)
                curveTo(15.866f, 7.3506f, 15.1497f, 6.6342f, 15.1497f, 5.7506f)
                close()
                moveTo(3.6498f, 11.9996f)
                curveTo(3.6498f, 11.116f, 4.3661f, 10.3996f, 5.2497f, 10.3996f)
                curveTo(6.1334f, 10.3996f, 6.8497f, 11.116f, 6.8497f, 11.9996f)
                curveTo(6.8497f, 12.8833f, 6.1334f, 13.5996f, 5.2497f, 13.5996f)
                curveTo(4.3661f, 13.5996f, 3.6498f, 12.8833f, 3.6498f, 11.9996f)
                close()
                moveTo(15.1497f, 18.2496f)
                curveTo(15.1497f, 17.366f, 15.866f, 16.6496f, 16.7497f, 16.6496f)
                curveTo(17.6333f, 16.6496f, 18.3497f, 17.366f, 18.3497f, 18.2496f)
                curveTo(18.3497f, 19.1333f, 17.6333f, 19.8496f, 16.7497f, 19.8496f)
                curveTo(15.866f, 19.8496f, 15.1497f, 19.1333f, 15.1497f, 18.2496f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun SharePreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Share,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
