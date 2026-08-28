package team.mino.core.designsystem.component.checkbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import team.mino.core.designsystem.component.checkbox.token.CheckboxTokens
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * [MinoCheckbox]의 기본값 모음.
 */
object MinoCheckboxDefaults {
    /** 체크박스 상자의 셰이프. */
    val shape: Shape = CheckboxTokens.BoxShape

    /** [MinoCheckbox]의 기본 [MinoCheckboxColors]. */
    @Composable
    @ReadOnlyComposable
    fun colors(): MinoCheckboxColors = MinoAndroidTheme.colors.defaultCheckboxColors

    /**
     * 기본값에서 일부만 바꾼 [MinoCheckboxColors]를 만든다.
     * [Color.Unspecified]는 기본값 유지를 뜻한다.
     */
    @Composable
    @ReadOnlyComposable
    fun colors(
        checkedContainerColor: Color = Color.Unspecified,
        checkedCheckmarkColor: Color = Color.Unspecified,
        uncheckedContainerColor: Color = Color.Unspecified,
        uncheckedBorderColor: Color = Color.Unspecified,
    ): MinoCheckboxColors =
        MinoAndroidTheme.colors.defaultCheckboxColors.copy(
            checkedContainerColor = checkedContainerColor,
            checkedCheckmarkColor = checkedCheckmarkColor,
            uncheckedContainerColor = uncheckedContainerColor,
            uncheckedBorderColor = uncheckedBorderColor,
        )

    internal val ColorScheme.defaultCheckboxColors: MinoCheckboxColors
        get() =
            defaultCheckboxColorsCached
                ?: MinoCheckboxColors(
                    checkedContainerColor = fromToken(CheckboxTokens.CheckedContainerColor),
                    checkedCheckmarkColor = fromToken(CheckboxTokens.CheckedCheckmarkColor),
                    uncheckedContainerColor = CheckboxTokens.UncheckedContainerColor,
                    uncheckedBorderColor = fromToken(CheckboxTokens.UncheckedBorderColor),
                ).also { defaultCheckboxColorsCached = it }
}

/**
 * [MinoCheckbox]의 상태별 색. 슬롯 값이 [Color.Unspecified]면 [copy]에서 원본을 유지한다.
 *
 * `enabled` 축의 슬롯은 두지 않는다. Figma 컴포넌트셋이 비활성 상태의 색을 따로 정의하지 않아,
 * [MinoCheckbox]의 `enabled`는 색이 아니라 입력만 막는다.
 */
@Immutable
class MinoCheckboxColors(
    val checkedContainerColor: Color,
    val checkedCheckmarkColor: Color,
    val uncheckedContainerColor: Color,
    val uncheckedBorderColor: Color,
) {
    @Stable
    internal fun containerColor(checked: Boolean): Color =
        if (checked) checkedContainerColor else uncheckedContainerColor

    /** 체크 상태에서는 컨테이너 색으로 채우므로 테두리를 그리지 않는다. */
    @Stable
    internal fun borderColor(checked: Boolean): Color? = if (checked) null else uncheckedBorderColor

    @Stable
    internal fun checkmarkColor(checked: Boolean): Color = if (checked) checkedCheckmarkColor else Color.Transparent

    fun copy(
        checkedContainerColor: Color = this.checkedContainerColor,
        checkedCheckmarkColor: Color = this.checkedCheckmarkColor,
        uncheckedContainerColor: Color = this.uncheckedContainerColor,
        uncheckedBorderColor: Color = this.uncheckedBorderColor,
    ): MinoCheckboxColors =
        MinoCheckboxColors(
            checkedContainerColor = checkedContainerColor.takeOrElse { this.checkedContainerColor },
            checkedCheckmarkColor = checkedCheckmarkColor.takeOrElse { this.checkedCheckmarkColor },
            uncheckedContainerColor = uncheckedContainerColor.takeOrElse { this.uncheckedContainerColor },
            uncheckedBorderColor = uncheckedBorderColor.takeOrElse { this.uncheckedBorderColor },
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinoCheckboxColors) return false

        if (checkedContainerColor != other.checkedContainerColor) return false
        if (checkedCheckmarkColor != other.checkedCheckmarkColor) return false
        if (uncheckedContainerColor != other.uncheckedContainerColor) return false
        if (uncheckedBorderColor != other.uncheckedBorderColor) return false

        return true
    }

    override fun hashCode(): Int =
        arrayOf(
            checkedContainerColor,
            checkedCheckmarkColor,
            uncheckedContainerColor,
            uncheckedBorderColor,
        ).contentHashCode()
}
