package team.mino.core.designsystem.foundation.color

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.component.bottomnavigation.MinoBottomNavigationItemColors
import team.mino.core.designsystem.component.button.MinoButtonColors
import team.mino.core.designsystem.component.button.MinoTextButtonColors
import team.mino.core.designsystem.component.category.MinoCategoryColors
import team.mino.core.designsystem.component.chip.MinoChipColors
import team.mino.core.designsystem.component.contentbadge.MinoContentBadgeColors
import team.mino.core.designsystem.component.menu.MinoMenuItemColors
import team.mino.core.designsystem.component.textinput.MinoTextInputColors
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.foundation.color.token.ColorDarkTokens
import team.mino.core.designsystem.foundation.color.token.ColorLightTokens

@Immutable
class ColorScheme(
    val staticWhite: Color,
    val staticBlack: Color,
    val primaryNormal: Color,
    val primaryStrong: Color,
    val primaryHeavy: Color,
    val labelNormal: Color,
    val labelStrong: Color,
    val labelNeutral: Color,
    val labelAlternative: Color,
    val labelAssistive: Color,
    val labelDisable: Color,
    val backgroundNormalNormal: Color,
    val backgroundNormalAlternative: Color,
    val backgroundElevatedNormal: Color,
    val backgroundElevatedAlternative: Color,
    val backgroundTransparentNormal: Color,
    val backgroundTransparentAlternative: Color,
    val interactionInactive: Color,
    val interactionDisable: Color,
    val lineNormalNormal: Color,
    val lineNormalNeutral: Color,
    val lineNormalAlternative: Color,
    val lineSolidNormal: Color,
    val lineSolidNeutral: Color,
    val lineSolidAlternative: Color,
    val statusPositive: Color,
    val statusCautionary: Color,
    val statusNegative: Color,
    val accentBackgroundRedOrange: Color,
    val accentBackgroundLime: Color,
    val accentBackgroundCyan: Color,
    val accentBackgroundLightBlue: Color,
    val accentBackgroundViolet: Color,
    val accentBackgroundPurple: Color,
    val accentBackgroundPink: Color,
    val accentForegroundRed: Color,
    val accentForegroundRedOrange: Color,
    val accentForegroundOrange: Color,
    val accentForegroundLime: Color,
    val accentForegroundGreen: Color,
    val accentForegroundCyan: Color,
    val accentForegroundLightBlue: Color,
    val accentForegroundBlue: Color,
    val accentForegroundViolet: Color,
    val accentForegroundPurple: Color,
    val accentForegroundPink: Color,
    val inversePrimary: Color,
    val inverseBackground: Color,
    val inverseLabel: Color,
    val fillNormal: Color,
    val fillStrong: Color,
    val fillAlternative: Color,
    val materialDimmer: Color,
) {
    // 컴포넌트 기본 Colors 캐시. M3 ColorScheme의 default*ColorsCached와 같은 방식으로,
    // 스킴(라이트/다크)당 1회만 생성해 재사용한다 (docs/adr/2026-07-25-design-system-component-m3-pattern.md).
    internal var defaultBottomNavigationItemColorsCached: MinoBottomNavigationItemColors? = null
    internal var solidPrimaryButtonColorsCached: MinoButtonColors? = null
    internal var solidAssistiveButtonColorsCached: MinoButtonColors? = null
    internal var outlinedPrimaryButtonColorsCached: MinoButtonColors? = null
    internal var outlinedAssistiveButtonColorsCached: MinoButtonColors? = null
    internal var primaryTextButtonColorsCached: MinoTextButtonColors? = null
    internal var assistiveTextButtonColorsCached: MinoTextButtonColors? = null
    internal var normalCategoryColorsCached: MinoCategoryColors? = null
    internal var alternativeCategoryColorsCached: MinoCategoryColors? = null
    internal var defaultChipColorsCached: MinoChipColors? = null
    internal var defaultContentBadgeColorsCached: MinoContentBadgeColors? = null
    internal var accentContentBadgeColorsCached: MinoContentBadgeColors? = null
    internal var defaultMenuItemColorsCached: MinoMenuItemColors? = null
    internal var defaultTextInputColorsCached: MinoTextInputColors? = null

    fun copy(
        staticWhite: Color = this.staticWhite,
        staticBlack: Color = this.staticBlack,
        primaryNormal: Color = this.primaryNormal,
        primaryStrong: Color = this.primaryStrong,
        primaryHeavy: Color = this.primaryHeavy,
        labelNormal: Color = this.labelNormal,
        labelStrong: Color = this.labelStrong,
        labelNeutral: Color = this.labelNeutral,
        labelAlternative: Color = this.labelAlternative,
        labelAssistive: Color = this.labelAssistive,
        labelDisable: Color = this.labelDisable,
        backgroundNormalNormal: Color = this.backgroundNormalNormal,
        backgroundNormalAlternative: Color = this.backgroundNormalAlternative,
        backgroundElevatedNormal: Color = this.backgroundElevatedNormal,
        backgroundElevatedAlternative: Color = this.backgroundElevatedAlternative,
        backgroundTransparentNormal: Color = this.backgroundTransparentNormal,
        backgroundTransparentAlternative: Color = this.backgroundTransparentAlternative,
        interactionInactive: Color = this.interactionInactive,
        interactionDisable: Color = this.interactionDisable,
        lineNormalNormal: Color = this.lineNormalNormal,
        lineNormalNeutral: Color = this.lineNormalNeutral,
        lineNormalAlternative: Color = this.lineNormalAlternative,
        lineSolidNormal: Color = this.lineSolidNormal,
        lineSolidNeutral: Color = this.lineSolidNeutral,
        lineSolidAlternative: Color = this.lineSolidAlternative,
        statusPositive: Color = this.statusPositive,
        statusCautionary: Color = this.statusCautionary,
        statusNegative: Color = this.statusNegative,
        accentBackgroundRedOrange: Color = this.accentBackgroundRedOrange,
        accentBackgroundLime: Color = this.accentBackgroundLime,
        accentBackgroundCyan: Color = this.accentBackgroundCyan,
        accentBackgroundLightBlue: Color = this.accentBackgroundLightBlue,
        accentBackgroundViolet: Color = this.accentBackgroundViolet,
        accentBackgroundPurple: Color = this.accentBackgroundPurple,
        accentBackgroundPink: Color = this.accentBackgroundPink,
        accentForegroundRed: Color = this.accentForegroundRed,
        accentForegroundRedOrange: Color = this.accentForegroundRedOrange,
        accentForegroundOrange: Color = this.accentForegroundOrange,
        accentForegroundLime: Color = this.accentForegroundLime,
        accentForegroundGreen: Color = this.accentForegroundGreen,
        accentForegroundCyan: Color = this.accentForegroundCyan,
        accentForegroundLightBlue: Color = this.accentForegroundLightBlue,
        accentForegroundBlue: Color = this.accentForegroundBlue,
        accentForegroundViolet: Color = this.accentForegroundViolet,
        accentForegroundPurple: Color = this.accentForegroundPurple,
        accentForegroundPink: Color = this.accentForegroundPink,
        inversePrimary: Color = this.inversePrimary,
        inverseBackground: Color = this.inverseBackground,
        inverseLabel: Color = this.inverseLabel,
        fillNormal: Color = this.fillNormal,
        fillStrong: Color = this.fillStrong,
        fillAlternative: Color = this.fillAlternative,
        materialDimmer: Color = this.materialDimmer,
    ): ColorScheme =
        ColorScheme(
            staticWhite = staticWhite,
            staticBlack = staticBlack,
            primaryNormal = primaryNormal,
            primaryStrong = primaryStrong,
            primaryHeavy = primaryHeavy,
            labelNormal = labelNormal,
            labelStrong = labelStrong,
            labelNeutral = labelNeutral,
            labelAlternative = labelAlternative,
            labelAssistive = labelAssistive,
            labelDisable = labelDisable,
            backgroundNormalNormal = backgroundNormalNormal,
            backgroundNormalAlternative = backgroundNormalAlternative,
            backgroundElevatedNormal = backgroundElevatedNormal,
            backgroundElevatedAlternative = backgroundElevatedAlternative,
            backgroundTransparentNormal = backgroundTransparentNormal,
            backgroundTransparentAlternative = backgroundTransparentAlternative,
            interactionInactive = interactionInactive,
            interactionDisable = interactionDisable,
            lineNormalNormal = lineNormalNormal,
            lineNormalNeutral = lineNormalNeutral,
            lineNormalAlternative = lineNormalAlternative,
            lineSolidNormal = lineSolidNormal,
            lineSolidNeutral = lineSolidNeutral,
            lineSolidAlternative = lineSolidAlternative,
            statusPositive = statusPositive,
            statusCautionary = statusCautionary,
            statusNegative = statusNegative,
            accentBackgroundRedOrange = accentBackgroundRedOrange,
            accentBackgroundLime = accentBackgroundLime,
            accentBackgroundCyan = accentBackgroundCyan,
            accentBackgroundLightBlue = accentBackgroundLightBlue,
            accentBackgroundViolet = accentBackgroundViolet,
            accentBackgroundPurple = accentBackgroundPurple,
            accentBackgroundPink = accentBackgroundPink,
            accentForegroundRed = accentForegroundRed,
            accentForegroundRedOrange = accentForegroundRedOrange,
            accentForegroundOrange = accentForegroundOrange,
            accentForegroundLime = accentForegroundLime,
            accentForegroundGreen = accentForegroundGreen,
            accentForegroundCyan = accentForegroundCyan,
            accentForegroundLightBlue = accentForegroundLightBlue,
            accentForegroundBlue = accentForegroundBlue,
            accentForegroundViolet = accentForegroundViolet,
            accentForegroundPurple = accentForegroundPurple,
            accentForegroundPink = accentForegroundPink,
            inversePrimary = inversePrimary,
            inverseBackground = inverseBackground,
            inverseLabel = inverseLabel,
            fillNormal = fillNormal,
            fillStrong = fillStrong,
            fillAlternative = fillAlternative,
            materialDimmer = materialDimmer,
        )

    override fun toString(): String =
        "ColorScheme(" +
            "staticWhite=$staticWhite, " +
            "staticBlack=$staticBlack, " +
            "primaryNormal=$primaryNormal, " +
            "primaryStrong=$primaryStrong, " +
            "primaryHeavy=$primaryHeavy, " +
            "labelNormal=$labelNormal, " +
            "labelStrong=$labelStrong, " +
            "labelNeutral=$labelNeutral, " +
            "labelAlternative=$labelAlternative, " +
            "labelAssistive=$labelAssistive, " +
            "labelDisable=$labelDisable, " +
            "backgroundNormalNormal=$backgroundNormalNormal, " +
            "backgroundNormalAlternative=$backgroundNormalAlternative, " +
            "backgroundElevatedNormal=$backgroundElevatedNormal, " +
            "backgroundElevatedAlternative=$backgroundElevatedAlternative, " +
            "backgroundTransparentNormal=$backgroundTransparentNormal, " +
            "backgroundTransparentAlternative=$backgroundTransparentAlternative, " +
            "interactionInactive=$interactionInactive, " +
            "interactionDisable=$interactionDisable, " +
            "lineNormalNormal=$lineNormalNormal, " +
            "lineNormalNeutral=$lineNormalNeutral, " +
            "lineNormalAlternative=$lineNormalAlternative, " +
            "lineSolidNormal=$lineSolidNormal, " +
            "lineSolidNeutral=$lineSolidNeutral, " +
            "lineSolidAlternative=$lineSolidAlternative, " +
            "statusPositive=$statusPositive, " +
            "statusCautionary=$statusCautionary, " +
            "statusNegative=$statusNegative, " +
            "accentBackgroundRedOrange=$accentBackgroundRedOrange, " +
            "accentBackgroundLime=$accentBackgroundLime, " +
            "accentBackgroundCyan=$accentBackgroundCyan, " +
            "accentBackgroundLightBlue=$accentBackgroundLightBlue, " +
            "accentBackgroundViolet=$accentBackgroundViolet, " +
            "accentBackgroundPurple=$accentBackgroundPurple, " +
            "accentBackgroundPink=$accentBackgroundPink, " +
            "accentForegroundRed=$accentForegroundRed, " +
            "accentForegroundRedOrange=$accentForegroundRedOrange, " +
            "accentForegroundOrange=$accentForegroundOrange, " +
            "accentForegroundLime=$accentForegroundLime, " +
            "accentForegroundGreen=$accentForegroundGreen, " +
            "accentForegroundCyan=$accentForegroundCyan, " +
            "accentForegroundLightBlue=$accentForegroundLightBlue, " +
            "accentForegroundBlue=$accentForegroundBlue, " +
            "accentForegroundViolet=$accentForegroundViolet, " +
            "accentForegroundPurple=$accentForegroundPurple, " +
            "accentForegroundPink=$accentForegroundPink, " +
            "inversePrimary=$inversePrimary, " +
            "inverseBackground=$inverseBackground, " +
            "inverseLabel=$inverseLabel, " +
            "fillNormal=$fillNormal, " +
            "fillStrong=$fillStrong, " +
            "fillAlternative=$fillAlternative, " +
            "materialDimmer=$materialDimmer" +
            ")"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColorScheme) return false

        if (staticWhite != other.staticWhite) return false
        if (staticBlack != other.staticBlack) return false
        if (primaryNormal != other.primaryNormal) return false
        if (primaryStrong != other.primaryStrong) return false
        if (primaryHeavy != other.primaryHeavy) return false
        if (labelNormal != other.labelNormal) return false
        if (labelStrong != other.labelStrong) return false
        if (labelNeutral != other.labelNeutral) return false
        if (labelAlternative != other.labelAlternative) return false
        if (labelAssistive != other.labelAssistive) return false
        if (labelDisable != other.labelDisable) return false
        if (backgroundNormalNormal != other.backgroundNormalNormal) return false
        if (backgroundNormalAlternative != other.backgroundNormalAlternative) return false
        if (backgroundElevatedNormal != other.backgroundElevatedNormal) return false
        if (backgroundElevatedAlternative != other.backgroundElevatedAlternative) return false
        if (backgroundTransparentNormal != other.backgroundTransparentNormal) return false
        if (backgroundTransparentAlternative != other.backgroundTransparentAlternative) return false
        if (interactionInactive != other.interactionInactive) return false
        if (interactionDisable != other.interactionDisable) return false
        if (lineNormalNormal != other.lineNormalNormal) return false
        if (lineNormalNeutral != other.lineNormalNeutral) return false
        if (lineNormalAlternative != other.lineNormalAlternative) return false
        if (lineSolidNormal != other.lineSolidNormal) return false
        if (lineSolidNeutral != other.lineSolidNeutral) return false
        if (lineSolidAlternative != other.lineSolidAlternative) return false
        if (statusPositive != other.statusPositive) return false
        if (statusCautionary != other.statusCautionary) return false
        if (statusNegative != other.statusNegative) return false
        if (accentBackgroundRedOrange != other.accentBackgroundRedOrange) return false
        if (accentBackgroundLime != other.accentBackgroundLime) return false
        if (accentBackgroundCyan != other.accentBackgroundCyan) return false
        if (accentBackgroundLightBlue != other.accentBackgroundLightBlue) return false
        if (accentBackgroundViolet != other.accentBackgroundViolet) return false
        if (accentBackgroundPurple != other.accentBackgroundPurple) return false
        if (accentBackgroundPink != other.accentBackgroundPink) return false
        if (accentForegroundRed != other.accentForegroundRed) return false
        if (accentForegroundRedOrange != other.accentForegroundRedOrange) return false
        if (accentForegroundOrange != other.accentForegroundOrange) return false
        if (accentForegroundLime != other.accentForegroundLime) return false
        if (accentForegroundGreen != other.accentForegroundGreen) return false
        if (accentForegroundCyan != other.accentForegroundCyan) return false
        if (accentForegroundLightBlue != other.accentForegroundLightBlue) return false
        if (accentForegroundBlue != other.accentForegroundBlue) return false
        if (accentForegroundViolet != other.accentForegroundViolet) return false
        if (accentForegroundPurple != other.accentForegroundPurple) return false
        if (accentForegroundPink != other.accentForegroundPink) return false
        if (inversePrimary != other.inversePrimary) return false
        if (inverseBackground != other.inverseBackground) return false
        if (inverseLabel != other.inverseLabel) return false
        if (fillNormal != other.fillNormal) return false
        if (fillStrong != other.fillStrong) return false
        if (fillAlternative != other.fillAlternative) return false
        if (materialDimmer != other.materialDimmer) return false

        return true
    }

    override fun hashCode(): Int =
        arrayOf(
            staticWhite,
            staticBlack,
            primaryNormal,
            primaryStrong,
            primaryHeavy,
            labelNormal,
            labelStrong,
            labelNeutral,
            labelAlternative,
            labelAssistive,
            labelDisable,
            backgroundNormalNormal,
            backgroundNormalAlternative,
            backgroundElevatedNormal,
            backgroundElevatedAlternative,
            backgroundTransparentNormal,
            backgroundTransparentAlternative,
            interactionInactive,
            interactionDisable,
            lineNormalNormal,
            lineNormalNeutral,
            lineNormalAlternative,
            lineSolidNormal,
            lineSolidNeutral,
            lineSolidAlternative,
            statusPositive,
            statusCautionary,
            statusNegative,
            accentBackgroundRedOrange,
            accentBackgroundLime,
            accentBackgroundCyan,
            accentBackgroundLightBlue,
            accentBackgroundViolet,
            accentBackgroundPurple,
            accentBackgroundPink,
            accentForegroundRed,
            accentForegroundRedOrange,
            accentForegroundOrange,
            accentForegroundLime,
            accentForegroundGreen,
            accentForegroundCyan,
            accentForegroundLightBlue,
            accentForegroundBlue,
            accentForegroundViolet,
            accentForegroundPurple,
            accentForegroundPink,
            inversePrimary,
            inverseBackground,
            inverseLabel,
            fillNormal,
            fillStrong,
            fillAlternative,
            materialDimmer,
        ).contentHashCode()
}

