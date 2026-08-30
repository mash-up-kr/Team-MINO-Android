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

val MinoIcons.CloseThick: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.CloseThick",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(4.5808f, 4.581f)
                curveTo(5.0885f, 4.0733f, 5.9116f, 4.0733f, 6.4192f, 4.581f)
                lineTo(12f, 10.1617f)
                lineTo(17.5808f, 4.581f)
                curveTo(18.0885f, 4.0733f, 18.9116f, 4.0733f, 19.4193f, 4.581f)
                curveTo(19.9269f, 5.0886f, 19.9269f, 5.9117f, 19.4193f, 6.4194f)
                lineTo(13.8385f, 12.0002f)
                lineTo(19.4193f, 17.581f)
                curveTo(19.9269f, 18.0887f, 19.9269f, 18.9118f, 19.4193f, 19.4194f)
                curveTo(18.9116f, 19.9271f, 18.0885f, 19.9271f, 17.5808f, 19.4194f)
                lineTo(12f, 13.8387f)
                lineTo(6.4192f, 19.4194f)
                curveTo(5.9116f, 19.9271f, 5.0885f, 19.9271f, 4.5808f, 19.4194f)
                curveTo(4.0731f, 18.9118f, 4.0731f, 18.0887f, 4.5808f, 17.581f)
                lineTo(10.1615f, 12.0002f)
                lineTo(4.5808f, 6.4194f)
                curveTo(4.0731f, 5.9117f, 4.0731f, 5.0886f, 4.5808f, 4.581f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun CloseThickPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.CloseThick,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
