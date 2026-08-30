package team.mino.core.designsystem.component.pagination

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.Dp
import team.mino.core.designsystem.component.pagination.token.PaginationDotsTokens
import team.mino.core.designsystem.foundation.color.ColorScheme
import team.mino.core.designsystem.foundation.color.fromToken
import team.mino.core.designsystem.theme.MinoAndroidTheme

/**
 * [MinoPaginationDots]의 기본값 모음.
 */
object MinoPaginationDotsDefaults {
    /** 점 하나의 한 변. 선택 여부와 무관하게 같다. */
    val dotSize: Dp = PaginationDotsTokens.DotSize

    /** 점과 점 사이 간격. */
    val dotSpacing: Dp = PaginationDotsTokens.DotSpacing

    /** [MinoPaginationDots]의 기본 [MinoPaginationDotsColors]. */
    @Composable
    @ReadOnlyComposable
    fun colors(): MinoPaginationDotsColors = MinoAndroidTheme.colors.defaultPaginationDotsColors

    /**
     * 기본값에서 일부만 바꾼 [MinoPaginationDotsColors]를 만든다.
     * [Color.Unspecified]는 기본값 유지를 뜻한다.
     */
    @Composable
    @ReadOnlyComposable
    fun colors(
        selectedDotColor: Color = Color.Unspecified,
        unselectedDotColor: Color = Color.Unspecified,
    ): MinoPaginationDotsColors =
        MinoAndroidTheme.colors.defaultPaginationDotsColors.copy(
            selectedDotColor = selectedDotColor,
            unselectedDotColor = unselectedDotColor,
        )

    /** 두 슬롯은 같은 색에서 불투명도로만 갈린다. */
    internal val ColorScheme.defaultPaginationDotsColors: MinoPaginationDotsColors
        get() =
            defaultPaginationDotsColorsCached
                ?: run {
                    val dotColor = fromToken(PaginationDotsTokens.DotColor)
                    MinoPaginationDotsColors(
                        selectedDotColor = dotColor,
                        unselectedDotColor = dotColor.copy(alpha = PaginationDotsTokens.UnselectedDotOpacity),
                    )
                }.also { defaultPaginationDotsColorsCached = it }

    /** [selected]에 대응하는 점 색. */
    @Stable
    internal fun dotColor(
        colors: MinoPaginationDotsColors,
        selected: Boolean,
    ): Color = if (selected) colors.selectedDotColor else colors.unselectedDotColor
}

/**
 * [MinoPaginationDots]의 상태별 색. 슬롯 값이 [Color.Unspecified]면 [copy]에서 원본을 유지한다.
 */
@Immutable
class MinoPaginationDotsColors(
    val selectedDotColor: Color,
    val unselectedDotColor: Color,
) {
    fun copy(
        selectedDotColor: Color = this.selectedDotColor,
        unselectedDotColor: Color = this.unselectedDotColor,
    ): MinoPaginationDotsColors =
        MinoPaginationDotsColors(
            selectedDotColor = selectedDotColor.takeOrElse { this.selectedDotColor },
            unselectedDotColor = unselectedDotColor.takeOrElse { this.unselectedDotColor },
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinoPaginationDotsColors) return false

        if (selectedDotColor != other.selectedDotColor) return false
        if (unselectedDotColor != other.unselectedDotColor) return false

        return true
    }

    override fun hashCode(): Int =
        arrayOf(
            selectedDotColor,
            unselectedDotColor,
        ).contentHashCode()
}
