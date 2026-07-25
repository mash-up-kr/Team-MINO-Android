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

val MinoIcons.LogoLinkedIn: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.LogoLinkedIn",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(14.389f, 2.35f)
                horizontalLineTo(9.6111f)
                curveTo(8.5241f, 2.35f, 7.6534f, 2.35f, 6.9496f, 2.4075f)
                curveTo(6.2269f, 2.4665f, 5.6014f, 2.5907f, 5.0255f, 2.8841f)
                curveTo(4.1035f, 3.3538f, 3.3539f, 4.1034f, 2.8842f, 5.0255f)
                curveTo(2.5908f, 5.6013f, 2.4666f, 6.2268f, 2.4076f, 6.9495f)
                curveTo(2.3501f, 7.6533f, 2.3501f, 8.524f, 2.3501f, 9.611f)
                verticalLineTo(14.389f)
                curveTo(2.3501f, 15.476f, 2.3501f, 16.3467f, 2.4076f, 17.0505f)
                curveTo(2.4666f, 17.7731f, 2.5908f, 18.3987f, 2.8842f, 18.9746f)
                curveTo(3.3539f, 19.8965f, 4.1035f, 20.6462f, 5.0255f, 21.1159f)
                curveTo(5.6014f, 21.4093f, 6.2269f, 21.5335f, 6.9496f, 21.5925f)
                curveTo(7.6533f, 21.65f, 8.524f, 21.65f, 9.611f, 21.65f)
                horizontalLineTo(14.389f)
                curveTo(15.476f, 21.65f, 16.3468f, 21.65f, 17.0505f, 21.5925f)
                curveTo(17.7732f, 21.5335f, 18.3987f, 21.4093f, 18.9746f, 21.1159f)
                curveTo(19.8966f, 20.6462f, 20.6462f, 19.8965f, 21.116f, 18.9746f)
                curveTo(21.4094f, 18.3987f, 21.5335f, 17.7731f, 21.5925f, 17.0505f)
                curveTo(21.65f, 16.3467f, 21.65f, 15.476f, 21.65f, 14.389f)
                verticalLineTo(9.6111f)
                curveTo(21.65f, 8.524f, 21.65f, 7.6533f, 21.5925f, 6.9495f)
                curveTo(21.5335f, 6.2268f, 21.4094f, 5.6013f, 21.116f, 5.0255f)
                curveTo(20.6462f, 4.1034f, 19.8966f, 3.3538f, 18.9746f, 2.8841f)
                curveTo(18.3987f, 2.5907f, 17.7732f, 2.4665f, 17.0505f, 2.4075f)
                curveTo(16.3468f, 2.35f, 15.4761f, 2.35f, 14.389f, 2.35f)
                close()
                moveTo(6.5499f, 7.8494f)
                curveTo(6.5499f, 8.5671f, 7.1304f, 9.1488f, 7.8495f, 9.1488f)
                curveTo(8.5655f, 9.1488f, 9.1461f, 8.5641f, 9.1461f, 7.8494f)
                curveTo(9.1461f, 7.1318f, 8.5655f, 6.55f, 7.8495f, 6.55f)
                curveTo(7.1304f, 6.55f, 6.5499f, 7.1318f, 6.5499f, 7.8494f)
                close()
                moveTo(6.7297f, 17.35f)
                horizontalLineTo(8.9692f)
                verticalLineTo(10.1323f)
                horizontalLineTo(6.7297f)
                verticalLineTo(17.35f)
                close()
                moveTo(17.3499f, 17.35f)
                horizontalLineTo(15.1133f)
                verticalLineTo(13.8416f)
                curveTo(15.1133f, 13.0058f, 15.0985f, 11.9279f, 13.9493f, 11.9279f)
                curveTo(12.7853f, 11.9279f, 12.6085f, 12.8404f, 12.6085f, 13.7825f)
                verticalLineTo(17.35f)
                horizontalLineTo(10.3749f)
                verticalLineTo(10.1323f)
                horizontalLineTo(12.5201f)
                verticalLineTo(11.1187f)
                horizontalLineTo(12.5496f)
                curveTo(12.8472f, 10.5516f, 13.578f, 9.9521f, 14.6654f, 9.9521f)
                curveTo(16.9314f, 9.9521f, 17.3499f, 11.4465f, 17.3499f, 13.3897f)
                verticalLineTo(17.35f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun LogoLinkedInPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.LogoLinkedIn,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
