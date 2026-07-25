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

val MinoIcons.FolderFill: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.FolderFill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719))) {
                moveTo(8.6314f, 3.4091f)
                curveTo(8.3407f, 3.3495f, 8.0449f, 3.3497f, 7.6947f, 3.3499f)
                lineTo(6.1647f, 3.3499f)
                curveTo(5.6349f, 3.3499f, 5.1837f, 3.3499f, 4.8133f, 3.3801f)
                curveTo(4.4241f, 3.412f, 4.0455f, 3.4816f, 3.6836f, 3.666f)
                curveTo(3.1379f, 3.944f, 2.6943f, 4.3877f, 2.4162f, 4.9333f)
                curveTo(2.2318f, 5.2953f, 2.1622f, 5.6739f, 2.1304f, 6.063f)
                curveTo(2.1001f, 6.4334f, 2.1001f, 6.8846f, 2.1002f, 7.4145f)
                verticalLineTo(16.5853f)
                curveTo(2.1001f, 17.1152f, 2.1001f, 17.5664f, 2.1304f, 17.9368f)
                curveTo(2.1622f, 18.326f, 2.2318f, 18.7046f, 2.4162f, 19.0665f)
                curveTo(2.6943f, 19.6122f, 3.1379f, 20.0558f, 3.6836f, 20.3338f)
                curveTo(4.0455f, 20.5182f, 4.4241f, 20.5879f, 4.8133f, 20.6197f)
                curveTo(5.1837f, 20.6499f, 5.6349f, 20.6499f, 6.1647f, 20.6499f)
                horizontalLineTo(17.8355f)
                curveTo(18.3654f, 20.6499f, 18.8166f, 20.6499f, 19.187f, 20.6197f)
                curveTo(19.5761f, 20.5879f, 19.9547f, 20.5182f, 20.3167f, 20.3338f)
                curveTo(20.8623f, 20.0558f, 21.306f, 19.6122f, 21.584f, 19.0665f)
                curveTo(21.7684f, 18.7046f, 21.8381f, 18.326f, 21.8699f, 17.9368f)
                curveTo(21.9001f, 17.5664f, 21.9001f, 17.1152f, 21.9001f, 16.5853f)
                verticalLineTo(9.4145f)
                curveTo(21.9001f, 8.8847f, 21.9001f, 8.4335f, 21.8699f, 8.063f)
                curveTo(21.8381f, 7.6739f, 21.7684f, 7.2953f, 21.584f, 6.9333f)
                curveTo(21.306f, 6.3877f, 20.8623f, 5.944f, 20.3167f, 5.666f)
                curveTo(19.9547f, 5.4816f, 19.5761f, 5.412f, 19.187f, 5.3802f)
                curveTo(18.8166f, 5.3499f, 18.3654f, 5.3499f, 17.8355f, 5.3499f)
                lineTo(11.5656f, 5.3499f)
                lineTo(10.1362f, 4.2063f)
                curveTo(9.8629f, 3.9874f, 9.6321f, 3.8025f, 9.3677f, 3.6674f)
                curveTo(9.1351f, 3.5485f, 8.8874f, 3.4617f, 8.6314f, 3.4091f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun FolderFillPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.FolderFill,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
