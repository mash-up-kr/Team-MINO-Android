package team.mino.core.designsystem.component.chip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import team.mino.core.designsystem.component.chip.token.ChipTokens
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * [MinoChip]의 기본값 모음.
 */
object MinoChipDefaults {
    /** [variant]·[active]·[enabled] 조합에 대응하는 배경색. */
    @Stable
    internal fun containerColor(
        colors: MinoChipColors,
        variant: ChipVariant,
        active: Boolean,
        enabled: Boolean,
    ): Color =
        when {
            !enabled && variant == ChipVariant.Solid -> colors.disabledContainerColor
            !enabled -> Color.Transparent
            variant == ChipVariant.Solid && active -> colors.solidActiveContainerColor
            variant == ChipVariant.Solid -> colors.solidInactiveContainerColor
            active -> colors.outlinedActiveTintColor
            else -> Color.Transparent
        }

    /** [variant]·[active]·[enabled] 조합에 대응하는 글자색. */
    @Stable
    internal fun contentColor(
        colors: MinoChipColors,
        variant: ChipVariant,
        active: Boolean,
        enabled: Boolean,
    ): Color =
        when {
            !enabled -> colors.disabledContentColor
            variant == ChipVariant.Solid && active -> colors.solidActiveContentColor
            variant == ChipVariant.Outlined && active -> colors.outlinedActiveContentColor
            else -> colors.inactiveContentColor
        }

    /** [variant]·[active]·[enabled] 조합에 대응하는 테두리색. Solid는 테두리가 없다. */
    @Stable
    internal fun borderColor(
        colors: MinoChipColors,
        variant: ChipVariant,
        active: Boolean,
        enabled: Boolean,
    ): Color? =
        when {
            variant == ChipVariant.Solid -> null
            !enabled -> colors.outlinedBorderColor
            active -> colors.outlinedActiveBorderColor
            else -> colors.outlinedBorderColor
        }

    /** [MinoChip]의 기본 [MinoChipColors]. */
    @Composable
    @ReadOnlyComposable
    fun colors(): MinoChipColors = MinoAndroidTheme.colors.defaultChipColors

    /**
     * 기본값에서 일부만 바꾼 [MinoChipColors]를 만든다.
     * [Color.Unspecified]는 기본값 유지를 뜻한다.
     */
    @Composable
    @ReadOnlyComposable
    fun colors(
        inactiveContentColor: Color = Color.Unspecified,
        solidActiveContainerColor: Color = Color.Unspecified,
        solidActiveContentColor: Color = Color.Unspecified,
        solidInactiveContainerColor: Color = Color.Unspecified,
        outlinedBorderColor: Color = Color.Unspecified,
        outlinedActiveContentColor: Color = Color.Unspecified,
        outlinedActiveTintColor: Color = Color.Unspecified,
        outlinedActiveBorderColor: Color = Color.Unspecified,
        disabledContentColor: Color = Color.Unspecified,
        disabledContainerColor: Color = Color.Unspecified,
    ): MinoChipColors =
        MinoAndroidTheme.colors.defaultChipColors.copy(
            inactiveContentColor = inactiveContentColor,
            solidActiveContainerColor = solidActiveContainerColor,
            solidActiveContentColor = solidActiveContentColor,
            solidInactiveContainerColor = solidInactiveContainerColor,
            outlinedBorderColor = outlinedBorderColor,
            outlinedActiveContentColor = outlinedActiveContentColor,
            outlinedActiveTintColor = outlinedActiveTintColor,
            outlinedActiveBorderColor = outlinedActiveBorderColor,
            disabledContentColor = disabledContentColor,
            disabledContainerColor = disabledContainerColor,
        )

