package team.mino.core.designsystem.component.contentbadge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import team.mino.core.designsystem.component.contentbadge.token.ContentBadgeTokens
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * [MinoContentBadge]의 기본값 모음.
 */
object MinoContentBadgeDefaults {
    /** [MinoContentBadge]의 기본(Neutral) [MinoContentBadgeColors]. */
    @Composable
    @ReadOnlyComposable
    fun colors(): MinoContentBadgeColors = MinoAndroidTheme.colors.defaultContentBadgeColors

    /**
     * 기본값에서 일부만 바꾼 [MinoContentBadgeColors]를 만든다.
     * [Color.Unspecified]는 기본값 유지를 뜻한다.
     */
    @Composable
    @ReadOnlyComposable
    fun colors(
        containerColor: Color = Color.Unspecified,
        contentColor: Color = Color.Unspecified,
        borderColor: Color = Color.Unspecified,
    ): MinoContentBadgeColors =
        MinoAndroidTheme.colors.defaultContentBadgeColors.copy(
            containerColor = containerColor,
            contentColor = contentColor,
            borderColor = borderColor,
        )

    /** [MinoContentBadge]의 `color`가 [ContentBadgeColor.Accent]일 때 쓰는 기본 강조색(Figma 예시: Cyan). */
    val defaultAccentColor: Color
        @Composable @ReadOnlyComposable get() = ContentBadgeTokens.DefaultAccentColor.value

    /** [color]에 대응하는 [MinoContentBadgeColors]. Neutral은 기본값을, Accent는 [accentColors] 파생값을 쓴다. */
    @Composable
    @ReadOnlyComposable
    fun colors(color: ContentBadgeColor): MinoContentBadgeColors =
        when (color) {
            is ContentBadgeColor.Neutral -> colors()
            is ContentBadgeColor.Accent -> accentColors(color.color)
        }

    /**
     * [baseColor] 하나에서 배경(Solid 8%)·테두리(Outlined 43%)를 파생한 [MinoContentBadgeColors]를 만든다.
     * Figma의 Accent 색(Color=Accent) variant에 대응.
     */
    fun accentColors(baseColor: Color): MinoContentBadgeColors =
        MinoContentBadgeColors(
            containerColor = baseColor.copy(alpha = ContentBadgeTokens.AccentTintOpacity),
            contentColor = baseColor,
            borderColor = baseColor.copy(alpha = ContentBadgeTokens.AccentBorderOpacity),
        )

    internal val ColorScheme.defaultContentBadgeColors: MinoContentBadgeColors
        get() =
            defaultContentBadgeColorsCached
                ?: MinoContentBadgeColors(
                    containerColor = fromToken(ContentBadgeTokens.NeutralContainerColor),
                    contentColor = fromToken(ContentBadgeTokens.NeutralContentColor),
                    borderColor = fromToken(ContentBadgeTokens.NeutralBorderColor),
                ).also { defaultContentBadgeColorsCached = it }
}

/**
 * [MinoContentBadge]의 색. `containerColor`는 [ContentBadgeVariant.Solid]에서, `borderColor`는
 * [ContentBadgeVariant.Outlined]에서만 쓰인다. 슬롯 값이 [Color.Unspecified]면 [copy]에서 원본을 유지한다.
 */
@Immutable
class MinoContentBadgeColors(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
) {
    fun copy(
        containerColor: Color = this.containerColor,
        contentColor: Color = this.contentColor,
        borderColor: Color = this.borderColor,
    ): MinoContentBadgeColors =
        MinoContentBadgeColors(
            containerColor = containerColor.takeOrElse { this.containerColor },
            contentColor = contentColor.takeOrElse { this.contentColor },
            borderColor = borderColor.takeOrElse { this.borderColor },
        )

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
