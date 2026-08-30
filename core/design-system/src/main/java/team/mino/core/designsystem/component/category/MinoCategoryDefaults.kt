package team.mino.core.designsystem.component.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
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
    /** [variant]에 대응하는 [MinoCategoryColors]. Figma `Variant` 속성(Normal·Alternative)에 대응. */
    @Composable
    @ReadOnlyComposable
    fun colors(variant: CategoryVariant = CategoryVariant.Normal): MinoCategoryColors =
        when (variant) {
            CategoryVariant.Normal -> MinoAndroidTheme.colors.normalCategoryColors
            CategoryVariant.Alternative -> MinoAndroidTheme.colors.alternativeCategoryColors
        }

    /**
     * [variant] 기본값에서 일부만 바꾼 [MinoCategoryColors]를 만든다.
     * [Color.Unspecified]는 기본값 유지를 뜻한다.
     */
    @Composable
    @ReadOnlyComposable
    fun colors(
        variant: CategoryVariant = CategoryVariant.Normal,
        activeContainerColor: Color = Color.Unspecified,
        activeContentColor: Color = Color.Unspecified,
        activeBorderColor: Color = Color.Unspecified,
        inactiveContainerColor: Color = Color.Unspecified,
        inactiveContentColor: Color = Color.Unspecified,
        inactiveBorderColor: Color = Color.Unspecified,
    ): MinoCategoryColors =
        colors(variant).copy(
            activeContainerColor = activeContainerColor,
            activeContentColor = activeContentColor,
            activeBorderColor = activeBorderColor,
            inactiveContainerColor = inactiveContainerColor,
            inactiveContentColor = inactiveContentColor,
            inactiveBorderColor = inactiveBorderColor,
        )

    /** 선택 항목이 채워지고, 비선택 항목은 배경색 + 테두리로 그려진다. */
    internal val ColorScheme.normalCategoryColors: MinoCategoryColors
        get() =
            normalCategoryColorsCached
                ?: MinoCategoryColors(
                    activeContainerColor = fromToken(CategoryTokens.NormalActiveContainerColor),
                    activeContentColor = fromToken(CategoryTokens.NormalActiveContentColor),
                    activeBorderColor = Color.Transparent,
                    inactiveContainerColor = fromToken(CategoryTokens.NormalInactiveContainerColor),
                    inactiveContentColor = fromToken(CategoryTokens.InactiveContentColor),
                    inactiveBorderColor = fromToken(CategoryTokens.InactiveBorderColor),
                ).also { normalCategoryColorsCached = it }

    /** 선택 항목이 Primary 틴트 + 테두리로, 비선택 항목은 테두리만으로 그려진다. */
    internal val ColorScheme.alternativeCategoryColors: MinoCategoryColors
        get() =
            alternativeCategoryColorsCached
                ?: run {
                    val accent = fromToken(CategoryTokens.AlternativeActiveColor)
                    MinoCategoryColors(
                        activeContainerColor = accent.copy(alpha = CategoryTokens.AlternativeActiveTintOpacity),
                        activeContentColor = accent,
                        activeBorderColor = accent.copy(alpha = CategoryTokens.AlternativeActiveBorderOpacity),
                        inactiveContainerColor = Color.Transparent,
                        inactiveContentColor = fromToken(CategoryTokens.InactiveContentColor),
                        inactiveBorderColor = fromToken(CategoryTokens.InactiveBorderColor),
                    )
                }.also { alternativeCategoryColorsCached = it }
}

/**
 * [MinoCategory] 항목의 선택 상태별 색. 슬롯 값이 [Color.Unspecified]면 [copy]에서 원본을 유지한다.
 * 테두리 슬롯이 [Color.Transparent]면 테두리를 그리지 않는다.
 */
@Immutable
class MinoCategoryColors(
    val activeContainerColor: Color,
    val activeContentColor: Color,
    val activeBorderColor: Color,
    val inactiveContainerColor: Color,
    val inactiveContentColor: Color,
    val inactiveBorderColor: Color,
) {
    fun copy(
        activeContainerColor: Color = this.activeContainerColor,
        activeContentColor: Color = this.activeContentColor,
        activeBorderColor: Color = this.activeBorderColor,
        inactiveContainerColor: Color = this.inactiveContainerColor,
        inactiveContentColor: Color = this.inactiveContentColor,
        inactiveBorderColor: Color = this.inactiveBorderColor,
    ): MinoCategoryColors =
        MinoCategoryColors(
            activeContainerColor = activeContainerColor.takeOrElse { this.activeContainerColor },
            activeContentColor = activeContentColor.takeOrElse { this.activeContentColor },
            activeBorderColor = activeBorderColor.takeOrElse { this.activeBorderColor },
            inactiveContainerColor = inactiveContainerColor.takeOrElse { this.inactiveContainerColor },
            inactiveContentColor = inactiveContentColor.takeOrElse { this.inactiveContentColor },
            inactiveBorderColor = inactiveBorderColor.takeOrElse { this.inactiveBorderColor },
        )

    @Stable
    internal fun containerColor(active: Boolean): Color = if (active) activeContainerColor else inactiveContainerColor

    @Stable
    internal fun contentColor(active: Boolean): Color = if (active) activeContentColor else inactiveContentColor

    /** 테두리를 그리지 않는 조합은 `null`을 준다. */
    @Stable
    internal fun borderColor(active: Boolean): Color? =
        (if (active) activeBorderColor else inactiveBorderColor).takeIf { it != Color.Transparent }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinoCategoryColors) return false

        if (activeContainerColor != other.activeContainerColor) return false
        if (activeContentColor != other.activeContentColor) return false
        if (activeBorderColor != other.activeBorderColor) return false
        if (inactiveContainerColor != other.inactiveContainerColor) return false
        if (inactiveContentColor != other.inactiveContentColor) return false
        if (inactiveBorderColor != other.inactiveBorderColor) return false

        return true
    }

    override fun hashCode(): Int =
        arrayOf(
            activeContainerColor,
            activeContentColor,
            activeBorderColor,
            inactiveContainerColor,
            inactiveContentColor,
            inactiveBorderColor,
        ).contentHashCode()
}
