package team.mino.core.designsystem.foundation.color.token

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.theme.MinoAndroidTheme

internal enum class ColorAccessKeyToken {
    Purple80,
    PurpleGrey80,
    Pink80,
    Purple40,
    PurpleGrey40,
    Pink40,
}

internal val ColorAccessKeyToken.value: Color
    @Composable
    @ReadOnlyComposable
    get() = MinoAndroidTheme.colors.fromToken(this)
