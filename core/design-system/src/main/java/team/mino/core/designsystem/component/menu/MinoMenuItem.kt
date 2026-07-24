package team.mino.core.designsystem.component.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import team.mino.core.designsystem.component.menu.token.MenuTokens
import team.mino.core.designsystem.foundation.shape.token.value
import team.mino.core.designsystem.foundation.typography.token.value
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable

/**
 * [MinoMenu] 안에 나열하는 메뉴 아이템 셀.
 *
 * @param active 현재 선택된 값 표시. 텍스트가 강조 스타일로 바뀐다.
 * @param caption 본문 아래 보조 설명. `null`이면 표시하지 않는다.
 * @param enabled `false`면 클릭이 막히고 셀 전체가 흐려진다.
 * @param contentPadding 셀 세로 패딩. [MinoMenuDefaults.ItemContentPadding](12dp) 또는
 *   [MinoMenuDefaults.ItemContentPaddingCompact](8dp).
 */
@Composable
fun MinoMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    active: Boolean = false,
    caption: String? = null,
    colors: MinoMenuItemColors = MinoMenuDefaults.itemColors(),
    contentPadding: PaddingValues = MinoMenuDefaults.ItemContentPadding,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MenuTokens.ItemShape.value)
            .rippleSingleClickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else MenuTokens.DisabledOpacity)
            .padding(horizontal = MenuTokens.ItemHorizontalPadding)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(MenuTokens.LabelCaptionSpacing),
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MenuTokens.LabelMinHeight)
                .wrapContentHeight(Alignment.CenterVertically),
            text = text,
            color = colors.textColor(enabled = enabled, active = active),
            style = if (active) MenuTokens.ActiveLabelFont.value else MenuTokens.LabelFont.value,
        )
        if (caption != null) {
            Text(
                text = caption,
                color = colors.captionColor(enabled = enabled),
                style = MenuTokens.CaptionFont.value,
            )
        }
    }
}
