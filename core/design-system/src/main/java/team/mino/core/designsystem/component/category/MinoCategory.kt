package team.mino.core.designsystem.component.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.designsystem.component.category.token.CategoryTokens
import team.mino.core.designsystem.component.category.token.chipSize
import team.mino.core.designsystem.component.category.token.chipSpacing
import team.mino.core.designsystem.component.category.token.trailingSize
import team.mino.core.designsystem.component.category.token.verticalPadding
import team.mino.core.designsystem.component.chip.token.contentPadding
import team.mino.core.designsystem.component.chip.token.font
import team.mino.core.designsystem.component.chip.token.shape
import team.mino.core.designsystem.component.chip.token.textHorizontalPadding
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.fade.horizontalFadingEdge
import team.mino.core.designsystem.util.modifier.selectable.rippleSingleSelectable
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 정보를 특정 주제·그룹으로 나누어 접근하는 가로 스크롤 탭(Figma `Category/Category`).
 * 좌우 끝은 스크롤 가능함을 나타내는 그라데이션 페이드로 처리된다.
 *
 * 항목은 Chip과 **치수가 같고 색만 다르다.** 그래서 `MinoChip`을 그대로 쓰지 않고 전용 항목을
 * 그리되, 크기 토큰은 [CategorySize]→`ChipSize` 매핑으로 공유한다.
 *
 * @param size 항목 크기. Figma `Size` 속성에 대응하며 Category 높이를 24/32/36/40dp로 가른다.
 * @param variant 항목 색 계열. Figma `Variant` 속성에 대응.
 * @param horizontalPadding 스크롤 콘텐츠 좌우에 20dp 여백을 준다. Figma `Horizontal Padding` 속성.
 *   여백은 스크롤 영역 **안쪽**에 들어가므로 스크롤하면 첫·마지막 항목이 여백까지 밀려난다.
 * @param verticalPadding 위아래에 여백을 준다. Figma `verticalPadding` 속성.
 * @param trailingContent 스크롤 영역 오른쪽에 고정으로 붙는 슬롯. Figma는 이 자리에 아이콘 버튼을
 *   둔다. 페이드 대상이 아니라 항상 또렷하게 보인다. `null`이면 표시하지 않는다.
 */
@Composable
fun MinoCategory(
    items: ImmutableList<String>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    size: CategorySize = CategorySize.Medium,
    variant: CategoryVariant = CategoryVariant.Normal,
    horizontalPadding: Boolean = false,
    verticalPadding: Boolean = false,
    colors: MinoCategoryColors = MinoCategoryDefaults.colors(variant),
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val listState = rememberLazyListState()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(CategoryTokens.TrailingSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .horizontalFadingEdge(listState, edgeWidth = CategoryTokens.GradientEdgeWidth),
            contentPadding = PaddingValues(
                horizontal = if (horizontalPadding) CategoryTokens.HorizontalPadding else 0.dp,
                vertical = if (verticalPadding) size.verticalPadding else 0.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(size.chipSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            itemsIndexed(items) { index, text ->
                CategoryChip(
                    text = text,
                    active = index == selectedIndex,
                    onClick = { onItemClick(index) },
                    size = size,
                    colors = colors,
                )
            }
        }
        if (trailingContent != null) {
            Box(
                modifier = Modifier.size(size.trailingSize),
                contentAlignment = Alignment.Center,
            ) {
                trailingContent()
            }
        }
    }
}

@Composable
private fun CategoryChip(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
    size: CategorySize,
    colors: MinoCategoryColors,
    modifier: Modifier = Modifier,
) {
    val chipSize = size.chipSize

    Box(
        modifier = modifier
            .surface(
                shape = chipSize.shape(),
                containerColor = colors.containerColor(active),
                borderColor = colors.borderColor(active),
                borderWidth = CategoryTokens.ChipBorderWidth,
            ).rippleSingleSelectable(selected = active, onClick = onClick)
            .padding(chipSize.contentPadding()),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = chipSize.textHorizontalPadding),
            text = text,
            color = colors.contentColor(active),
            style = chipSize.font.value,
        )
    }
}

/**
 * [MinoCategory]의 크기. Figma `Size` 속성(Small·Medium·Large·XLarge)에 대응.
 * 항목 칩 크기와 이름이 한 단계 밀려 있다 — `CategoryTokens`의 매핑 참조.
 */
enum class CategorySize {
    Small,
    Medium,
    Large,
    XLarge,
}

/** [MinoCategory] 항목의 색 계열. Figma `Variant` 속성(Normal·Alternative)에 대응. */
enum class CategoryVariant {
    /** 선택 항목을 검정으로 채운다. 비선택 항목은 배경색 + 테두리. */
    Normal,

    /** 선택 항목을 Primary 틴트 + 테두리로 표시한다. 비선택 항목은 테두리만. */
    Alternative,
}
