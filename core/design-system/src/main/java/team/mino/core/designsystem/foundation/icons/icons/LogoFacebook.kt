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

val MinoIcons.LogoFacebook: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.LogoFacebook",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(11.9999f, 2.1f)
                curveTo(6.5351f, 2.1f, 2.1f, 6.5352f, 2.1f, 12f)
                curveTo(2.1f, 16.9401f, 5.7233f, 21.0387f, 10.4555f, 21.7812f)
                verticalLineTo(14.8611f)
                horizontalLineTo(7.9409f)
                verticalLineTo(12f)
                horizontalLineTo(10.4555f)
                verticalLineTo(9.822f)
                curveTo(10.4555f, 7.3371f, 11.9306f, 5.9709f, 14.1977f, 5.9709f)
                curveTo(15.2768f, 5.9709f, 16.4153f, 6.1689f, 16.4153f, 6.1689f)
                verticalLineTo(8.6043f)
                horizontalLineTo(15.1679f)
                curveTo(13.9403f, 8.6043f, 13.5542f, 9.3666f, 13.5542f, 10.1487f)
                verticalLineTo(12.0099f)
                horizontalLineTo(16.2965f)
                lineTo(15.8609f, 14.871f)
                horizontalLineTo(13.5542f)
                verticalLineTo(21.7911f)
                curveTo(18.2864f, 21.0486f, 21.8999f, 16.9401f, 21.8999f, 12f)
                curveTo(21.8999f, 6.5352f, 17.4746f, 2.1099f, 12.0098f, 2.1099f)
                lineTo(11.9999f, 2.1f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun LogoFacebookPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.LogoFacebook,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