internal fun lightColorScheme(
    staticWhite: Color = ColorLightTokens.StaticWhite,
    staticBlack: Color = ColorLightTokens.StaticBlack,
    primaryNormal: Color = ColorLightTokens.PrimaryNormal,
    primaryStrong: Color = ColorLightTokens.PrimaryStrong,
    primaryHeavy: Color = ColorLightTokens.PrimaryHeavy,
    labelNormal: Color = ColorLightTokens.LabelNormal,
    labelStrong: Color = ColorLightTokens.LabelStrong,
    labelNeutral: Color = ColorLightTokens.LabelNeutral,
    labelAlternative: Color = ColorLightTokens.LabelAlternative,
    labelAssistive: Color = ColorLightTokens.LabelAssistive,
    labelDisable: Color = ColorLightTokens.LabelDisable,
    backgroundNormalNormal: Color = ColorLightTokens.BackgroundNormalNormal,
    backgroundNormalAlternative: Color = ColorLightTokens.BackgroundNormalAlternative,
    backgroundElevatedNormal: Color = ColorLightTokens.BackgroundElevatedNormal,
    backgroundElevatedAlternative: Color = ColorLightTokens.BackgroundElevatedAlternative,
    backgroundTransparentNormal: Color = ColorLightTokens.BackgroundTransparentNormal,
    backgroundTransparentAlternative: Color = ColorLightTokens.BackgroundTransparentAlternative,
    interactionInactive: Color = ColorLightTokens.InteractionInactive,
    interactionDisable: Color = ColorLightTokens.InteractionDisable,
    lineNormalNormal: Color = ColorLightTokens.LineNormalNormal,
    lineNormalNeutral: Color = ColorLightTokens.LineNormalNeutral,
    lineNormalAlternative: Color = ColorLightTokens.LineNormalAlternative,
    lineSolidNormal: Color = ColorLightTokens.LineSolidNormal,
    lineSolidNeutral: Color = ColorLightTokens.LineSolidNeutral,
    lineSolidAlternative: Color = ColorLightTokens.LineSolidAlternative,
    statusPositive: Color = ColorLightTokens.StatusPositive,
    statusCautionary: Color = ColorLightTokens.StatusCautionary,
    statusNegative: Color = ColorLightTokens.StatusNegative,
    accentBackgroundRedOrange: Color = ColorLightTokens.AccentBackgroundRedOrange,
    accentBackgroundLime: Color = ColorLightTokens.AccentBackgroundLime,
    accentBackgroundCyan: Color = ColorLightTokens.AccentBackgroundCyan,
    accentBackgroundLightBlue: Color = ColorLightTokens.AccentBackgroundLightBlue,
    accentBackgroundViolet: Color = ColorLightTokens.AccentBackgroundViolet,
    accentBackgroundPurple: Color = ColorLightTokens.AccentBackgroundPurple,
    accentBackgroundPink: Color = ColorLightTokens.AccentBackgroundPink,
    accentForegroundRed: Color = ColorLightTokens.AccentForegroundRed,
    accentForegroundRedOrange: Color = ColorLightTokens.AccentForegroundRedOrange,
    accentForegroundOrange: Color = ColorLightTokens.AccentForegroundOrange,
    accentForegroundLime: Color = ColorLightTokens.AccentForegroundLime,
    accentForegroundGreen: Color = ColorLightTokens.AccentForegroundGreen,
    accentForegroundCyan: Color = ColorLightTokens.AccentForegroundCyan,
    accentForegroundLightBlue: Color = ColorLightTokens.AccentForegroundLightBlue,
    accentForegroundBlue: Color = ColorLightTokens.AccentForegroundBlue,
    accentForegroundViolet: Color = ColorLightTokens.AccentForegroundViolet,
    accentForegroundPurple: Color = ColorLightTokens.AccentForegroundPurple,
    accentForegroundPink: Color = ColorLightTokens.AccentForegroundPink,
    inversePrimary: Color = ColorLightTokens.InversePrimary,
    inverseBackground: Color = ColorLightTokens.InverseBackground,
    inverseLabel: Color = ColorLightTokens.InverseLabel,
    fillNormal: Color = ColorLightTokens.FillNormal,
    fillStrong: Color = ColorLightTokens.FillStrong,
    fillAlternative: Color = ColorLightTokens.FillAlternative,
    materialDimmer: Color = ColorLightTokens.MaterialDimmer,
): ColorScheme =
    ColorScheme(
        staticWhite = staticWhite,
        staticBlack = staticBlack,
        primaryNormal = primaryNormal,
        primaryStrong = primaryStrong,
        primaryHeavy = primaryHeavy,
        labelNormal = labelNormal,
        labelStrong = labelStrong,
        labelNeutral = labelNeutral,
        labelAlternative = labelAlternative,
        labelAssistive = labelAssistive,
        labelDisable = labelDisable,
        backgroundNormalNormal = backgroundNormalNormal,
        backgroundNormalAlternative = backgroundNormalAlternative,
        backgroundElevatedNormal = backgroundElevatedNormal,
        backgroundElevatedAlternative = backgroundElevatedAlternative,
        backgroundTransparentNormal = backgroundTransparentNormal,
        backgroundTransparentAlternative = backgroundTransparentAlternative,
        interactionInactive = interactionInactive,
        interactionDisable = interactionDisable,
        lineNormalNormal = lineNormalNormal,
        lineNormalNeutral = lineNormalNeutral,
        lineNormalAlternative = lineNormalAlternative,
        lineSolidNormal = lineSolidNormal,
        lineSolidNeutral = lineSolidNeutral,
        lineSolidAlternative = lineSolidAlternative,
        statusPositive = statusPositive,
        statusCautionary = statusCautionary,
        statusNegative = statusNegative,
        accentBackgroundRedOrange = accentBackgroundRedOrange,
        accentBackgroundLime = accentBackgroundLime,
        accentBackgroundCyan = accentBackgroundCyan,
        accentBackgroundLightBlue = accentBackgroundLightBlue,
        accentBackgroundViolet = accentBackgroundViolet,
        accentBackgroundPurple = accentBackgroundPurple,
        accentBackgroundPink = accentBackgroundPink,
        accentForegroundRed = accentForegroundRed,
        accentForegroundRedOrange = accentForegroundRedOrange,
        accentForegroundOrange = accentForegroundOrange,
        accentForegroundLime = accentForegroundLime,
        accentForegroundGreen = accentForegroundGreen,
        accentForegroundCyan = accentForegroundCyan,
        accentForegroundLightBlue = accentForegroundLightBlue,
        accentForegroundBlue = accentForegroundBlue,
        accentForegroundViolet = accentForegroundViolet,
        accentForegroundPurple = accentForegroundPurple,
        accentForegroundPink = accentForegroundPink,
        inversePrimary = inversePrimary,
        inverseBackground = inverseBackground,
        inverseLabel = inverseLabel,
        fillNormal = fillNormal,
        fillStrong = fillStrong,
        fillAlternative = fillAlternative,
        materialDimmer = materialDimmer,
    )

