package team.mino.core.designsystem.component.contentbadge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.component.contentbadge.token.ContentBadgeTokens
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * [MinoContentBadge]의 기본값 모음.
 */
object MinoContentBadgeDefaults {
    /** [color]에 대응하는 [MinoContentBadgeColors]. Figma `Color` 속성에 대응. */
    @Composable
    @ReadOnlyComposable
    fun colors(color: ContentBadgeColor): MinoContentBadgeColors =
        when (color) {
            ContentBadgeColor.Neutral -> MinoAndroidTheme.colors.defaultContentBadgeColors
            ContentBadgeColor.Accent -> MinoAndroidTheme.colors.accentContentBadgeColors
            ContentBadgeColor.LightBlue -> MinoAndroidTheme.colors.lightBlueContentBadgeColors
        }

    internal val ColorScheme.defaultContentBadgeColors: MinoContentBadgeColors
        get() =
            defaultContentBadgeColorsCached
                ?: MinoContentBadgeColors(
                    containerColor = fromToken(ContentBadgeTokens.NeutralContainerColor),
                    contentColor = fromToken(ContentBadgeTokens.NeutralContentColor),
                    borderColor = fromToken(ContentBadgeTokens.NeutralBorderColor),
                ).also { defaultContentBadgeColorsCached = it }

    internal val ColorScheme.accentContentBadgeColors: MinoContentBadgeColors
        get() =
            accentContentBadgeColorsCached
                ?: accentColors(ContentBadgeTokens.AccentColor)
                    .also { accentContentBadgeColorsCached = it }

    internal val ColorScheme.lightBlueContentBadgeColors: MinoContentBadgeColors
        get() =
            lightBlueContentBadgeColorsCached
                ?: accentColors(ContentBadgeTokens.LightBlueColor)
                    .also { lightBlueContentBadgeColorsCached = it }

    /** Accent 계열은 한 색에서 배경·테두리를 알파로 파생한다. */
    private fun ColorScheme.accentColors(accentToken: ColorAccessKeyToken): MinoContentBadgeColors {
        val accent = fromToken(accentToken)
        return MinoContentBadgeColors(
            containerColor = accent.copy(alpha = ContentBadgeTokens.AccentTintOpacity),
            contentColor = accent,
            borderColor = accent.copy(alpha = ContentBadgeTokens.AccentBorderOpacity),
        )
    }
}

/**
 * [MinoContentBadge]의 색. `containerColor`는 [ContentBadgeVariant.Solid]에서, `borderColor`는
 * [ContentBadgeVariant.Outlined]에서만 쓰인다.
 */
@Immutable
class MinoContentBadgeColors(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinoContentBadgeColors) return false

        if (containerColor != other.containerColor) return false
        if (contentColor != other.contentColor) return false
        if (borderColor != other.borderColor) return false

        return true
    }

    override fun hashCode(): Int =
        arrayOf(
            containerColor,
            contentColor,
            borderColor,
        ).contentHashCode()
}
