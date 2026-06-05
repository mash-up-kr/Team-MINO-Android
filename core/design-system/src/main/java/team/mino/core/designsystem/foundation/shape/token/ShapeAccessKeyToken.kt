package team.mino.core.designsystem.foundation.shape.token

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Shape
import team.mino.core.designsystem.foundation.shape.fromToken
import team.mino.core.designsystem.theme.MinoAndroidTheme

internal enum class ShapeAccessKeyToken {
    Small,
    Medium,
    Large,
}

internal val ShapeAccessKeyToken.value: Shape
    @Composable
    @ReadOnlyComposable
    get() = MinoAndroidTheme.shapes.fromToken(this)
