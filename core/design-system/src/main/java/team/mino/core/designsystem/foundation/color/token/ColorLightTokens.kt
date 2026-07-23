package team.mino.core.designsystem.foundation.color.token

internal object ColorLightTokens {
    val StaticWhite = AtomicColorToken.Common100
    val StaticBlack = AtomicColorToken.Common0

    val PrimaryNormal = AtomicColorToken.Common0
    val PrimaryStrong = AtomicColorToken.Neutral5
    val PrimaryHeavy = AtomicColorToken.CoolNeutral10

    val LabelNormal = AtomicColorToken.CoolNeutral10
    val LabelStrong = AtomicColorToken.Common0
    val LabelNeutral = AtomicColorToken.CoolNeutral22.copy(alpha = AtomicOpacityToken.Opacity88)
    val LabelAlternative = AtomicColorToken.CoolNeutral25.copy(alpha = AtomicOpacityToken.Opacity61)
    val LabelAssistive = AtomicColorToken.CoolNeutral25.copy(alpha = AtomicOpacityToken.Opacity28)
    val LabelDisable = AtomicColorToken.CoolNeutral25.copy(alpha = AtomicOpacityToken.Opacity16)

    val BackgroundNormalNormal = AtomicColorToken.Common100
    val BackgroundNormalAlternative = AtomicColorToken.CoolNeutral99
    val BackgroundElevatedNormal = AtomicColorToken.Common100
    val BackgroundElevatedAlternative = AtomicColorToken.CoolNeutral99
    val BackgroundTransparentNormal = AtomicColorToken.Common100.copy(alpha = AtomicOpacityToken.Opacity8)
    val BackgroundTransparentAlternative = AtomicColorToken.Common100.copy(alpha = AtomicOpacityToken.Opacity28)

    val InteractionInactive = AtomicColorToken.CoolNeutral70
    val InteractionDisable = AtomicColorToken.CoolNeutral98

    val LineNormalNormal = AtomicColorToken.CoolNeutral50.copy(alpha = AtomicOpacityToken.Opacity22)
    val LineNormalNeutral = AtomicColorToken.CoolNeutral50.copy(alpha = AtomicOpacityToken.Opacity16)
    val LineNormalAlternative = AtomicColorToken.CoolNeutral50.copy(alpha = AtomicOpacityToken.Opacity8)
    val LineSolidNormal = AtomicColorToken.CoolNeutral96
    val LineSolidNeutral = AtomicColorToken.CoolNeutral97
    val LineSolidAlternative = AtomicColorToken.CoolNeutral98

    val StatusPositive = AtomicColorToken.Green50
    val StatusCautionary = AtomicColorToken.Orange50
    val StatusNegative = AtomicColorToken.Red50

    val AccentBackgroundRedOrange = AtomicColorToken.RedOrange50
    val AccentBackgroundLime = AtomicColorToken.Lime50
    val AccentBackgroundCyan = AtomicColorToken.Cyan50
    val AccentBackgroundLightBlue = AtomicColorToken.LightBlue50
    val AccentBackgroundViolet = AtomicColorToken.Violet50
    val AccentBackgroundPurple = AtomicColorToken.Purple50
    val AccentBackgroundPink = AtomicColorToken.Pink50

    val AccentForegroundRed = AtomicColorToken.Red40
    val AccentForegroundRedOrange = AtomicColorToken.RedOrange48
    val AccentForegroundOrange = AtomicColorToken.Orange39
    val AccentForegroundLime = AtomicColorToken.Lime37
    val AccentForegroundGreen = AtomicColorToken.Green40
    val AccentForegroundCyan = AtomicColorToken.Cyan40
    val AccentForegroundLightBlue = AtomicColorToken.LightBlue40
    val AccentForegroundBlue = AtomicColorToken.Blue45
    val AccentForegroundViolet = AtomicColorToken.Violet45
    val AccentForegroundPurple = AtomicColorToken.Purple40
    val AccentForegroundPink = AtomicColorToken.Pink46

    val InversePrimary = AtomicColorToken.Common100
    val InverseBackground = AtomicColorToken.CoolNeutral15
    val InverseLabel = AtomicColorToken.CoolNeutral99

    val FillNormal = AtomicColorToken.CoolNeutral50.copy(alpha = AtomicOpacityToken.Opacity8)
    val FillStrong = AtomicColorToken.CoolNeutral50.copy(alpha = AtomicOpacityToken.Opacity16)
    val FillAlternative = AtomicColorToken.CoolNeutral50.copy(alpha = AtomicOpacityToken.Opacity5)

    val MaterialDimmer = AtomicColorToken.CoolNeutral10.copy(alpha = AtomicOpacityToken.Opacity52)
}
