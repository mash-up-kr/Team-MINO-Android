package team.mino.core.designsystem.foundation.typography.token

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import team.mino.core.designsystem.foundation.typography.fromToken
import team.mino.core.designsystem.theme.MinoAndroidTheme

internal enum class TypographyAccessKeyToken {
    BodyLarge,
}

internal val TypographyAccessKeyToken.value: TextStyle
    @Composable
    @ReadOnlyComposable
    get() = MinoAndroidTheme.typography.fromToken(this)
