package team.mino.core.designsystem.component.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import team.mino.core.designsystem.component.category.token.CategoryTokens
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * [MinoCategory]의 기본값 모음.
 */
object MinoCategoryDefaults {
    /** [MinoCategory]의 기본 [MinoCategoryColors]. */
    @Composable
    @ReadOnlyComposable
    fun colors(): MinoCategoryColors = MinoAndroidTheme.colors.defaultCategoryColors

    /**
     * 기본값에서 일부만 바꾼 [MinoCategoryColors]를 만든다.
     * [Color.Unspecified]는 기본값 유지를 뜻한다.
     */
    @Composable
    @ReadOnlyComposable
    fun colors(
        chipActiveContainerColor: Color = Color.Unspecified,
        chipActiveContentColor: Color = Color.Unspecified,
        chipInactiveContainerColor: Color = Color.Unspecified,
        chipInactiveContentColor: Color = Color.Unspecified,
        chipInactiveBorderColor: Color = Color.Unspecified,
        pushBadgeColor: Color = Color.Unspecified,
    ): MinoCategoryColors =
        MinoAndroidTheme.colors.defaultCategoryColors.copy(
            chipActiveContainerColor = chipActiveContainerColor,
            chipActiveContentColor = chipActiveContentColor,
            chipInactiveContainerColor = chipInactiveContainerColor,
            chipInactiveContentColor = chipInactiveContentColor,
            chipInactiveBorderColor = chipInactiveBorderColor,
            pushBadgeColor = pushBadgeColor,
        )

    internal val ColorScheme.defaultCategoryColors: MinoCategoryColors
        get() =
            defaultCategoryColorsCached
                ?: MinoCategoryColors(
                    chipActiveContainerColor = fromToken(CategoryTokens.ChipActiveContainerColor),
                    chipActiveContentColor = fromToken(CategoryTokens.ChipActiveContentColor),
                    chipInactiveContainerColor = fromToken(CategoryTokens.ChipInactiveContainerColor),
                    chipInactiveContentColor = fromToken(CategoryTokens.ChipInactiveContentColor),
                    chipInactiveBorderColor = fromToken(CategoryTokens.ChipInactiveBorderColor),
                    pushBadgeColor = fromToken(CategoryTokens.PushBadgeColor),
                ).also { defaultCategoryColorsCached = it }
}

/**
 * [MinoCategory]의 상태별 색. 슬롯 값이 [Color.Unspecified]면 [copy]에서 원본을 유지한다.
 */
@Immutable
class MinoCategoryColors(
    val chipActiveContainerColor: Color,
    val chipActiveContentColor: Color,
    val chipInactiveContainerColor: Color,
    val chipInactiveContentColor: Color,
    val chipInactiveBorderColor: Color,
    val pushBadgeColor: Color,
) {
    fun copy(
        chipActiveContainerColor: Color = this.chipActiveContainerColor,
        chipActiveContentColor: Color = this.chipActiveContentColor,
        chipInactiveContainerColor: Color = this.chipInactiveContainerColor,
        chipInactiveContentColor: Color = this.chipInactiveContentColor,
        chipInactiveBorderColor: Color = this.chipInactiveBorderColor,
        pushBadgeColor: Color = this.pushBadgeColor,
    ): MinoCategoryColors =
        MinoCategoryColors(
            chipActiveContainerColor = chipActiveContainerColor.takeOrElse { this.chipActiveContainerColor },
            chipActiveContentColor = chipActiveContentColor.takeOrElse { this.chipActiveContentColor },
            chipInactiveContainerColor = chipInactiveContainerColor.takeOrElse { this.chipInactiveContainerColor },
            chipInactiveContentColor = chipInactiveContentColor.takeOrElse { this.chipInactiveContentColor },
            chipInactiveBorderColor = chipInactiveBorderColor.takeOrElse { this.chipInactiveBorderColor },
            pushBadgeColor = pushBadgeColor.takeOrElse { this.pushBadgeColor },
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinoCategoryColors) return false

        if (chipActiveContainerColor != other.chipActiveContainerColor) return false
        if (chipActiveContentColor != other.chipActiveContentColor) return false
        if (chipInactiveContainerColor != other.chipInactiveContainerColor) return false
        if (chipInactiveContentColor != other.chipInactiveContentColor) return false
        if (chipInactiveBorderColor != other.chipInactiveBorderColor) return false
        if (pushBadgeColor != other.pushBadgeColor) return false

        return true
    }

    override fun hashCode(): Int =
        arrayOf(
            chipActiveContainerColor,
            chipActiveContentColor,
            chipInactiveContainerColor,
            chipInactiveContentColor,
            chipInactiveBorderColor,
            pushBadgeColor,
        ).contentHashCode()
}
