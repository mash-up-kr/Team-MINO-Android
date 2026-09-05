package team.mino.core.designsystem.component.switch

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse

/**
 * [MinoSwitch]의 상태별 색. 슬롯 값이 [Color.Unspecified]면 [copy]에서 원본을 유지한다.
 */
@Immutable
class MinoSwitchColors(
    val checkedTrackColor: Color,
    val checkedThumbColor: Color,
    val uncheckedTrackColor: Color,
    val uncheckedThumbColor: Color,
    val disabledTrackColor: Color,
    val disabledThumbColor: Color,
) {
    fun copy(
        checkedTrackColor: Color = this.checkedTrackColor,
        checkedThumbColor: Color = this.checkedThumbColor,
        uncheckedTrackColor: Color = this.uncheckedTrackColor,
        uncheckedThumbColor: Color = this.uncheckedThumbColor,
        disabledTrackColor: Color = this.disabledTrackColor,
        disabledThumbColor: Color = this.disabledThumbColor,
    ): MinoSwitchColors =
        MinoSwitchColors(
            checkedTrackColor = checkedTrackColor.takeOrElse { this.checkedTrackColor },
            checkedThumbColor = checkedThumbColor.takeOrElse { this.checkedThumbColor },
            uncheckedTrackColor = uncheckedTrackColor.takeOrElse { this.uncheckedTrackColor },
            uncheckedThumbColor = uncheckedThumbColor.takeOrElse { this.uncheckedThumbColor },
            disabledTrackColor = disabledTrackColor.takeOrElse { this.disabledTrackColor },
            disabledThumbColor = disabledThumbColor.takeOrElse { this.disabledThumbColor },
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinoSwitchColors) return false

        if (checkedTrackColor != other.checkedTrackColor) return false
        if (checkedThumbColor != other.checkedThumbColor) return false
        if (uncheckedTrackColor != other.uncheckedTrackColor) return false
        if (uncheckedThumbColor != other.uncheckedThumbColor) return false
        if (disabledTrackColor != other.disabledTrackColor) return false
        if (disabledThumbColor != other.disabledThumbColor) return false

        return true
    }

    override fun hashCode(): Int =
        arrayOf(
            checkedTrackColor,
            checkedThumbColor,
            uncheckedTrackColor,
            uncheckedThumbColor,
            disabledTrackColor,
            disabledThumbColor,
        ).contentHashCode()
}
