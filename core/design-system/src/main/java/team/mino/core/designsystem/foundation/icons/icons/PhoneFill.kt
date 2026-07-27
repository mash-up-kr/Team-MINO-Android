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

val MinoIcons.PhoneFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.PhoneFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(8.1818f, 15.8218f)
                curveTo(11.4907f, 19.1308f, 14.8244f, 20.3535f, 17.0084f, 20.7959f)
                curveTo(18.8212f, 21.1632f, 20.4912f, 20.3415f, 21.6552f, 19.1775f)
                lineTo(21.9272f, 18.9055f)
                curveTo(23.1944f, 17.6383f, 23.0204f, 15.5366f, 21.5622f, 14.495f)
                lineTo(19.5676f, 13.0703f)
                curveTo(18.613f, 12.3884f, 17.3052f, 12.4966f, 16.4756f, 13.3262f)
                lineTo(15.6465f, 14.1553f)
                curveTo(15.0405f, 13.9035f, 13.8216f, 13.2592f, 12.283f, 11.7206f)
                curveTo(10.7444f, 10.182f, 10.1001f, 8.9631f, 9.8483f, 8.357f)
                lineTo(10.6774f, 7.5279f)
                curveTo(11.5069f, 6.6984f, 11.6152f, 5.3906f, 10.9333f, 4.4359f)
                lineTo(9.5086f, 2.4413f)
                curveTo(8.467f, 0.9831f, 6.3653f, 0.8092f, 5.0982f, 2.0763f)
                lineTo(4.8261f, 2.3483f)
                curveTo(3.6621f, 3.5124f, 2.8404f, 5.1823f, 3.2077f, 6.9952f)
                curveTo(3.6501f, 9.1792f, 4.8728f, 12.5129f, 8.1818f, 15.8218f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun PhoneFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.PhoneFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
