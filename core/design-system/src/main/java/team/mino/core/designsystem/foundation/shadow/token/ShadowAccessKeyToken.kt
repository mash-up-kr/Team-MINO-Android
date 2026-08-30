package team.mino.core.designsystem.foundation.shadow.token

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import team.mino.core.designsystem.foundation.shadow.MinoShadow
import team.mino.core.designsystem.foundation.shadow.fromToken
import team.mino.core.designsystem.theme.MinoAndroidTheme

internal enum class ShadowAccessKeyToken {
    NormalXsmall,
    NormalSmall,
    NormalMedium,
    NormalLarge,
    NormalXlarge,
    SpreadSmall,
    SpreadMedium,
}

internal val ShadowAccessKeyToken.value: MinoShadow
    @Composable
    @ReadOnlyComposable
    get() = MinoAndroidTheme.shadows.fromToken(this)
