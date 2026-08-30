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

val MinoIcons.Write: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Write",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(21.3864f, 2.614f)
                curveTo(20.3446f, 1.5721f, 18.6555f, 1.572f, 17.6136f, 2.6139f)
                lineTo(8.3637f, 11.8639f)
                curveTo(8.1949f, 12.0326f, 8.1001f, 12.2616f, 8.1001f, 12.5003f)
                verticalLineTo(15.0003f)
                curveTo(8.1001f, 15.4973f, 8.503f, 15.9003f, 9.0001f, 15.9003f)
                horizontalLineTo(11.5001f)
                curveTo(11.7388f, 15.9003f, 11.9677f, 15.8054f, 12.1365f, 15.6366f)
                lineTo(21.3863f, 6.3865f)
                curveTo(22.4281f, 5.3448f, 22.4281f, 3.6558f, 21.3864f, 2.614f)
                close()
                moveTo(18.8864f, 3.8867f)
                curveTo(19.2253f, 3.5478f, 19.7747f, 3.5478f, 20.1135f, 3.8867f)
                curveTo(20.4524f, 4.2256f, 20.4524f, 4.7749f, 20.1135f, 5.1137f)
                lineTo(11.1273f, 14.1003f)
                horizontalLineTo(9.9001f)
                verticalLineTo(12.873f)
                lineTo(18.8864f, 3.8867f)
                close()
            }
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(12.1027f, 3.1032f)
                curveTo(12.5982f, 3.1032f, 13f, 3.5049f, 13f, 4.0005f)
                curveTo(13f, 4.496f, 12.5982f, 4.8977f, 12.1027f, 4.8977f)
                horizontalLineTo(6.8001f)
                curveTo(5.9855f, 4.8977f, 5.7917f, 4.9088f, 5.6601f, 4.9516f)
                curveTo(5.3253f, 5.0604f, 5.0627f, 5.3229f, 4.9539f, 5.6578f)
                curveTo(4.9112f, 5.7893f, 4.9001f, 5.9832f, 4.9001f, 6.7977f)
                verticalLineTo(17.1977f)
                curveTo(4.9001f, 18.0123f, 4.9112f, 18.2061f, 4.9539f, 18.3377f)
                curveTo(5.0627f, 18.6725f, 5.3253f, 18.9351f, 5.6601f, 19.0439f)
                curveTo(5.7917f, 19.0866f, 5.9855f, 19.0977f, 6.8001f, 19.0977f)
                horizontalLineTo(17.2f)
                curveTo(18.0146f, 19.0977f, 18.2084f, 19.0866f, 18.3399f, 19.0439f)
                curveTo(18.6748f, 18.9351f, 18.9374f, 18.6725f, 19.0462f, 18.3377f)
                curveTo(19.0889f, 18.2061f, 19.1f, 18.0123f, 19.1f, 17.1977f)
                verticalLineTo(11.9008f)
                curveTo(19.1f, 11.4038f, 19.5029f, 11.0008f, 20f, 11.0008f)
                curveTo(20.497f, 11.0008f, 20.8999f, 11.4038f, 20.8999f, 11.9008f)
                verticalLineTo(16.8386f)
                curveTo(20.8999f, 17.3684f, 20.8999f, 17.8197f, 20.8696f, 18.1901f)
                curveTo(20.8378f, 18.5792f, 20.7682f, 18.9578f, 20.5838f, 19.3197f)
                curveTo(20.3058f, 19.8654f, 19.8621f, 20.3091f, 19.3165f, 20.5871f)
                curveTo(18.9545f, 20.7715f, 18.5759f, 20.8411f, 18.1868f, 20.8729f)
                curveTo(17.8164f, 20.9032f, 17.3652f, 20.9032f, 16.8354f, 20.9032f)
                horizontalLineTo(7.1645f)
                curveTo(6.6347f, 20.9032f, 6.1835f, 20.9032f, 5.8131f, 20.8729f)
                curveTo(5.4239f, 20.8411f, 5.0453f, 20.7715f, 4.6834f, 20.5871f)
                curveTo(4.1377f, 20.3091f, 3.6941f, 19.8654f, 3.416f, 19.3197f)
                curveTo(3.2316f, 18.9578f, 3.162f, 18.5792f, 3.1302f, 18.1901f)
                curveTo(3.0999f, 17.8197f, 3.0999f, 17.3685f, 3.1f, 16.8387f)
                verticalLineTo(7.1678f)
                curveTo(3.0999f, 6.638f, 3.0999f, 6.1867f, 3.1302f, 5.8163f)
                curveTo(3.162f, 5.4271f, 3.2316f, 5.0485f, 3.416f, 4.6866f)
                curveTo(3.6941f, 4.1409f, 4.1377f, 3.6973f, 4.6834f, 3.4193f)
                curveTo(5.0453f, 3.2348f, 5.4239f, 3.1652f, 5.8131f, 3.1334f)
                curveTo(6.1835f, 3.1031f, 6.6346f, 3.1032f, 7.1644f, 3.1032f)
                horizontalLineTo(12.1027f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun WritePreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Write,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
