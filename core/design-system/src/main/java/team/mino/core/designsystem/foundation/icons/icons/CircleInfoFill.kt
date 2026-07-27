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

val MinoIcons.CircleInfoFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.CircleInfoFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(2.1f, 12.0001f)
                curveTo(2.1f, 6.5325f, 6.5323f, 2.1001f, 11.9999f, 2.1001f)
                curveTo(17.4675f, 2.1001f, 21.8999f, 6.5325f, 21.8999f, 12.0001f)
                curveTo(21.8999f, 17.4677f, 17.4675f, 21.9001f, 11.9999f, 21.9001f)
                curveTo(6.5323f, 21.9001f, 2.1f, 17.4677f, 2.1f, 12.0001f)
                close()
                moveTo(12.9999f, 8f)
                curveTo(12.9999f, 8.5523f, 12.5522f, 9f, 11.9999f, 9f)
                curveTo(11.4476f, 9f, 10.9999f, 8.5523f, 10.9999f, 8f)
                curveTo(10.9999f, 7.4477f, 11.4476f, 7f, 11.9999f, 7f)
                curveTo(12.5522f, 7f, 12.9999f, 7.4477f, 12.9999f, 8f)
                close()
                moveTo(12f, 10.6f)
                curveTo(12.4971f, 10.6f, 12.9f, 11.0029f, 12.9f, 11.5f)
                verticalLineTo(16f)
                curveTo(12.9f, 16.4971f, 12.4971f, 16.9f, 12f, 16.9f)
                curveTo(11.503f, 16.9f, 11.1f, 16.4971f, 11.1f, 16f)
                verticalLineTo(11.5f)
                curveTo(11.1f, 11.0029f, 11.503f, 10.6f, 12f, 10.6f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun CircleInfoFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.CircleInfoFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
