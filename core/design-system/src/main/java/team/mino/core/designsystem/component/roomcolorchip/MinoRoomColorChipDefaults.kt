package team.mino.core.designsystem.component.roomcolorchip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import team.mino.core.designsystem.component.roomcolorchip.token.RoomColorChipTokens
import team.mino.core.designsystem.component.roomcolorchip.token.borderColor
import team.mino.core.designsystem.component.roomcolorchip.token.containerColor
import team.mino.core.designsystem.component.roomcolorchip.token.selectedContainerColor
import team.mino.core.designsystem.foundation.color.token.value

/**
 * [MinoRoomColorChip]의 기본값 모음.
 */
object MinoRoomColorChipDefaults {
    /** [color]에 대응하는 [MinoRoomColorChipColors]. */
    @Composable
    @ReadOnlyComposable
    fun colors(color: MinoRoomColor): MinoRoomColorChipColors =
        MinoRoomColorChipColors(
            containerColor = color.containerColor,
            borderColor = color.borderColor,
            selectedContainerColor = color.selectedContainerColor,
            selectedContentColor = RoomColorChipTokens.SelectedContentColor.value,
        )

    /** [selected]에 대응하는 채움색. */
    @Stable
    internal fun containerColor(
        colors: MinoRoomColorChipColors,
        selected: Boolean,
    ): Color = if (selected) colors.selectedContainerColor else colors.containerColor

    /** [selected]에 대응하는 테두리색. 선택 상태에는 테두리가 없다. */
    @Stable
    internal fun borderColor(
        colors: MinoRoomColorChipColors,
        selected: Boolean,
    ): Color? = if (selected) null else colors.borderColor
}

/**
 * [MinoRoomColorChip]의 상태별 색. 슬롯 값이 [Color.Unspecified]면 [copy]에서 원본을 유지한다.
 *
 * @param selectedContentColor 선택 상태에 얹히는 체크 아이콘의 색.
 */
@Immutable
class MinoRoomColorChipColors(
    val containerColor: Color,
    val borderColor: Color,
    val selectedContainerColor: Color,
    val selectedContentColor: Color,
) {
    fun copy(
        containerColor: Color = this.containerColor,
        borderColor: Color = this.borderColor,
        selectedContainerColor: Color = this.selectedContainerColor,
        selectedContentColor: Color = this.selectedContentColor,
    ): MinoRoomColorChipColors =
        MinoRoomColorChipColors(
            containerColor = containerColor.takeOrElse { this.containerColor },
            borderColor = borderColor.takeOrElse { this.borderColor },
            selectedContainerColor = selectedContainerColor.takeOrElse { this.selectedContainerColor },
            selectedContentColor = selectedContentColor.takeOrElse { this.selectedContentColor },
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinoRoomColorChipColors) return false

        if (containerColor != other.containerColor) return false
        if (borderColor != other.borderColor) return false
        if (selectedContainerColor != other.selectedContainerColor) return false
        if (selectedContentColor != other.selectedContentColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + borderColor.hashCode()
        result = 31 * result + selectedContainerColor.hashCode()
        result = 31 * result + selectedContentColor.hashCode()
        return result
    }
}
