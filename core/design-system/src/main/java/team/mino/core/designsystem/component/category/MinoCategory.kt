package team.mino.core.designsystem.component.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.designsystem.component.category.token.CategoryTokens
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.fade.horizontalFadingEdge
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 정보를 특정 주제·그룹으로 나누어 접근하는 가로 스크롤 탭(Figma `Category/Category`).
 * 좌우 끝은 스크롤 가능함을 나타내는 그라데이션 페이드로 처리된다.
 *
 * @param trailingIconBadge 아이콘 버튼 위에 알림 점(Push Badge)을 표시할지 여부.
 * @param content `null`이 아니면 탭 목록 오른쪽에 고정 아이콘 버튼을 그린다(Figma Icon Button 슬롯).
 */
@Composable
fun MinoCategory(
    items: ImmutableList<String>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onTrailingIconClick: () -> Unit = {},
    trailingIconBadge: Boolean = false,
    colors: MinoCategoryColors = MinoCategoryDefaults.colors(),
    content: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CategoryTokens.ContentGap),
    ) {
        LazyRow(
            modifier = Modifier
                .weight(1f)
                .horizontalFadingEdge(edgeWidth = CategoryTokens.GradientEdgeWidth),
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
        if (content != null) {
            CategoryIconButton(
                onClick = onTrailingIconClick,
                badge = trailingIconBadge,
                colors = colors,
                content = content,
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

@Composable
private fun CategoryIconButton(
    onClick: () -> Unit,
    badge: Boolean,
    colors: MinoCategoryColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(CategoryTokens.IconButtonTouchSize)
            .clip(CategoryTokens.IconButtonShape)
            .rippleSingleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.size(CategoryTokens.IconButtonSize), contentAlignment = Alignment.Center) {
            content()
            if (badge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(CategoryTokens.PushBadgeSize)
                        .clip(CircleShape)
                        .background(colors.pushBadgeColor),
                )
            }
        }
    }
}
