package team.mino.feature.sample.main.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.feature.sample.main.component.token.RoomCardTokens

/**
 * [MinoRoomCard]·[MinoRoomCheckBoxCard]의 기본값 모음. 크기·간격은 [RoomCardTokens] 참고.
 *
 * 카드 본문은 상태가 없어 단일 값 프로퍼티로 두고, `checked` 상태를 갖는 체크박스만
 * [MinoRoomCheckBoxCardColors]로 묶는다.
 */
object MinoRoomCardDefaults {
    val coverShape: Shape
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.shapes.large

    /** 커버 이미지가 없을 때 채우는 배경색. */
    val coverBackgroundColor: Color
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.colors.fillNormal

    val coverPlaceholderTint: Color
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.colors.labelAssistive

    val titleFont: TextStyle
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.typography.body1NormalBold

    val titleColor: Color
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.colors.labelNormal

    val memoFont: TextStyle
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.typography.label2Medium

    val memoColor: Color
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.colors.labelAlternative

    val placeCountFont: TextStyle
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.typography.label2Medium

    val placeCountColor: Color
        @Composable @ReadOnlyComposable get() = MinoAndroidTheme.colors.labelAlternative

    val checkBoxShape: Shape get() = RoomCardTokens.CheckBoxShape

    /** [MinoRoomCheckBoxCard]의 기본 [MinoRoomCheckBoxCardColors]. */
    @Composable
    fun checkBoxColors(): MinoRoomCheckBoxCardColors {
        val checkedContainerColor = MinoAndroidTheme.colors.primaryNormal
        val checkedCheckmarkColor = MinoAndroidTheme.colors.inverseLabel
        val uncheckedContainerColor = MinoAndroidTheme.colors.backgroundNormalNormal
        val uncheckedBorderColor = MinoAndroidTheme.colors.lineNormalNormal

        // design-system 밖이라 ColorScheme에 캐시 필드를 둘 수 없어, 테마 색이 바뀔 때만 재생성한다.
        return remember(
            checkedContainerColor,
            checkedCheckmarkColor,
            uncheckedContainerColor,
            uncheckedBorderColor,
        ) {
            MinoRoomCheckBoxCardColors(
                checkedContainerColor = checkedContainerColor,
                checkedCheckmarkColor = checkedCheckmarkColor,
                uncheckedContainerColor = uncheckedContainerColor,
                uncheckedBorderColor = uncheckedBorderColor,
            )
        }
    }

    /**
     * 기본값에서 일부만 바꾼 [MinoRoomCheckBoxCardColors]를 만든다.
     * [Color.Unspecified]는 기본값 유지를 뜻한다.
     */
    @Composable
    fun checkBoxColors(
        checkedContainerColor: Color = Color.Unspecified,
        checkedCheckmarkColor: Color = Color.Unspecified,
        uncheckedContainerColor: Color = Color.Unspecified,
        uncheckedBorderColor: Color = Color.Unspecified,
    ): MinoRoomCheckBoxCardColors =
        checkBoxColors().copy(
            checkedContainerColor = checkedContainerColor,
            checkedCheckmarkColor = checkedCheckmarkColor,
            uncheckedContainerColor = uncheckedContainerColor,
            uncheckedBorderColor = uncheckedBorderColor,
        )
}

/**
 * [MinoRoomCheckBoxCard] 체크박스의 상태별 색. 슬롯 값이 [Color.Unspecified]면 [copy]에서 원본을 유지한다.
 */
@Immutable
class MinoRoomCheckBoxCardColors(
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
    ): MinoRoomCheckBoxCardColors =
        MinoRoomCheckBoxCardColors(
            checkedContainerColor = checkedContainerColor.takeOrElse { this.checkedContainerColor },
            checkedCheckmarkColor = checkedCheckmarkColor.takeOrElse { this.checkedCheckmarkColor },
            uncheckedContainerColor = uncheckedContainerColor.takeOrElse { this.uncheckedContainerColor },
            uncheckedBorderColor = uncheckedBorderColor.takeOrElse { this.uncheckedBorderColor },
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinoRoomCheckBoxCardColors) return false

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
