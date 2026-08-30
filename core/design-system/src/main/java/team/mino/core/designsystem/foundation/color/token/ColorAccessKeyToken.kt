package team.mino.core.designsystem.foundation.color.token

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.theme.MinoAndroidTheme

internal enum class ColorAccessKeyToken {
    StaticWhite,
    StaticBlack,
    PrimaryNormal,
    PrimaryStrong,
    PrimaryHeavy,
    LabelNormal,
    LabelStrong,
    LabelNeutral,
    LabelAlternative,
    LabelAssistive,
    LabelDisable,
    BackgroundNormalNormal,
    BackgroundNormalAlternative,
    BackgroundElevatedNormal,
    BackgroundElevatedAlternative,
    BackgroundTransparentNormal,
    BackgroundTransparentAlternative,
    InteractionInactive,
    InteractionDisable,
    LineNormalNormal,
    LineNormalNeutral,
    LineNormalAlternative,
    LineSolidNormal,
    LineSolidNeutral,
    LineSolidAlternative,
    StatusPositive,
    StatusCautionary,
    StatusNegative,
    AccentBackgroundRedOrange,
    AccentBackgroundLime,
    AccentBackgroundCyan,
    AccentBackgroundLightBlue,
    AccentBackgroundViolet,
    AccentBackgroundPurple,
    AccentBackgroundPink,
    AccentForegroundRed,
    AccentForegroundRedOrange,
    AccentForegroundOrange,
    AccentForegroundLime,
    AccentForegroundGreen,
    AccentForegroundCyan,
    AccentForegroundLightBlue,
    AccentForegroundBlue,
    AccentForegroundViolet,
    AccentForegroundPurple,
    AccentForegroundPink,
    InversePrimary,
    InverseBackground,
    InverseLabel,
    FillNormal,
    FillStrong,
    FillAlternative,
    MaterialDimmer,
}

internal val ColorAccessKeyToken.value: Color
    @Composable
    @ReadOnlyComposable
    get() = MinoAndroidTheme.colors.fromToken(this)
