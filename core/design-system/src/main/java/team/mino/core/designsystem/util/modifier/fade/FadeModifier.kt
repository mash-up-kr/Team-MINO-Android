package team.mino.core.designsystem.util.modifier.fade

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 가로 스크롤 콘텐츠의 좌우 끝을 [edgeWidth]만큼 투명하게 페이드아웃한다.
 * Figma `Gradient/Solid`(mask-image 기반) 컴포넌트의 Compose 대응 구현.
 * 그라데이션 브러시는 [drawWithCache]로 크기가 바뀔 때만 다시 만든다.
 * 스크롤 상태와 무관하게 항상 양끝을 페이드하므로, `LazyListState`가 있다면
 * [horizontalFadingEdge] (scrollState 오버로드)로 스크롤 가능한 방향만 페이드하는 쪽을 우선 고려한다.
 */
fun Modifier.horizontalFadingEdge(edgeWidth: Dp = 24.dp): Modifier =
    this
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithCache {
            val brushes = fadingEdgeBrushes(edgeWidth)
            onDrawWithContent {
                drawContent()
                drawRect(
                    brush = brushes.leading,
                    size = size.copy(width = brushes.edgeWidthPx),
                    blendMode = BlendMode.DstIn,
                )
                drawRect(
                    brush = brushes.trailing,
                    topLeft = Offset(size.width - brushes.edgeWidthPx, 0f),
                    size = size.copy(width = brushes.edgeWidthPx),
                    blendMode = BlendMode.DstIn,
                )
            }
        }

/**
 * [horizontalFadingEdge]와 같지만, [scrollState]가 스크롤 가능한 방향에만 페이드를 그린다.
 * 스크롤할 내용이 없거나 맨 앞/끝까지 스크롤한 상태에서는 해당 끝의 첫·마지막 항목이 흐려지지 않는다.
 * `canScrollBackward`/`canScrollForward`는 스냅샷 상태라 draw 단계에서 읽어도 [drawWithCache]로
 * 캐시된 그라데이션 [Brush] 재생성 없이 프레임마다 최신 스크롤 상태로 다시 그려진다.
 */
fun Modifier.horizontalFadingEdge(
    scrollState: LazyListState,
    edgeWidth: Dp = 24.dp,
): Modifier =
    this
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithCache {
            val brushes = fadingEdgeBrushes(edgeWidth)
            onDrawWithContent {
                drawContent()
                if (scrollState.canScrollBackward) {
                    drawRect(
                        brush = brushes.leading,
                        size = size.copy(width = brushes.edgeWidthPx),
                        blendMode = BlendMode.DstIn,
                    )
                }
                if (scrollState.canScrollForward) {
                    drawRect(
                        brush = brushes.trailing,
                        topLeft = Offset(size.width - brushes.edgeWidthPx, 0f),
                        size = size.copy(width = brushes.edgeWidthPx),
                        blendMode = BlendMode.DstIn,
                    )
                }
            }
        }

/** [drawWithCache] 캐시 블록에서 [edgeWidth]로부터 좌우 페이드 그라데이션을 한 번만 만든다. */
private fun CacheDrawScope.fadingEdgeBrushes(edgeWidth: Dp): FadingEdgeBrushes {
    val edgeWidthPx = minOf(edgeWidth.toPx(), size.width / 2f)
    return FadingEdgeBrushes(
        edgeWidthPx = edgeWidthPx,
        leading = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.Black),
            startX = 0f,
            endX = edgeWidthPx,
        ),
        trailing = Brush.horizontalGradient(
            colors = listOf(Color.Black, Color.Transparent),
            startX = size.width - edgeWidthPx,
            endX = size.width,
        ),
    )
}

private class FadingEdgeBrushes(val edgeWidthPx: Float, val leading: Brush, val trailing: Brush)
