package team.mino.core.designsystem.component.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import team.mino.core.designsystem.component.menu.token.MenuTokens
import team.mino.core.designsystem.foundation.shadow.MinoShadow
import team.mino.core.designsystem.util.modifier.shadow.dropShadow

/**
 * 선택 가능한 행동을 나열하는 메뉴 컨테이너. [content]에는 [MinoMenuItem]·[MinoMenuTitle]을 나열한다.
 *
 * @param border 컨테이너 외곽선. `null`이면 그리지 않는다.
 * @param shadow 컨테이너 그림자. `null`이면 그리지 않는다.
 * @param actionArea 아이템 목록 아래에 붙는 액션 영역([MinoMenuActionArea]). 구분선이 메뉴 폭을
 *   가로질러야 해서 아이템 목록의 좌우 여백 **바깥**에 놓인다. `null`이면 표시하지 않는다.
 *   액션 영역을 쓸 때는 구분선이 메뉴 폭 전체를 덮도록 호출부가 메뉴 폭을 정해 준다.
 */
@Composable
fun MinoMenu(
    modifier: Modifier = Modifier,
    shape: Shape = MinoMenuDefaults.shape,
    containerColor: Color = MinoMenuDefaults.containerColor,
    border: BorderStroke? = MinoMenuDefaults.border,
    shadow: MinoShadow? = MinoMenuDefaults.shadow,
    actionArea: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .widthIn(min = MenuTokens.ContainerMinWidth)
            .then(if (shadow != null) Modifier.dropShadow(shape = shape, shadow = shadow) else Modifier)
            .clip(shape)
            .background(containerColor)
            .then(if (border != null) Modifier.border(border = border, shape = shape) else Modifier),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = MenuTokens.ContainerHorizontalPadding,
                vertical = MenuTokens.ContainerVerticalPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(MenuTokens.CellSpacing),
            content = content,
        )
        actionArea?.invoke()
    }
}
