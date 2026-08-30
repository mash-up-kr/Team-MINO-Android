package team.mino.core.designsystem.component.menu

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.component.menu.token.MenuTokens
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.typography.token.value

/**
 * [MinoMenu] 안에서 아이템 묶음을 나누는 그룹 헤더(Figma `Menu/Resource/Item/Title`).
 * 클릭 대상이 아니라 표시 전용이다.
 *
 * 좌우 여백은 [MinoMenuItem]의 라벨과 시작점이 맞도록 셀과 같은 값을 쓴다.
 */
@Composable
fun MinoMenuTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MenuTokens.ItemHorizontalPadding)
            .padding(
                horizontal = MenuTokens.TitleHorizontalPadding,
                vertical = MenuTokens.TitleVerticalPadding,
            ),
        text = text,
        color = MenuTokens.TitleColor.value,
        style = MenuTokens.TitleFont.value,
    )
}
