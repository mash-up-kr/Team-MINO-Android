package team.mino.core.designsystem.foundation.color.token

internal object ColorDarkTokens {
    val StaticWhite = AtomicColorToken.Common100
    val StaticBlack = AtomicColorToken.Common0

    val PrimaryNormal = AtomicColorToken.Common100
    val PrimaryStrong = AtomicColorToken.CoolNeutral95
    val PrimaryHeavy = AtomicColorToken.CoolNeutral90

    val LabelNormal = AtomicColorToken.CoolNeutral99
    val LabelStrong = AtomicColorToken.Common100
    val LabelNeutral = AtomicColorToken.CoolNeutral90.copy(alpha = AtomicOpacityToken.Opacity88)
    val LabelAlternative = AtomicColorToken.CoolNeutral80.copy(alpha = AtomicOpacityToken.Opacity61)
    val LabelAssistive = AtomicColorToken.CoolNeutral80.copy(alpha = AtomicOpacityToken.Opacity28)
    val LabelDisable = AtomicColorToken.CoolNeutral70.copy(alpha = AtomicOpacityToken.Opacity16)

    val BackgroundNormalNormal = AtomicColorToken.CoolNeutral15
    val BackgroundNormalAlternative = AtomicColorToken.CoolNeutral5
    val BackgroundElevatedNormal = AtomicColorToken.CoolNeutral17
    val BackgroundElevatedAlternative = AtomicColorToken.CoolNeutral7
    val BackgroundTransparentNormal = AtomicColorToken.CoolNeutral17.copy(alpha = AtomicOpacityToken.Opacity61)
    val BackgroundTransparentAlternative = AtomicColorToken.CoolNeutral17.copy(alpha = AtomicOpacityToken.Opacity61)

    val InteractionInactive = AtomicColorToken.CoolNeutral40
    val InteractionDisable = AtomicColorToken.CoolNeutral22

    val LineNormalNormal = AtomicColorToken.CoolNeutral50.copy(alpha = AtomicOpacityToken.Opacity32)
    val LineNormalNeutral = AtomicColorToken.CoolNeutral50.copy(alpha = AtomicOpacityToken.Opacity28)
    val LineNormalAlternative = AtomicColorToken.CoolNeutral50.copy(alpha = AtomicOpacityToken.Opacity22)
    val LineSolidNormal = AtomicColorToken.CoolNeutral25
    val LineSolidNeutral = AtomicColorToken.CoolNeutral23
    val LineSolidAlternative = AtomicColorToken.CoolNeutral22

    val StatusPositive = AtomicColorToken.Green60
    val StatusCautionary = AtomicColorToken.Orange60
    val StatusNegative = AtomicColorToken.Red60

    val AccentBackgroundRedOrange = AtomicColorToken.RedOrange60
    val AccentBackgroundLime = AtomicColorToken.Lime60
    val AccentBackgroundCyan = AtomicColorToken.Cyan60
    val AccentBackgroundLightBlue = AtomicColorToken.LightBlue60
    val AccentBackgroundViolet = AtomicColorToken.Violet60
    val AccentBackgroundPurple = AtomicColorToken.Purple60
    val AccentBackgroundPink = AtomicColorToken.Pink60

    val AccentForegroundRed = AtomicColorToken.Red60
    val AccentForegroundRedOrange = AtomicColorToken.RedOrange60
    val AccentForegroundOrange = AtomicColorToken.Orange50
    val AccentForegroundLime = AtomicColorToken.Lime50
    val AccentForegroundGreen = AtomicColorToken.Green60
    val AccentForegroundCyan = AtomicColorToken.Cyan50
    val AccentForegroundLightBlue = AtomicColorToken.LightBlue50
    val AccentForegroundBlue = AtomicColorToken.Blue65
    val AccentForegroundViolet = AtomicColorToken.Violet70
    val AccentForegroundPurple = AtomicColorToken.Purple60
    val AccentForegroundPink = AtomicColorToken.Pink60

    val InversePrimary = AtomicColorToken.CoolNeutral10
    val InverseBackground = AtomicColorToken.Common100
    val InverseLabel = AtomicColorToken.CoolNeutral10

    val FillNormal = AtomicColorToken.CoolNeutral50.copy(alpha = AtomicOpacityToken.Opacity22)
    val FillStrong = AtomicColorToken.CoolNeutral50.copy(alpha = AtomicOpacityToken.Opacity28)
    val FillAlternative = AtomicColorToken.CoolNeutral50.copy(alpha = AtomicOpacityToken.Opacity12)

    val MaterialDimmer = AtomicColorToken.CoolNeutral10.copy(alpha = AtomicOpacityToken.Opacity74)
}