    internal val ColorScheme.defaultChipColors: MinoChipColors
        get() =
            defaultChipColorsCached
                ?: MinoChipColors(
                    inactiveContentColor = fromToken(ChipTokens.InactiveContentColor),
                    solidActiveContainerColor = fromToken(ChipTokens.SolidActiveContainerColor),
                    solidActiveContentColor = fromToken(ChipTokens.SolidActiveContentColor),
                    solidInactiveContainerColor = fromToken(ChipTokens.SolidInactiveContainerColor),
                    outlinedBorderColor = fromToken(ChipTokens.OutlinedBorderColor),
                    outlinedActiveContentColor = fromToken(ChipTokens.OutlinedActiveContentColor),
                    outlinedActiveTintColor =
                        fromToken(ChipTokens.SolidActiveContainerColor)
                            .copy(alpha = ChipTokens.ActiveTintOpacity),
                    outlinedActiveBorderColor =
                        fromToken(ChipTokens.SolidActiveContainerColor)
                            .copy(alpha = ChipTokens.ActiveBorderOpacity),
                    disabledContentColor = fromToken(ChipTokens.DisabledContentColor),
                    disabledContainerColor = fromToken(ChipTokens.DisabledContainerColor),
                ).also { defaultChipColorsCached = it }
}

/**
 * [MinoChip]의 상태별 색. 슬롯 값이 [Color.Unspecified]면 [copy]에서 원본을 유지한다.
 */
@Immutable
class MinoChipColors(
    val inactiveContentColor: Color,
    val solidActiveContainerColor: Color,
    val solidActiveContentColor: Color,
    val solidInactiveContainerColor: Color,
    val outlinedBorderColor: Color,
    val outlinedActiveContentColor: Color,
    val outlinedActiveTintColor: Color,
    val outlinedActiveBorderColor: Color,
    val disabledContentColor: Color,
    val disabledContainerColor: Color,
) {
    fun copy(
        inactiveContentColor: Color = this.inactiveContentColor,
        solidActiveContainerColor: Color = this.solidActiveContainerColor,
        solidActiveContentColor: Color = this.solidActiveContentColor,
        solidInactiveContainerColor: Color = this.solidInactiveContainerColor,
        outlinedBorderColor: Color = this.outlinedBorderColor,
        outlinedActiveContentColor: Color = this.outlinedActiveContentColor,
        outlinedActiveTintColor: Color = this.outlinedActiveTintColor,
        outlinedActiveBorderColor: Color = this.outlinedActiveBorderColor,
        disabledContentColor: Color = this.disabledContentColor,
        disabledContainerColor: Color = this.disabledContainerColor,
    ): MinoChipColors =
        MinoChipColors(
            inactiveContentColor = inactiveContentColor.takeOrElse { this.inactiveContentColor },
            solidActiveContainerColor = solidActiveContainerColor.takeOrElse { this.solidActiveContainerColor },
            solidActiveContentColor = solidActiveContentColor.takeOrElse { this.solidActiveContentColor },
            solidInactiveContainerColor = solidInactiveContainerColor.takeOrElse { this.solidInactiveContainerColor },
            outlinedBorderColor = outlinedBorderColor.takeOrElse { this.outlinedBorderColor },
            outlinedActiveContentColor = outlinedActiveContentColor.takeOrElse { this.outlinedActiveContentColor },
            outlinedActiveTintColor = outlinedActiveTintColor.takeOrElse { this.outlinedActiveTintColor },
            outlinedActiveBorderColor = outlinedActiveBorderColor.takeOrElse { this.outlinedActiveBorderColor },
            disabledContentColor = disabledContentColor.takeOrElse { this.disabledContentColor },
            disabledContainerColor = disabledContainerColor.takeOrElse { this.disabledContainerColor },
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinoChipColors) return false

        if (inactiveContentColor != other.inactiveContentColor) return false
        if (solidActiveContainerColor != other.solidActiveContainerColor) return false
        if (solidActiveContentColor != other.solidActiveContentColor) return false
        if (solidInactiveContainerColor != other.solidInactiveContainerColor) return false
        if (outlinedBorderColor != other.outlinedBorderColor) return false
        if (outlinedActiveContentColor != other.outlinedActiveContentColor) return false
        if (outlinedActiveTintColor != other.outlinedActiveTintColor) return false
        if (outlinedActiveBorderColor != other.outlinedActiveBorderColor) return false
        if (disabledContentColor != other.disabledContentColor) return false
        if (disabledContainerColor != other.disabledContainerColor) return false

        return true
    }

    override fun hashCode(): Int =
        arrayOf(
            inactiveContentColor,
            solidActiveContainerColor,
            solidActiveContentColor,
            solidInactiveContainerColor,
            outlinedBorderColor,
            outlinedActiveContentColor,
            outlinedActiveTintColor,
            outlinedActiveBorderColor,
            disabledContentColor,
            disabledContainerColor,
        ).contentHashCode()
}
