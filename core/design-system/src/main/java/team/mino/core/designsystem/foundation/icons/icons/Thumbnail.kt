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

val MinoIcons.Thumbnail: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Thumbnail",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(15.5704f, 13.0991f)
                horizontalLineTo(18.93f)
                curveTo(19.1843f, 13.0991f, 19.4263f, 13.0991f, 19.6303f, 13.1157f)
                curveTo(19.8527f, 13.1339f, 20.1078f, 13.1763f, 20.3628f, 13.3062f)
                curveTo(20.7203f, 13.4884f, 21.0109f, 13.779f, 21.1931f, 14.1365f)
                curveTo(21.323f, 14.3915f, 21.3654f, 14.6466f, 21.3836f, 14.869f)
                curveTo(21.4002f, 15.073f, 21.4002f, 15.315f, 21.4002f, 15.5693f)
                verticalLineTo(18.9289f)
                curveTo(21.4002f, 19.1832f, 21.4002f, 19.4253f, 21.3836f, 19.6292f)
                curveTo(21.3654f, 19.8516f, 21.323f, 20.1067f, 21.1931f, 20.3617f)
                curveTo(21.0109f, 20.7192f, 20.7203f, 21.0099f, 20.3628f, 21.192f)
                curveTo(20.1078f, 21.322f, 19.8527f, 21.3643f, 19.6303f, 21.3825f)
                curveTo(19.4263f, 21.3992f, 19.1843f, 21.3992f, 18.93f, 21.3991f)
                horizontalLineTo(15.5704f)
                curveTo(15.3161f, 21.3992f, 15.0741f, 21.3992f, 14.8701f, 21.3825f)
                curveTo(14.6477f, 21.3643f, 14.3926f, 21.322f, 14.1376f, 21.192f)
                curveTo(13.7801f, 21.0099f, 13.4895f, 20.7192f, 13.3073f, 20.3617f)
                curveTo(13.1774f, 20.1067f, 13.135f, 19.8516f, 13.1168f, 19.6292f)
                curveTo(13.1002f, 19.4252f, 13.1002f, 19.1832f, 13.1002f, 18.9289f)
                verticalLineTo(15.5694f)
                curveTo(13.1002f, 15.315f, 13.1002f, 15.073f, 13.1168f, 14.869f)
                curveTo(13.135f, 14.6466f, 13.1774f, 14.3915f, 13.3073f, 14.1365f)
                curveTo(13.4895f, 13.779f, 13.7801f, 13.4884f, 14.1376f, 13.3062f)
                curveTo(14.3926f, 13.1763f, 14.6477f, 13.1339f, 14.8701f, 13.1157f)
                curveTo(15.0741f, 13.0991f, 15.3161f, 13.0991f, 15.5704f, 13.0991f)
                close()
                moveTo(15.0003f, 14.9f)
                curveTo(14.945f, 14.9f, 14.9003f, 14.9448f, 14.9003f, 15f)
                verticalLineTo(19.5f)
                curveTo(14.9003f, 19.5552f, 14.945f, 19.6f, 15.0003f, 19.6f)
                horizontalLineTo(19.5003f)
                curveTo(19.5555f, 19.6f, 19.6003f, 19.5552f, 19.6003f, 19.5f)
                verticalLineTo(15f)
                curveTo(19.6003f, 14.9448f, 19.5555f, 14.9f, 19.5003f, 14.9f)
                horizontalLineTo(15.0003f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(5.0705f, 13.0991f)
                horizontalLineTo(8.43f)
                curveTo(8.6843f, 13.0991f, 8.9263f, 13.0991f, 9.1303f, 13.1157f)
                curveTo(9.3527f, 13.1339f, 9.6078f, 13.1763f, 9.8628f, 13.3062f)
                curveTo(10.2203f, 13.4884f, 10.511f, 13.779f, 10.6931f, 14.1365f)
                curveTo(10.823f, 14.3915f, 10.8654f, 14.6466f, 10.8836f, 14.869f)
                curveTo(10.9003f, 15.073f, 10.9002f, 15.315f, 10.9002f, 15.5693f)
                verticalLineTo(18.9289f)
                curveTo(10.9002f, 19.1832f, 10.9003f, 19.4253f, 10.8836f, 19.6292f)
                curveTo(10.8654f, 19.8516f, 10.823f, 20.1067f, 10.6931f, 20.3617f)
                curveTo(10.511f, 20.7192f, 10.2203f, 21.0099f, 9.8628f, 21.192f)
                curveTo(9.6078f, 21.322f, 9.3527f, 21.3643f, 9.1303f, 21.3825f)
                curveTo(8.9264f, 21.3992f, 8.6843f, 21.3992f, 8.43f, 21.3991f)
                horizontalLineTo(5.0705f)
                curveTo(4.8162f, 21.3992f, 4.5741f, 21.3992f, 4.3701f, 21.3825f)
                curveTo(4.1477f, 21.3643f, 3.8926f, 21.322f, 3.6377f, 21.192f)
                curveTo(3.2801f, 21.0099f, 2.9895f, 20.7192f, 2.8073f, 20.3617f)
                curveTo(2.6774f, 20.1067f, 2.635f, 19.8516f, 2.6169f, 19.6292f)
                curveTo(2.6002f, 19.4252f, 2.6002f, 19.1832f, 2.6002f, 18.9289f)
                verticalLineTo(15.5694f)
                curveTo(2.6002f, 15.315f, 2.6002f, 15.073f, 2.6169f, 14.869f)
                curveTo(2.635f, 14.6466f, 2.6774f, 14.3915f, 2.8073f, 14.1365f)
                curveTo(2.9895f, 13.779f, 3.2801f, 13.4884f, 3.6377f, 13.3062f)
                curveTo(3.8926f, 13.1763f, 4.1477f, 13.1339f, 4.3701f, 13.1157f)
                curveTo(4.5741f, 13.0991f, 4.8162f, 13.0991f, 5.0705f, 13.0991f)
                close()
                moveTo(4.5003f, 14.9f)
                curveTo(4.4451f, 14.9f, 4.4003f, 14.9448f, 4.4003f, 15f)
                verticalLineTo(19.5f)
                curveTo(4.4003f, 19.5552f, 4.4451f, 19.6f, 4.5003f, 19.6f)
                horizontalLineTo(9.0003f)
                curveTo(9.0555f, 19.6f, 9.1003f, 19.5552f, 9.1003f, 19.5f)
                verticalLineTo(15f)
                curveTo(9.1003f, 14.9448f, 9.0555f, 14.9f, 9.0003f, 14.9f)
                horizontalLineTo(4.5003f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(15.5704f, 2.5991f)
                horizontalLineTo(18.93f)
                curveTo(19.1843f, 2.5991f, 19.4263f, 2.5991f, 19.6303f, 2.6157f)
                curveTo(19.8527f, 2.6339f, 20.1078f, 2.6763f, 20.3628f, 2.8062f)
                curveTo(20.7203f, 2.9884f, 21.0109f, 3.279f, 21.1931f, 3.6365f)
                curveTo(21.323f, 3.8915f, 21.3654f, 4.1466f, 21.3836f, 4.369f)
                curveTo(21.4002f, 4.573f, 21.4002f, 4.815f, 21.4002f, 5.0693f)
                verticalLineTo(8.4289f)
                curveTo(21.4002f, 8.6832f, 21.4002f, 8.9253f, 21.3836f, 9.1292f)
                curveTo(21.3654f, 9.3516f, 21.323f, 9.6067f, 21.1931f, 9.8617f)
                curveTo(21.0109f, 10.2192f, 20.7203f, 10.5099f, 20.3628f, 10.692f)
                curveTo(20.1078f, 10.822f, 19.8527f, 10.8643f, 19.6303f, 10.8825f)
                curveTo(19.4263f, 10.8992f, 19.1843f, 10.8992f, 18.93f, 10.8991f)
                horizontalLineTo(15.5704f)
                curveTo(15.3161f, 10.8992f, 15.0741f, 10.8992f, 14.8701f, 10.8825f)
                curveTo(14.6477f, 10.8643f, 14.3926f, 10.822f, 14.1376f, 10.692f)
                curveTo(13.7801f, 10.5099f, 13.4895f, 10.2192f, 13.3073f, 9.8617f)
                curveTo(13.1774f, 9.6067f, 13.135f, 9.3516f, 13.1168f, 9.1292f)
                curveTo(13.1002f, 8.9253f, 13.1002f, 8.6832f, 13.1002f, 8.4289f)
                verticalLineTo(5.0694f)
                curveTo(13.1002f, 4.815f, 13.1002f, 4.573f, 13.1168f, 4.369f)
                curveTo(13.135f, 4.1466f, 13.1774f, 3.8915f, 13.3073f, 3.6365f)
                curveTo(13.4895f, 3.279f, 13.7801f, 2.9884f, 14.1376f, 2.8062f)
                curveTo(14.3926f, 2.6763f, 14.6477f, 2.6339f, 14.8701f, 2.6157f)
                curveTo(15.0741f, 2.5991f, 15.3161f, 2.5991f, 15.5704f, 2.5991f)
                close()
                moveTo(15.0003f, 4.4f)
                curveTo(14.945f, 4.4f, 14.9003f, 4.4448f, 14.9003f, 4.5f)
                verticalLineTo(9f)
                curveTo(14.9003f, 9.0552f, 14.945f, 9.1f, 15.0003f, 9.1f)
                horizontalLineTo(19.5003f)
                curveTo(19.5555f, 9.1f, 19.6003f, 9.0552f, 19.6003f, 9f)
                verticalLineTo(4.5f)
                curveTo(19.6003f, 4.4448f, 19.5555f, 4.4f, 19.5003f, 4.4f)
                horizontalLineTo(15.0003f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(5.0705f, 2.5991f)
                horizontalLineTo(8.43f)
                curveTo(8.6843f, 2.5991f, 8.9263f, 2.5991f, 9.1303f, 2.6157f)
                curveTo(9.3527f, 2.6339f, 9.6078f, 2.6763f, 9.8628f, 2.8062f)
                curveTo(10.2203f, 2.9884f, 10.511f, 3.279f, 10.6931f, 3.6365f)
                curveTo(10.823f, 3.8915f, 10.8654f, 4.1466f, 10.8836f, 4.369f)
                curveTo(10.9003f, 4.573f, 10.9002f, 4.815f, 10.9002f, 5.0693f)
                verticalLineTo(8.4289f)
                curveTo(10.9002f, 8.6832f, 10.9003f, 8.9253f, 10.8836f, 9.1292f)
                curveTo(10.8654f, 9.3516f, 10.823f, 9.6067f, 10.6931f, 9.8617f)
                curveTo(10.511f, 10.2192f, 10.2203f, 10.5099f, 9.8628f, 10.692f)
                curveTo(9.6078f, 10.822f, 9.3527f, 10.8643f, 9.1303f, 10.8825f)
                curveTo(8.9264f, 10.8992f, 8.6843f, 10.8992f, 8.43f, 10.8991f)
                horizontalLineTo(5.0705f)
                curveTo(4.8162f, 10.8992f, 4.5741f, 10.8992f, 4.3701f, 10.8825f)
                curveTo(4.1477f, 10.8643f, 3.8926f, 10.822f, 3.6377f, 10.692f)
                curveTo(3.2801f, 10.5099f, 2.9895f, 10.2192f, 2.8073f, 9.8617f)
                curveTo(2.6774f, 9.6067f, 2.635f, 9.3516f, 2.6169f, 9.1292f)
                curveTo(2.6002f, 8.9253f, 2.6002f, 8.6832f, 2.6002f, 8.4289f)
                verticalLineTo(5.0694f)
                curveTo(2.6002f, 4.815f, 2.6002f, 4.573f, 2.6169f, 4.369f)
                curveTo(2.635f, 4.1466f, 2.6774f, 3.8915f, 2.8073f, 3.6365f)
                curveTo(2.9895f, 3.279f, 3.2801f, 2.9884f, 3.6377f, 2.8062f)
                curveTo(3.8926f, 2.6763f, 4.1477f, 2.6339f, 4.3701f, 2.6157f)
                curveTo(4.5741f, 2.5991f, 4.8162f, 2.5991f, 5.0705f, 2.5991f)
                close()
                moveTo(4.5003f, 4.4f)
                curveTo(4.4451f, 4.4f, 4.4003f, 4.4448f, 4.4003f, 4.5f)
                verticalLineTo(9f)
                curveTo(4.4003f, 9.0552f, 4.4451f, 9.1f, 4.5003f, 9.1f)
                horizontalLineTo(9.0003f)
                curveTo(9.0555f, 9.1f, 9.1003f, 9.0552f, 9.1003f, 9f)
                verticalLineTo(4.5f)
                curveTo(9.1003f, 4.4448f, 9.0555f, 4.4f, 9.0003f, 4.4f)
                horizontalLineTo(4.5003f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun ThumbnailPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Thumbnail,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
