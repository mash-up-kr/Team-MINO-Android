package team.mino.core.designsystem.component.switch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.component.switch.token.SwitchTokens
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * [MinoSwitch]의 기본값 모음.
 */
object MinoSwitchDefaults {
    /** [checked]·[enabled] 조합에 대응하는 트랙 색. */
    @Stable
    internal fun trackColor(
        colors: MinoSwitchColors,
        checked: Boolean,
        enabled: Boolean,
    ): Color =
        when {
            !enabled -> colors.disabledTrackColor
            checked -> colors.checkedTrackColor
            else -> colors.uncheckedTrackColor
        }

    /** [checked]·[enabled] 조합에 대응하는 섬(thumb) 색. */
    @Stable
    internal fun thumbColor(
        colors: MinoSwitchColors,
        checked: Boolean,
        enabled: Boolean,
    ): Color =
        when {
            !enabled -> colors.disabledThumbColor
            checked -> colors.checkedThumbColor
            else -> colors.uncheckedThumbColor
        }

    /** [MinoSwitch]의 기본 [MinoSwitchColors]. */
    @Composable
    @ReadOnlyComposable
    fun colors(): MinoSwitchColors = MinoAndroidTheme.colors.defaultSwitchColors

    /**
     * 기본값에서 일부만 바꾼 [MinoSwitchColors]를 만든다.
     * [Color.Unspecified]는 기본값 유지를 뜻한다.
     */
    @Composable
    @ReadOnlyComposable
    fun colors(
        checkedTrackColor: Color = Color.Unspecified,
        checkedThumbColor: Color = Color.Unspecified,
        uncheckedTrackColor: Color = Color.Unspecified,
        uncheckedThumbColor: Color = Color.Unspecified,
        disabledTrackColor: Color = Color.Unspecified,
        disabledThumbColor: Color = Color.Unspecified,
    ): MinoSwitchColors =
        MinoAndroidTheme.colors.defaultSwitchColors.copy(
            checkedTrackColor = checkedTrackColor,
            checkedThumbColor = checkedThumbColor,
            uncheckedTrackColor = uncheckedTrackColor,
            uncheckedThumbColor = uncheckedThumbColor,
            disabledTrackColor = disabledTrackColor,
            disabledThumbColor = disabledThumbColor,
        )

    internal val ColorScheme.defaultSwitchColors: MinoSwitchColors
        get() =
            defaultSwitchColorsCached
                ?: MinoSwitchColors(
                    checkedTrackColor = fromToken(SwitchTokens.CheckedTrackColor),
                    checkedThumbColor = fromToken(SwitchTokens.CheckedThumbColor),
                    uncheckedTrackColor = fromToken(SwitchTokens.UncheckedTrackColor),
                    uncheckedThumbColor = fromToken(SwitchTokens.UncheckedThumbColor),
                    disabledTrackColor = fromToken(SwitchTokens.DisabledTrackColor),
                    disabledThumbColor = fromToken(SwitchTokens.DisabledThumbColor),
                ).also { defaultSwitchColorsCached = it }
}
