package team.mino.core.designsystem.component.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.designsystem.component.category.token.CategoryTokens
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.fade.horizontalFadingEdge
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 정보를 특정 주제·그룹으로 나누어 접근하는 가로 스크롤 탭(Figma `Category/Category`).
 * 좌우 끝은 스크롤 가능함을 나타내는 그라데이션 페이드로 처리된다.
 */
@Composable
fun MinoCategory(
    items: ImmutableList<String>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    colors: MinoCategoryColors = MinoCategoryDefaults.colors(),
) {
    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        modifier = modifier.horizontalFadingEdge(listState, edgeWidth = CategoryTokens.GradientEdgeWidth),
        horizontalArrangement = Arrangement.spacedBy(CategoryTokens.ChipSpacing),
    ) {
        itemsIndexed(items) { index, text ->
            CategoryChip(
                text = text,
                active = index == selectedIndex,
                onClick = { onItemClick(index) },
                colors = colors,
            )
        }
    }
}

@Composable
private fun CategoryChip(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
    colors: MinoCategoryColors,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (active) colors.chipActiveContainerColor else colors.chipInactiveContainerColor
    val contentColor = if (active) colors.chipActiveContentColor else colors.chipInactiveContentColor

    Box(
        modifier = modifier
            .surface(
                shape = CategoryTokens.ChipShape,
                containerColor = containerColor,
                borderColor = if (!active) colors.chipInactiveBorderColor else null,
                borderWidth = CategoryTokens.ChipBorderWidth,
            ).rippleSingleClickable(onClick = onClick)
            .padding(CategoryTokens.ChipPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = contentColor, style = CategoryTokens.ChipFont.value)
    }
}
