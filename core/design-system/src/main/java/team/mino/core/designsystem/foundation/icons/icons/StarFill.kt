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

val MinoIcons.StarFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.StarFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(12.8613f, 1.4081f)
                curveTo(12.3199f, 1.1327f, 11.6795f, 1.1327f, 11.1381f, 1.4081f)
                curveTo(10.7036f, 1.6292f, 10.4542f, 2.0393f, 10.2878f, 2.3568f)
                curveTo(10.1122f, 2.692f, 9.9256f, 3.1408f, 9.7075f, 3.6651f)
                lineTo(8.5022f, 6.5629f)
                curveTo(8.4299f, 6.7368f, 8.3908f, 6.8298f, 8.3581f, 6.8966f)
                curveTo(8.3482f, 6.9167f, 8.3414f, 6.9292f, 8.3375f, 6.936f)
                curveTo(8.3314f, 6.9416f, 8.3247f, 6.9465f, 8.3175f, 6.9505f)
                curveTo(8.3098f, 6.9521f, 8.2959f, 6.9547f, 8.2737f, 6.9579f)
                curveTo(8.2001f, 6.9684f, 8.0995f, 6.9768f, 7.9119f, 6.9919f)
                lineTo(4.7834f, 7.2427f)
                curveTo(4.2174f, 7.2881f, 3.7329f, 7.3269f, 3.3598f, 7.3903f)
                curveTo(3.0064f, 7.4504f, 2.5393f, 7.5609f, 2.1948f, 7.9058f)
                curveTo(1.7655f, 8.3356f, 1.5676f, 8.9447f, 1.6623f, 9.5447f)
                curveTo(1.7383f, 10.0262f, 2.0512f, 10.3902f, 2.3018f, 10.6465f)
                curveTo(2.5663f, 10.9171f, 2.9355f, 11.2333f, 3.3667f, 11.6027f)
                lineTo(5.7503f, 13.6445f)
                curveTo(5.8933f, 13.767f, 5.9697f, 13.8329f, 6.0231f, 13.8847f)
                curveTo(6.0392f, 13.9003f, 6.0489f, 13.9106f, 6.0541f, 13.9165f)
                curveTo(6.0576f, 13.9239f, 6.0602f, 13.9318f, 6.0617f, 13.9399f)
                curveTo(6.0609f, 13.9477f, 6.0591f, 13.9618f, 6.0553f, 13.9839f)
                curveTo(6.0425f, 14.0571f, 6.0195f, 14.1554f, 5.9758f, 14.3385f)
                lineTo(5.2476f, 17.3914f)
                curveTo(5.1158f, 17.9437f, 5.003f, 18.4165f, 4.9481f, 18.7909f)
                curveTo(4.896f, 19.1456f, 4.8568f, 19.624f, 5.0783f, 20.0582f)
                curveTo(5.3544f, 20.5992f, 5.8725f, 20.9757f, 6.4724f, 21.0711f)
                curveTo(6.9538f, 21.1476f, 7.3967f, 20.9624f, 7.7179f, 20.8033f)
                curveTo(8.0571f, 20.6354f, 8.4718f, 20.382f, 8.9564f, 20.086f)
                lineTo(11.6348f, 18.45f)
                curveTo(11.7955f, 18.3519f, 11.8818f, 18.2996f, 11.9476f, 18.2649f)
                curveTo(11.9674f, 18.2544f, 11.9802f, 18.2483f, 11.9874f, 18.2451f)
                curveTo(11.9956f, 18.2441f, 12.0038f, 18.2441f, 12.012f, 18.2451f)
                curveTo(12.0192f, 18.2483f, 12.032f, 18.2544f, 12.0518f, 18.2649f)
                curveTo(12.1176f, 18.2996f, 12.2039f, 18.3519f, 12.3646f, 18.45f)
                lineTo(15.043f, 20.0861f)
                curveTo(15.5275f, 20.382f, 15.9424f, 20.6354f, 16.2815f, 20.8033f)
                curveTo(16.6027f, 20.9624f, 17.0455f, 21.1476f, 17.527f, 21.0711f)
                curveTo(18.1268f, 20.9757f, 18.645f, 20.5992f, 18.921f, 20.0582f)
                curveTo(19.1426f, 19.624f, 19.1033f, 19.1456f, 19.0513f, 18.7909f)
                curveTo(18.9964f, 18.4165f, 18.8836f, 17.9437f, 18.7518f, 17.3914f)
                lineTo(18.0236f, 14.3385f)
                curveTo(17.9799f, 14.1554f, 17.9569f, 14.0571f, 17.9441f, 13.9839f)
                curveTo(17.9403f, 13.9618f, 17.9385f, 13.9477f, 17.9376f, 13.9399f)
                curveTo(17.9392f, 13.9318f, 17.9418f, 13.9239f, 17.9452f, 13.9165f)
                curveTo(17.9505f, 13.9106f, 17.9602f, 13.9003f, 17.9763f, 13.8847f)
                curveTo(18.0297f, 13.8329f, 18.1061f, 13.767f, 18.2491f, 13.6445f)
                lineTo(20.6327f, 11.6027f)
                curveTo(21.0639f, 11.2333f, 21.433f, 10.9171f, 21.6976f, 10.6465f)
                curveTo(21.9481f, 10.3902f, 22.2611f, 10.0262f, 22.3371f, 9.5447f)
                curveTo(22.4317f, 8.9447f, 22.2338f, 8.3356f, 21.8046f, 7.9058f)
                curveTo(21.4601f, 7.5609f, 20.993f, 7.4504f, 20.6396f, 7.3903f)
                curveTo(20.2665f, 7.3269f, 19.782f, 7.2881f, 19.216f, 7.2427f)
                lineTo(16.0875f, 6.9919f)
                curveTo(15.8999f, 6.9768f, 15.7993f, 6.9684f, 15.7257f, 6.9579f)
                curveTo(15.7035f, 6.9547f, 15.6896f, 6.9521f, 15.6819f, 6.9505f)
                curveTo(15.6746f, 6.9465f, 15.6679f, 6.9416f, 15.6619f, 6.936f)
                curveTo(15.658f, 6.9292f, 15.6512f, 6.9167f, 15.6413f, 6.8966f)
                curveTo(15.6086f, 6.8298f, 15.5695f, 6.7368f, 15.4972f, 6.5629f)
                lineTo(14.2919f, 3.6651f)
                curveTo(14.0738f, 3.1408f, 13.8872f, 2.692f, 13.7116f, 2.3568f)
                curveTo(13.5452f, 2.0393f, 13.2957f, 1.6292f, 12.8613f, 1.4081f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun StarFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.StarFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
