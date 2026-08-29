package team.mino.core.designsystem.component.pagination

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import team.mino.core.designsystem.component.pagination.token.PaginationDotsTokens
import team.mino.core.designsystem.util.modifier.selectable.rippleSingleSelectable

/**
 * 여러 쪽 중 지금 몇 번째를 보고 있는지 점으로 알리는 표시(Figma `Pagination/Dots`).
 *
 * 점은 모두 같은 크기의 원이고, 선택된 하나만 색이 짙다. 선택 여부로 점이 커지거나 캡슐로
 * 늘어나지 않는다.
 *
 * **이 컴포넌트는 캐러셀을 모른다.** 페이저 상태를 받지 않고 점의 개수와 선택된 인덱스,
 * 그리고 눌렸을 때 알릴 곳만 받는다. 인덱스를 실제 쪽과 잇는 일은 호출부의 몫이다.
 *
 * @param count 그릴 점의 개수.
 * @param selectedIndex 짙게 그릴 점의 인덱스(0-based). 범위를 벗어나면 짙은 점이 없다.
 * @param onDotClick 점을 눌렀을 때 그 인덱스를 알린다. `null`이면 표시 전용이라 눌리지 않는다.
 */
@Composable
fun MinoPaginationDots(
    count: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onDotClick: ((index: Int) -> Unit)? = null,
    colors: MinoPaginationDotsColors = MinoPaginationDotsDefaults.colors(),
) {
    Row(
        modifier = modifier.then(if (onDotClick == null) Modifier else Modifier.selectableGroup()),
        horizontalArrangement = Arrangement.spacedBy(MinoPaginationDotsDefaults.dotSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val selected = index == selectedIndex

            Box(
                modifier = Modifier
                    .size(MinoPaginationDotsDefaults.dotSize)
                    .background(
                        color = MinoPaginationDotsDefaults.dotColor(colors, selected),
                        shape = PaginationDotsTokens.DotShape,
                    ).then(
                        if (onDotClick == null) {
                            Modifier
                        } else {
                            Modifier.rippleSingleSelectable(
                                selected = selected,
                                role = Role.Tab,
                                onClick = { onDotClick(index) },
                            )
                        },
                    ),
            )
        }
    }
}