internal fun darkColorScheme(
    staticWhite: Color = ColorDarkTokens.StaticWhite,
    staticBlack: Color = ColorDarkTokens.StaticBlack,
    primaryNormal: Color = ColorDarkTokens.PrimaryNormal,
    primaryStrong: Color = ColorDarkTokens.PrimaryStrong,
    primaryHeavy: Color = ColorDarkTokens.PrimaryHeavy,
    labelNormal: Color = ColorDarkTokens.LabelNormal,
    labelStrong: Color = ColorDarkTokens.LabelStrong,
    labelNeutral: Color = ColorDarkTokens.LabelNeutral,
    labelAlternative: Color = ColorDarkTokens.LabelAlternative,
    labelAssistive: Color = ColorDarkTokens.LabelAssistive,
    labelDisable: Color = ColorDarkTokens.LabelDisable,
    backgroundNormalNormal: Color = ColorDarkTokens.BackgroundNormalNormal,
    backgroundNormalAlternative: Color = ColorDarkTokens.BackgroundNormalAlternative,
    backgroundElevatedNormal: Color = ColorDarkTokens.BackgroundElevatedNormal,
    backgroundElevatedAlternative: Color = ColorDarkTokens.BackgroundElevatedAlternative,
    backgroundTransparentNormal: Color = ColorDarkTokens.BackgroundTransparentNormal,
    backgroundTransparentAlternative: Color = ColorDarkTokens.BackgroundTransparentAlternative,
    interactionInactive: Color = ColorDarkTokens.InteractionInactive,
    interactionDisable: Color = ColorDarkTokens.InteractionDisable,
    lineNormalNormal: Color = ColorDarkTokens.LineNormalNormal,
    lineNormalNeutral: Color = ColorDarkTokens.LineNormalNeutral,
    lineNormalAlternative: Color = ColorDarkTokens.LineNormalAlternative,
    lineSolidNormal: Color = ColorDarkTokens.LineSolidNormal,
    lineSolidNeutral: Color = ColorDarkTokens.LineSolidNeutral,
    lineSolidAlternative: Color = ColorDarkTokens.LineSolidAlternative,
    statusPositive: Color = ColorDarkTokens.StatusPositive,
    statusCautionary: Color = ColorDarkTokens.StatusCautionary,
    statusNegative: Color = ColorDarkTokens.StatusNegative,
    accentBackgroundRedOrange: Color = ColorDarkTokens.AccentBackgroundRedOrange,
    accentBackgroundLime: Color = ColorDarkTokens.AccentBackgroundLime,
    accentBackgroundCyan: Color = ColorDarkTokens.AccentBackgroundCyan,
    accentBackgroundLightBlue: Color = ColorDarkTokens.AccentBackgroundLightBlue,
    accentBackgroundViolet: Color = ColorDarkTokens.AccentBackgroundViolet,
    accentBackgroundPurple: Color = ColorDarkTokens.AccentBackgroundPurple,
    accentBackgroundPink: Color = ColorDarkTokens.AccentBackgroundPink,
    accentForegroundRed: Color = ColorDarkTokens.AccentForegroundRed,
    accentForegroundRedOrange: Color = ColorDarkTokens.AccentForegroundRedOrange,
    accentForegroundOrange: Color = ColorDarkTokens.AccentForegroundOrange,
    accentForegroundLime: Color = ColorDarkTokens.AccentForegroundLime,
    accentForegroundGreen: Color = ColorDarkTokens.AccentForegroundGreen,
    accentForegroundCyan: Color = ColorDarkTokens.AccentForegroundCyan,
    accentForegroundLightBlue: Color = ColorDarkTokens.AccentForegroundLightBlue,
    accentForegroundBlue: Color = ColorDarkTokens.AccentForegroundBlue,
    accentForegroundViolet: Color = ColorDarkTokens.AccentForegroundViolet,
    accentForegroundPurple: Color = ColorDarkTokens.AccentForegroundPurple,
    accentForegroundPink: Color = ColorDarkTokens.AccentForegroundPink,
    inversePrimary: Color = ColorDarkTokens.InversePrimary,
    inverseBackground: Color = ColorDarkTokens.InverseBackground,
    inverseLabel: Color = ColorDarkTokens.InverseLabel,
    fillNormal: Color = ColorDarkTokens.FillNormal,
    fillStrong: Color = ColorDarkTokens.FillStrong,
    fillAlternative: Color = ColorDarkTokens.FillAlternative,
    materialDimmer: Color = ColorDarkTokens.MaterialDimmer,
): ColorScheme =
    ColorScheme(
        staticWhite = staticWhite,
        staticBlack = staticBlack,
        primaryNormal = primaryNormal,
        primaryStrong = primaryStrong,
        primaryHeavy = primaryHeavy,
        labelNormal = labelNormal,
        labelStrong = labelStrong,
        labelNeutral = labelNeutral,
        labelAlternative = labelAlternative,
        labelAssistive = labelAssistive,
        labelDisable = labelDisable,
        backgroundNormalNormal = backgroundNormalNormal,
        backgroundNormalAlternative = backgroundNormalAlternative,
        backgroundElevatedNormal = backgroundElevatedNormal,
        backgroundElevatedAlternative = backgroundElevatedAlternative,
        backgroundTransparentNormal = backgroundTransparentNormal,
        backgroundTransparentAlternative = backgroundTransparentAlternative,
        interactionInactive = interactionInactive,
        interactionDisable = interactionDisable,
        lineNormalNormal = lineNormalNormal,
        lineNormalNeutral = lineNormalNeutral,
        lineNormalAlternative = lineNormalAlternative,
        lineSolidNormal = lineSolidNormal,
        lineSolidNeutral = lineSolidNeutral,
        lineSolidAlternative = lineSolidAlternative,
        statusPositive = statusPositive,
        statusCautionary = statusCautionary,
        statusNegative = statusNegative,
        accentBackgroundRedOrange = accentBackgroundRedOrange,
        accentBackgroundLime = accentBackgroundLime,
        accentBackgroundCyan = accentBackgroundCyan,
        accentBackgroundLightBlue = accentBackgroundLightBlue,
        accentBackgroundViolet = accentBackgroundViolet,
        accentBackgroundPurple = accentBackgroundPurple,
        accentBackgroundPink = accentBackgroundPink,
        accentForegroundRed = accentForegroundRed,
        accentForegroundRedOrange = accentForegroundRedOrange,
        accentForegroundOrange = accentForegroundOrange,
        accentForegroundLime = accentForegroundLime,
        accentForegroundGreen = accentForegroundGreen,
        accentForegroundCyan = accentForegroundCyan,
        accentForegroundLightBlue = accentForegroundLightBlue,
        accentForegroundBlue = accentForegroundBlue,
        accentForegroundViolet = accentForegroundViolet,
        accentForegroundPurple = accentForegroundPurple,
        accentForegroundPink = accentForegroundPink,
        inversePrimary = inversePrimary,
        inverseBackground = inverseBackground,
        inverseLabel = inverseLabel,
        fillNormal = fillNormal,
        fillStrong = fillStrong,
        fillAlternative = fillAlternative,
        materialDimmer = materialDimmer,
    )

