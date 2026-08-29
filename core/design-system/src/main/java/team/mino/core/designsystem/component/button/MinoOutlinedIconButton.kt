package team.mino.core.designsystem.component.button

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 아이콘 하나만 담는 원형 아웃라인 버튼(Figma `Button/Icon/Outlined`).
 *
 * 배경 없이 테두리만 두르고 모서리가 완전한 원이다. 콘텐츠 위에 얹혀 닫기·나가기처럼
 * 화면 위계를 벗어나는 동작을 받는 자리에 쓴다.
 *
 * [MinoIconButton]과는 **다른 Figma 컴포넌트셋**이다. 생김새가 비슷하고 치수도 겹치지만
 * 속성 축이 달라(이쪽은 size·style 축이 없다) 코드도 컴포넌트를 나눈다. 겹치는 치수는
 * 컴포넌트 토큰으로 공유하므로 Figma가 값을 바꾸면 두 컴포넌트가 함께 움직인다.
 *
 * @param contentPadding 아이콘 둘레 패딩. 이 값이 아이콘 크기와 합쳐져 버튼의 지름이 된다.
 * @param content Figma의 `icon` 슬롯에 대응한다. 슬롯 안에서는 `LocalContentColor`가 지정돼 있어,
 *   색을 따로 넘기지 않은 [androidx.compose.material3.Icon]은 그 색으로 그려진다.
 */
@Composable
fun MinoOutlinedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = MinoOutlinedIconButtonDefaults.contentPadding,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .surface(
                shape = MinoOutlinedIconButtonDefaults.shape,
                containerColor = Color.Transparent,
                borderColor = MinoOutlinedIconButtonDefaults.borderColor,
                borderWidth = MinoOutlinedIconButtonDefaults.borderWidth,
            ).rippleSingleClickable(onClick = onClick)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides MinoOutlinedIconButtonDefaults.contentColor) {
            Box(
                modifier = Modifier.size(MinoOutlinedIconButtonDefaults.iconSize),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}
