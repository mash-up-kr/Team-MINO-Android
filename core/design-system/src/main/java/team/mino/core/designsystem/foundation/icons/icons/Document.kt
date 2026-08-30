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

val MinoIcons.Document: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "MinoIcons.Document",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF171719)), pathFillType = PathFillType.EvenOdd) {
                moveTo(13.2793f, 2.153f)
                curveTo(13.0563f, 2.0995f, 12.8293f, 2.1f, 12.6374f, 2.1004f)
                lineTo(12.5872f, 2.1005f)
                lineTo(7.9146f, 2.1005f)
                curveTo(7.3847f, 2.1005f, 6.9336f, 2.1005f, 6.5631f, 2.1307f)
                curveTo(6.174f, 2.1625f, 5.7954f, 2.2322f, 5.4334f, 2.4166f)
                curveTo(4.8878f, 2.6946f, 4.4441f, 3.1383f, 4.1661f, 3.6839f)
                curveTo(3.9817f, 4.0459f, 3.9121f, 4.4245f, 3.8803f, 4.8136f)
                curveTo(3.85f, 5.184f, 3.85f, 5.6352f, 3.85f, 6.165f)
                verticalLineTo(17.8359f)
                curveTo(3.85f, 18.3657f, 3.85f, 18.817f, 3.8803f, 19.1874f)
                curveTo(3.9121f, 19.5765f, 3.9817f, 19.9551f, 4.1661f, 20.3171f)
                curveTo(4.4441f, 20.8627f, 4.8878f, 21.3064f, 5.4334f, 21.5844f)
                curveTo(5.7954f, 21.7688f, 6.174f, 21.8385f, 6.5631f, 21.8703f)
                curveTo(6.9336f, 21.9005f, 7.3847f, 21.9005f, 7.9146f, 21.9005f)
                horizontalLineTo(16.0854f)
                curveTo(16.6152f, 21.9005f, 17.0664f, 21.9005f, 17.4369f, 21.8703f)
                curveTo(17.826f, 21.8385f, 18.2046f, 21.7688f, 18.5665f, 21.5844f)
                curveTo(19.1122f, 21.3064f, 19.5559f, 20.8627f, 19.8339f, 20.3171f)
                curveTo(20.0183f, 19.9551f, 20.0879f, 19.5765f, 20.1197f, 19.1874f)
                curveTo(20.15f, 18.817f, 20.15f, 18.3658f, 20.15f, 17.836f)
                verticalLineTo(9.6628f)
                lineTo(20.15f, 9.6126f)
                curveTo(20.1505f, 9.4207f, 20.151f, 9.1937f, 20.0975f, 8.9707f)
                curveTo(20.0509f, 8.7768f, 19.9741f, 8.5915f, 19.8699f, 8.4215f)
                curveTo(19.7501f, 8.2259f, 19.5892f, 8.0657f, 19.4532f, 7.9304f)
                lineTo(19.4177f, 7.8949f)
                lineTo(14.3549f, 2.8327f)
                lineTo(14.3195f, 2.7971f)
                curveTo(14.1841f, 2.6612f, 14.024f, 2.5003f, 13.8284f, 2.3805f)
                curveTo(13.6584f, 2.2763f, 13.4731f, 2.1995f, 13.2793f, 2.153f)
                close()
                moveTo(12.6004f, 3.9007f)
                curveTo(12.596f, 3.9007f, 12.5917f, 3.9007f, 12.5872f, 3.9007f)
                horizontalLineTo(7.95f)
                curveTo(7.3751f, 3.9007f, 6.998f, 3.9014f, 6.7097f, 3.925f)
                curveTo(6.4318f, 3.9477f, 6.3165f, 3.9871f, 6.2506f, 4.0206f)
                curveTo(6.0436f, 4.1261f, 5.8754f, 4.2944f, 5.7699f, 4.5014f)
                curveTo(5.7363f, 4.5672f, 5.697f, 4.6826f, 5.6743f, 4.9604f)
                curveTo(5.6507f, 5.2488f, 5.65f, 5.6258f, 5.65f, 6.2007f)
                verticalLineTo(17.8007f)
                curveTo(5.65f, 18.3756f, 5.6507f, 18.7527f, 5.6743f, 19.041f)
                curveTo(5.697f, 19.3189f, 5.7363f, 19.4342f, 5.7699f, 19.5001f)
                curveTo(5.8754f, 19.7071f, 6.0436f, 19.8754f, 6.2506f, 19.9808f)
                curveTo(6.3165f, 20.0144f, 6.4318f, 20.0538f, 6.7097f, 20.0765f)
                curveTo(6.998f, 20.1f, 7.3751f, 20.1007f, 7.95f, 20.1007f)
                horizontalLineTo(16.05f)
                curveTo(16.6249f, 20.1007f, 17.0019f, 20.1f, 17.2903f, 20.0765f)
                curveTo(17.5681f, 20.0538f, 17.6835f, 20.0144f, 17.7493f, 19.9808f)
                curveTo(17.9563f, 19.8754f, 18.1246f, 19.7071f, 18.2301f, 19.5001f)
                curveTo(18.2636f, 19.4342f, 18.303f, 19.3189f, 18.3257f, 19.041f)
                curveTo(18.3493f, 18.7527f, 18.35f, 18.3756f, 18.35f, 17.8007f)
                verticalLineTo(9.9004f)
                lineTo(15.0706f, 9.9004f)
                curveTo(14.8163f, 9.9005f, 14.5742f, 9.9005f, 14.3703f, 9.8838f)
                curveTo(14.1479f, 9.8657f, 13.8927f, 9.8233f, 13.6378f, 9.6934f)
                curveTo(13.2803f, 9.5112f, 12.9896f, 9.2205f, 12.8074f, 8.863f)
                curveTo(12.6775f, 8.6081f, 12.6351f, 8.3529f, 12.617f, 8.1305f)
                curveTo(12.6003f, 7.9266f, 12.6003f, 7.6846f, 12.6004f, 7.4303f)
                lineTo(12.6004f, 3.9007f)
                close()
                moveTo(17.0772f, 8.1004f)
                lineTo(14.4004f, 5.4238f)
                verticalLineTo(8.0004f)
                curveTo(14.4004f, 8.0557f, 14.4451f, 8.1004f, 14.5004f, 8.1004f)
                horizontalLineTo(17.0772f)
                close()
            }
        }.build()
}

@UiModePreviews
@Composable
private fun DocumentPreview() {
    MinoAndroidAppTheme {
        Icon(
            imageVector = MinoIcons.Document,
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }
}
