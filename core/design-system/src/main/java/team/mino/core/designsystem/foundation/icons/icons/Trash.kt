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

val MinoIcons.Trash: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Trash",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(10f, 10.6006f)
                curveTo(10.497f, 10.6006f, 10.8999f, 11.0036f, 10.8999f, 11.5006f)
                verticalLineTo(16.5006f)
                curveTo(10.8999f, 16.9977f, 10.497f, 17.4006f, 10f, 17.4006f)
                curveTo(9.5029f, 17.4006f, 9.0999f, 16.9977f, 9.0999f, 16.5006f)
                verticalLineTo(11.5006f)
                curveTo(9.0999f, 11.0036f, 9.5029f, 10.6006f, 10f, 10.6006f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(13.9999f, 10.6006f)
                curveTo(14.497f, 10.6006f, 14.8999f, 11.0036f, 14.8999f, 11.5006f)
                verticalLineTo(16.5006f)
                curveTo(14.8999f, 16.9977f, 14.497f, 17.4006f, 13.9999f, 17.4006f)
                curveTo(13.5029f, 17.4006f, 13.0999f, 16.9977f, 13.0999f, 16.5006f)
                verticalLineTo(11.5006f)
                curveTo(13.0999f, 11.0036f, 13.5029f, 10.6006f, 13.9999f, 10.6006f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(19.9999f, 5.851f)
                horizontalLineTo(16.3998f)
                curveTo(16.3997f, 5.3503f, 16.3985f, 4.9218f, 16.3695f, 4.5672f)
                curveTo(16.3378f, 4.1781f, 16.2681f, 3.7995f, 16.0837f, 3.4375f)
                curveTo(15.8057f, 2.8919f, 15.362f, 2.4482f, 14.8164f, 2.1702f)
                curveTo(14.4544f, 1.9858f, 14.0758f, 1.9161f, 13.6867f, 1.8843f)
                curveTo(13.3163f, 1.8541f, 12.8651f, 1.8541f, 12.3353f, 1.8541f)
                horizontalLineTo(11.6644f)
                curveTo(11.1346f, 1.8541f, 10.6833f, 1.8541f, 10.3129f, 1.8843f)
                curveTo(9.9238f, 1.9161f, 9.5452f, 1.9858f, 9.1832f, 2.1702f)
                curveTo(8.6376f, 2.4482f, 8.1939f, 2.8919f, 7.9159f, 3.4375f)
                curveTo(7.7315f, 3.7995f, 7.6618f, 4.1781f, 7.63f, 4.5672f)
                curveTo(7.6011f, 4.9218f, 7.5998f, 5.3503f, 7.5998f, 5.851f)
                horizontalLineTo(4f)
                curveTo(3.5029f, 5.851f, 3.1f, 6.2539f, 3.1f, 6.751f)
                curveTo(3.1f, 7.2481f, 3.5029f, 7.651f, 4f, 7.651f)
                horizontalLineTo(4.5999f)
                lineTo(4.5999f, 18.0874f)
                curveTo(4.5998f, 18.6172f, 4.5998f, 19.0684f, 4.6301f, 19.4388f)
                curveTo(4.6619f, 19.828f, 4.7315f, 20.2066f, 4.9159f, 20.5685f)
                curveTo(5.194f, 21.1142f, 5.6376f, 21.5579f, 6.1833f, 21.8359f)
                curveTo(6.5452f, 22.0203f, 6.9238f, 22.0899f, 7.313f, 22.1217f)
                curveTo(7.6834f, 22.152f, 8.1345f, 22.152f, 8.6643f, 22.152f)
                horizontalLineTo(15.3352f)
                curveTo(15.8651f, 22.152f, 16.3163f, 22.152f, 16.6867f, 22.1217f)
                curveTo(17.0759f, 22.0899f, 17.4545f, 22.0203f, 17.8164f, 21.8359f)
                curveTo(18.3621f, 21.5579f, 18.8057f, 21.1142f, 19.0837f, 20.5685f)
                curveTo(19.2682f, 20.2066f, 19.3378f, 19.828f, 19.3696f, 19.4388f)
                curveTo(19.3998f, 19.0684f, 19.3998f, 18.6172f, 19.3998f, 18.0874f)
                verticalLineTo(7.651f)
                horizontalLineTo(19.9999f)
                curveTo(20.497f, 7.651f, 20.8999f, 7.2481f, 20.8999f, 6.751f)
                curveTo(20.8999f, 6.2539f, 20.497f, 5.851f, 19.9999f, 5.851f)
                close()
                moveTo(14.6001f, 5.851f)
                horizontalLineTo(9.4002f)
                verticalLineTo(5.545f)
                curveTo(9.4002f, 4.7304f, 9.4113f, 4.5366f, 9.454f, 4.4051f)
                curveTo(9.5628f, 4.0702f, 9.8254f, 3.8076f, 10.1602f, 3.6988f)
                curveTo(10.2918f, 3.6561f, 10.4856f, 3.645f, 11.3001f, 3.645f)
                horizontalLineTo(12.7001f)
                curveTo(13.5147f, 3.645f, 13.7085f, 3.6561f, 13.8401f, 3.6988f)
                curveTo(14.1749f, 3.8076f, 14.4375f, 4.0702f, 14.5463f, 4.4051f)
                curveTo(14.589f, 4.5366f, 14.6001f, 4.7304f, 14.6001f, 5.545f)
                verticalLineTo(5.851f)
                close()
                moveTo(6.4002f, 18.4456f)
                verticalLineTo(7.651f)
                horizontalLineTo(17.6002f)
                verticalLineTo(18.4456f)
                curveTo(17.6002f, 19.2601f, 17.589f, 19.454f, 17.5463f, 19.5855f)
                curveTo(17.4375f, 19.9204f, 17.175f, 20.1829f, 16.8401f, 20.2917f)
                curveTo(16.7086f, 20.3345f, 16.5147f, 20.3456f, 15.7002f, 20.3456f)
                horizontalLineTo(8.3002f)
                curveTo(7.4856f, 20.3456f, 7.2918f, 20.3345f, 7.1603f, 20.2917f)
                curveTo(6.8254f, 20.1829f, 6.5628f, 19.9204f, 6.454f, 19.5855f)
                curveTo(6.4113f, 19.454f, 6.4002f, 19.2601f, 6.4002f, 18.4456f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun TrashPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Trash,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
