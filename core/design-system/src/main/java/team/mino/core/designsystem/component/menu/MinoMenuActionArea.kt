package team.mino.core.designsystem.component.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import team.mino.core.designsystem.component.menu.token.MenuTokens
import team.mino.core.designsystem.foundation.color.token.value

/**
 * [MinoMenu] 맨 아래에 고정으로 붙는 액션 영역(Figma `Menu/Resource/Action Area`).
 * [MinoMenu]의 `actionArea` 슬롯에 넣어 쓴다 — 메뉴 컨테이너의 좌우 여백 바깥에 놓여야
 * 구분선이 메뉴 폭 전체를 가로지른다.
 *
 * 배경은 그리지 않고 메뉴 컨테이너의 배경을 그대로 비친다. 컨테이너 색을 바꿔도 따라가도록 한 것이다.
 *
 * @param leadingContent 왼쪽 슬롯. Figma는 이 자리에 텍스트 버튼·배지·아이콘·체크박스를 둔다.
 * @param trailingContent 오른쪽 슬롯. Figma는 이 자리에 `Small` 크기의 채운 버튼을 둔다.
 *   [leadingContent]가 없어도 오른쪽 끝에 붙는다.
 */
@Composable
fun MinoMenuActionArea(
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val dividerColor = MenuTokens.ActionAreaDividerColor.value

    Row(
        // 구분선은 바운즈 안쪽 위에 그린다. Figma의 stroke도 프레임 높이를 늘리지 않는다.
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    color = dividerColor,
                    size = size.copy(height = MenuTokens.ActionAreaDividerThickness.toPx()),
                )
            }.padding(MenuTokens.ActionAreaPadding),
        horizontalArrangement = Arrangement.spacedBy(MenuTokens.ActionAreaContentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingContent != null) {
            Box(modifier = Modifier.weight(1f).padding(start = MenuTokens.ActionAreaLeadingStartPadding)) {
                leadingContent()
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        trailingContent?.invoke()
    }
}
