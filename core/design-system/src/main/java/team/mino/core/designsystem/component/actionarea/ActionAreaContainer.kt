package team.mino.core.designsystem.component.actionarea

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import team.mino.core.designsystem.component.actionarea.token.ActionAreaTokens
import team.mino.core.designsystem.foundation.color.token.value
import team.mino.core.designsystem.foundation.typography.token.value

/**
 * [MinoActionArea]의 껍데기. 컨테이너 패딩·캡션·액션 간 간격과 [sticky] 배경·상단 페이드,
 * [extra] 영역과 그 위 구분선을 그린다.
 *
 * 액션을 어떻게 조합하고 어떤 방향으로 늘어놓는지는 [content]가 정하고(계열마다 다르다),
 * 그 바깥의 표면·여백은 전부 여기가 소유한다.
 * Figma도 같은 선으로 나뉘어 있다 — 액션 묶음은 `Action Area/Resource/Actions` 리소스가,
 * extra·divider·상단 라운드는 `Action Area/Action Area` 컴포넌트셋이 갖는다.
 */
@Composable
internal fun ActionAreaContainer(
    sticky: Boolean,
    caption: String?,
    divider: Boolean,
    extra: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // 표면(배경)은 sticky이거나 extra가 붙었을 때만 존재한다. 둘 다 아니면 콘텐츠 흐름 안에 놓인
    // 기본형이라 화면 배경이 그대로 비쳐야 한다.
    val hasSurface = sticky || extra != null
    val surfaceColor = MinoActionAreaDefaults.surfaceColor

    Column(modifier = modifier) {
        if (sticky) {
            val fadeBrush = remember(surfaceColor) {
                Brush.verticalGradient(
                    *Array(ActionAreaTokens.StickyGradientAlphaStops.size) { index ->
                        val (position, alpha) = ActionAreaTokens.StickyGradientAlphaStops[index]
                        position to surfaceColor.copy(alpha = alpha)
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

        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // extra가 붙으면 상단 모서리가 둥글어진다. 투명색을 칠하는 대신 배경 노드를
                    // 아예 만들지 않는 경로를 남겨 둔다 — 기본형이 그쪽이라 흔한 경로다.
                    .then(if (extra != null) Modifier.clip(ActionAreaTokens.ExtraShape) else Modifier)
                    .then(if (hasSurface) Modifier.background(surfaceColor) else Modifier),
            ) {
                if (extra != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ActionAreaTokens.ExtraPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        extra()
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ActionAreaTokens.ContainerPadding),
                    verticalArrangement = Arrangement.spacedBy(ActionAreaTokens.ContainerSpacing),
                ) {
                    if (caption != null) {
                        Text(
                            text = caption,
                            modifier = Modifier.fillMaxWidth(),
                            style = ActionAreaTokens.CaptionFont.value,
                            color = ActionAreaTokens.CaptionColor.value,
                            textAlign = TextAlign.Center,
                        )
                    }
                    content()
                }
            }

            // Figma는 구분선을 루트에 `inset-0`으로 얹은 별도 레이어로 두고 위쪽 변만 그린다.
            // 상단 라운드를 따라가지 않는 곧은 선이라 그대로 옮긴다.
            if (extra != null && divider) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(ActionAreaTokens.DividerThickness)
                        .background(ActionAreaTokens.DividerColor.value),
                )
            }
        }
    }
}
