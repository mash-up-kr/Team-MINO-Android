package team.mino.core.designsystem.component.actionarea

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.component.actionarea.token.ActionAreaTokens

/**
 * 액션 영역 3종([MinoActionArea]·[MinoSubActionArea]·[MinoAlternativeActionArea])이 공유하는
 * 껍데기. 컨테이너 패딩·액션 간 간격과 [sticky] 배경·상단 페이드를 그린다.
 *
 * 셋이 다른 것은 [content] 안쪽뿐이다. 배경·페이드·패딩은 Figma가 액션 영역 공통으로 정의한
 * 값이라 항상 같아야 해서, 세 파일에 복사하지 않고 여기 한 곳에 둔다.
 */
@Composable
internal fun ActionAreaContainer(
    sticky: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    // sticky가 아니면 배경이 없다. Figma에서 배경 레이어가 Sticky 속성에 묶여 있어, 기본형은
    // 콘텐츠 흐름 안에서 화면 배경이 그대로 비치는 상태다.
    val containerColor = if (sticky) MinoActionAreaDefaults.stickyContainerColor else Color.Transparent

    Column(modifier = modifier) {
        if (sticky) {
            val fadeBrush = remember(containerColor) {
                Brush.verticalGradient(
                    *Array(ActionAreaTokens.StickyGradientAlphaStops.size) { index ->
                        val (position, alpha) = ActionAreaTokens.StickyGradientAlphaStops[index]
                        position to containerColor.copy(alpha = alpha)
                    },
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ActionAreaTokens.StickyGradientHeight)
                    .background(fadeBrush),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // 투명색을 칠하는 대신 아예 배경 노드를 만들지 않는다. 기본값이 sticky=false라
                // 이쪽이 흔한 경로다.
                .then(if (sticky) Modifier.background(containerColor) else Modifier)
                .padding(ActionAreaTokens.ContainerPadding),
            verticalArrangement = Arrangement.spacedBy(ActionAreaTokens.ActionColumnSpacing),
            content = content,
        )
    }
}