internal fun ColorScheme.fromToken(value: ColorAccessKeyToken): Color =
    when (value) {
        ColorAccessKeyToken.StaticWhite -> staticWhite
        ColorAccessKeyToken.StaticBlack -> staticBlack
        ColorAccessKeyToken.PrimaryNormal -> primaryNormal
        ColorAccessKeyToken.PrimaryStrong -> primaryStrong
        ColorAccessKeyToken.PrimaryHeavy -> primaryHeavy
        ColorAccessKeyToken.LabelNormal -> labelNormal
        ColorAccessKeyToken.LabelStrong -> labelStrong
        ColorAccessKeyToken.LabelNeutral -> labelNeutral
        ColorAccessKeyToken.LabelAlternative -> labelAlternative
        ColorAccessKeyToken.LabelAssistive -> labelAssistive
        ColorAccessKeyToken.LabelDisable -> labelDisable
        ColorAccessKeyToken.BackgroundNormalNormal -> backgroundNormalNormal
        ColorAccessKeyToken.BackgroundNormalAlternative -> backgroundNormalAlternative
        ColorAccessKeyToken.BackgroundElevatedNormal -> backgroundElevatedNormal
        ColorAccessKeyToken.BackgroundElevatedAlternative -> backgroundElevatedAlternative
        ColorAccessKeyToken.BackgroundTransparentNormal -> backgroundTransparentNormal
        ColorAccessKeyToken.BackgroundTransparentAlternative -> backgroundTransparentAlternative
        ColorAccessKeyToken.InteractionInactive -> interactionInactive
        ColorAccessKeyToken.InteractionDisable -> interactionDisable
        ColorAccessKeyToken.LineNormalNormal -> lineNormalNormal
        ColorAccessKeyToken.LineNormalNeutral -> lineNormalNeutral
        ColorAccessKeyToken.LineNormalAlternative -> lineNormalAlternative
        ColorAccessKeyToken.LineSolidNormal -> lineSolidNormal
        ColorAccessKeyToken.LineSolidNeutral -> lineSolidNeutral
        ColorAccessKeyToken.LineSolidAlternative -> lineSolidAlternative
        ColorAccessKeyToken.StatusPositive -> statusPositive
        ColorAccessKeyToken.StatusCautionary -> statusCautionary
        ColorAccessKeyToken.StatusNegative -> statusNegative
        ColorAccessKeyToken.AccentBackgroundRedOrange -> accentBackgroundRedOrange
        ColorAccessKeyToken.AccentBackgroundLime -> accentBackgroundLime
        ColorAccessKeyToken.AccentBackgroundCyan -> accentBackgroundCyan
        ColorAccessKeyToken.AccentBackgroundLightBlue -> accentBackgroundLightBlue
        ColorAccessKeyToken.AccentBackgroundViolet -> accentBackgroundViolet
        ColorAccessKeyToken.AccentBackgroundPurple -> accentBackgroundPurple
        ColorAccessKeyToken.AccentBackgroundPink -> accentBackgroundPink
        ColorAccessKeyToken.AccentForegroundRed -> accentForegroundRed
        ColorAccessKeyToken.AccentForegroundRedOrange -> accentForegroundRedOrange
        ColorAccessKeyToken.AccentForegroundOrange -> accentForegroundOrange
        ColorAccessKeyToken.AccentForegroundLime -> accentForegroundLime
        ColorAccessKeyToken.AccentForegroundGreen -> accentForegroundGreen
        ColorAccessKeyToken.AccentForegroundCyan -> accentForegroundCyan
        ColorAccessKeyToken.AccentForegroundLightBlue -> accentForegroundLightBlue
        ColorAccessKeyToken.AccentForegroundBlue -> accentForegroundBlue
        ColorAccessKeyToken.AccentForegroundViolet -> accentForegroundViolet
        ColorAccessKeyToken.AccentForegroundPurple -> accentForegroundPurple
        ColorAccessKeyToken.AccentForegroundPink -> accentForegroundPink
        ColorAccessKeyToken.InversePrimary -> inversePrimary
        ColorAccessKeyToken.InverseBackground -> inverseBackground
        ColorAccessKeyToken.InverseLabel -> inverseLabel
        ColorAccessKeyToken.FillNormal -> fillNormal
        ColorAccessKeyToken.FillStrong -> fillStrong
        ColorAccessKeyToken.FillAlternative -> fillAlternative
        ColorAccessKeyToken.MaterialDimmer -> materialDimmer
    }

private val LightColorScheme = lightColorScheme()
private val DarkColorScheme = darkColorScheme()

@Composable
internal fun provideColorScheme(): ColorScheme =
    when {
        isSystemInDarkTheme() -> DarkColorScheme
        else -> LightColorScheme
    }

internal val LocalColorScheme = staticCompositionLocalOf { LightColorScheme }
