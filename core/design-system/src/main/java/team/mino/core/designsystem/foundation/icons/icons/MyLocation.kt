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

/** Figma `Icon/Normal/My Location`(componentSetId 3276-209734) — GPS 십자선 아이콘, 20x20 프레임. */
val MinoIcons.MyLocation: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.MyLocation",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(10.4717f, 0.845215f)
                curveTo(15.3149f, 1.09087f, 19.166f, 5.09618f, 19.166f, 10.0005f)
                lineTo(19.1543f, 10.4722f)
                curveTo(18.9086f, 15.3153f, 14.9042f, 19.1663f, 10f, 19.1665f)
                lineTo(9.52832f, 19.1548f)
                curveTo(4.8411f, 18.9174f, 1.08246f, 15.1593f, 0.844727f, 10.4722f)
                lineTo(0.833008f, 10.0005f)
                curveTo(0.833008f, 4.93788f, 4.93739f, 0.833496f, 10f, 0.833496f)
                lineTo(10.4717f, 0.845215f)
                close()
                moveTo(10.833f, 5.8335f)
                curveTo(10.833f, 6.29355f, 10.46f, 6.66628f, 10f, 6.6665f)
                curveTo(9.53979f, 6.6665f, 9.16607f, 6.29369f, 9.16602f, 5.8335f)
                lineTo(9.16602f, 2.32471f)
                curveTo(5.56845f, 2.71119f, 2.71173f, 5.56895f, 2.3252f, 9.1665f)
                lineTo(5.83301f, 9.1665f)
                curveTo(6.2932f, 9.16655f, 6.66602f, 9.54028f, 6.66602f, 10.0005f)
                curveTo(6.66579f, 10.4605f, 6.29306f, 10.8334f, 5.83301f, 10.8335f)
                lineTo(2.3252f, 10.8335f)
                curveTo(2.71146f, 14.4312f, 5.56829f, 17.2878f, 9.16602f, 17.6743f)
                lineTo(9.16602f, 14.1665f)
                curveTo(9.16614f, 13.7064f, 9.53984f, 13.3335f, 10f, 13.3335f)
                curveTo(10.46f, 13.3337f, 10.8329f, 13.7065f, 10.833f, 14.1665f)
                lineTo(10.833f, 17.6743f)
                curveTo(14.431f, 17.2881f, 17.2876f, 14.4315f, 17.6738f, 10.8335f)
                lineTo(14.166f, 10.8335f)
                curveTo(13.706f, 10.8334f, 13.3332f, 10.4605f, 13.333f, 10.0005f)
                curveTo(13.333f, 9.54033f, 13.7059f, 9.16663f, 14.166f, 9.1665f)
                lineTo(17.6738f, 9.1665f)
                curveTo(17.2873f, 5.56874f, 14.4308f, 2.71092f, 10.833f, 2.32471f)
                lineTo(10.833f, 5.8335f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun MyLocationPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.MyLocation,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(20.dp),
            tint = ColorAccessKeyToken.LabelAlternative.value,
        )
    }
}
