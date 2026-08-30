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

val MinoIcons.ExternalLink: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.ExternalLink",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(14.7499f, 2.8496f)
                curveTo(14.2529f, 2.8496f, 13.85f, 3.2525f, 13.85f, 3.7496f)
                curveTo(13.85f, 4.2467f, 14.2529f, 4.6496f, 14.7499f, 4.6496f)
                horizontalLineTo(18.0771f)
                lineTo(11.3636f, 11.3632f)
                curveTo(11.0121f, 11.7147f, 11.0121f, 12.2845f, 11.3636f, 12.636f)
                curveTo(11.715f, 12.9875f, 12.2849f, 12.9875f, 12.6364f, 12.636f)
                lineTo(19.3499f, 5.9224f)
                verticalLineTo(9.2496f)
                curveTo(19.3499f, 9.7467f, 19.7529f, 10.1496f, 20.2499f, 10.1496f)
                curveTo(20.747f, 10.1496f, 21.1499f, 9.7467f, 21.1499f, 9.2496f)
                verticalLineTo(3.7496f)
                curveTo(21.1499f, 3.2525f, 20.747f, 2.8496f, 20.2499f, 2.8496f)
                horizontalLineTo(14.7499f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(10.0995f, 2.8502f)
                curveTo(10.5966f, 2.8502f, 10.9995f, 3.2531f, 10.9995f, 3.7502f)
                curveTo(10.9995f, 4.2473f, 10.5966f, 4.6502f, 10.0995f, 4.6502f)
                horizontalLineTo(8.5498f)
                curveTo(7.6948f, 4.6502f, 7.1078f, 4.6509f, 6.6527f, 4.6881f)
                curveTo(6.2081f, 4.7244f, 5.9692f, 4.791f, 5.7964f, 4.8791f)
                curveTo(5.4013f, 5.0804f, 5.08f, 5.4017f, 4.8787f, 5.7968f)
                curveTo(4.7906f, 5.9697f, 4.724f, 6.2085f, 4.6877f, 6.6531f)
                curveTo(4.6505f, 7.1082f, 4.6498f, 7.6953f, 4.6498f, 8.5502f)
                verticalLineTo(15.4502f)
                curveTo(4.6498f, 16.3051f, 4.6505f, 16.8922f, 4.6877f, 17.3473f)
                curveTo(4.724f, 17.7919f, 4.7906f, 18.0307f, 4.8787f, 18.2036f)
                curveTo(5.08f, 18.5987f, 5.4013f, 18.92f, 5.7964f, 19.1213f)
                curveTo(5.9692f, 19.2094f, 6.2081f, 19.276f, 6.6527f, 19.3123f)
                curveTo(7.1078f, 19.3495f, 7.6948f, 19.3502f, 8.5498f, 19.3502f)
                horizontalLineTo(15.4498f)
                curveTo(16.3047f, 19.3502f, 16.8918f, 19.3495f, 17.3469f, 19.3123f)
                curveTo(17.7915f, 19.276f, 18.0303f, 19.2094f, 18.2031f, 19.1213f)
                curveTo(18.5983f, 18.92f, 18.9195f, 18.5987f, 19.1209f, 18.2036f)
                curveTo(19.2089f, 18.0307f, 19.2755f, 17.7919f, 19.3119f, 17.3473f)
                curveTo(19.3491f, 16.8922f, 19.3498f, 16.3051f, 19.3498f, 15.4502f)
                verticalLineTo(13.9003f)
                curveTo(19.3498f, 13.4032f, 19.7527f, 13.0003f, 20.2497f, 13.0003f)
                curveTo(20.7468f, 13.0003f, 21.1497f, 13.4032f, 21.1497f, 13.9003f)
                verticalLineTo(15.4879f)
                curveTo(21.1498f, 16.296f, 21.1498f, 16.9568f, 21.1059f, 17.4939f)
                curveTo(21.0605f, 18.0498f, 20.9636f, 18.5519f, 20.7247f, 19.0208f)
                curveTo(20.3508f, 19.7546f, 19.7541f, 20.3512f, 19.0203f, 20.7251f)
                curveTo(18.5514f, 20.964f, 18.0493f, 21.0609f, 17.4934f, 21.1063f)
                curveTo(16.9564f, 21.1502f, 16.2955f, 21.1502f, 15.4875f, 21.1502f)
                horizontalLineTo(8.5121f)
                curveTo(7.704f, 21.1502f, 7.0432f, 21.1502f, 6.5061f, 21.1063f)
                curveTo(5.9502f, 21.0609f, 5.4481f, 20.964f, 4.9792f, 20.7251f)
                curveTo(4.2454f, 20.3512f, 3.6488f, 19.7546f, 3.2749f, 19.0208f)
                curveTo(3.036f, 18.5519f, 2.9391f, 18.0498f, 2.8937f, 17.4939f)
                curveTo(2.8498f, 16.9568f, 2.8498f, 16.296f, 2.8498f, 15.4879f)
                verticalLineTo(8.5125f)
                curveTo(2.8498f, 7.7045f, 2.8498f, 7.0436f, 2.8937f, 6.5065f)
                curveTo(2.9391f, 5.9506f, 3.036f, 5.4485f, 3.2749f, 4.9796f)
                curveTo(3.6488f, 4.2458f, 4.2454f, 3.6492f, 4.9792f, 3.2753f)
                curveTo(5.4481f, 3.0364f, 5.9502f, 2.9395f, 6.5061f, 2.8941f)
                curveTo(7.0432f, 2.8502f, 7.704f, 2.8502f, 8.5121f, 2.8502f)
                horizontalLineTo(10.0995f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ExternalLinkPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.ExternalLink,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
