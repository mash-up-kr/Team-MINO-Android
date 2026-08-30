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

val MinoIcons.CircleExclamationFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.CircleExclamationFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(2.0999f, 12.0001f)
                curveTo(2.0999f, 6.5325f, 6.5323f, 2.1001f, 11.9999f, 2.1001f)
                curveTo(17.4675f, 2.1001f, 21.8998f, 6.5325f, 21.8998f, 12.0001f)
                curveTo(21.8998f, 17.4677f, 17.4675f, 21.9001f, 11.9999f, 21.9001f)
                curveTo(6.5323f, 21.9001f, 2.0999f, 17.4677f, 2.0999f, 12.0001f)
                close()
                moveTo(12f, 7.1f)
                curveTo(12.497f, 7.1f, 12.9f, 7.5029f, 12.9f, 8f)
                verticalLineTo(12.5f)
                curveTo(12.9f, 12.9971f, 12.497f, 13.4f, 12f, 13.4f)
                curveTo(11.5029f, 13.4f, 11.1f, 12.9971f, 11.1f, 12.5f)
                verticalLineTo(8f)
                curveTo(11.1f, 7.5029f, 11.5029f, 7.1f, 12f, 7.1f)
                close()
                moveTo(12.9998f, 16f)
                curveTo(12.9998f, 16.5523f, 12.5521f, 17f, 11.9998f, 17f)
                curveTo(11.4476f, 17f, 10.9998f, 16.5523f, 10.9998f, 16f)
                curveTo(10.9998f, 15.4477f, 11.4476f, 15f, 11.9998f, 15f)
                curveTo(12.5521f, 15f, 12.9998f, 15.4477f, 12.9998f, 16f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun CircleExclamationFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.CircleExclamationFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
