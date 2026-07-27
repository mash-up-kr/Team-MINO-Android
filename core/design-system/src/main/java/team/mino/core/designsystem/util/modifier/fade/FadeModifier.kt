package team.mino.core.designsystem.util.modifier.fade

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
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
            val edgeWidthPx = minOf(edgeWidth.toPx(), size.width / 2f)
            val leadingBrush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.Black),
                startX = 0f,
                endX = edgeWidthPx,
            )
            val trailingBrush = Brush.horizontalGradient(
                colors = listOf(Color.Black, Color.Transparent),
                startX = size.width - edgeWidthPx,
                endX = size.width,
            )
            onDrawWithContent {
                drawContent()
                drawRect(
                    brush = leadingBrush,
                    size = size.copy(width = edgeWidthPx),
                    blendMode = BlendMode.DstIn,
                )
                drawRect(
                    brush = trailingBrush,
                    topLeft = Offset(size.width - edgeWidthPx, 0f),
                    size = size.copy(width = edgeWidthPx),
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
            val edgeWidthPx = minOf(edgeWidth.toPx(), size.width / 2f)
            val leadingBrush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.Black),
                startX = 0f,
                endX = edgeWidthPx,
            )
            val trailingBrush = Brush.horizontalGradient(
                colors = listOf(Color.Black, Color.Transparent),
                startX = size.width - edgeWidthPx,
                endX = size.width,
            )
            onDrawWithContent {
                drawContent()
                if (scrollState.canScrollBackward) {
                    drawRect(
                        brush = leadingBrush,
                        size = size.copy(width = edgeWidthPx),
                        blendMode = BlendMode.DstIn,
                    )
                }
                if (scrollState.canScrollForward) {
                    drawRect(
                        brush = trailingBrush,
                        topLeft = Offset(size.width - edgeWidthPx, 0f),
                        size = size.copy(width = edgeWidthPx),
                        blendMode = BlendMode.DstIn,
                    )
                }
            }
        }
