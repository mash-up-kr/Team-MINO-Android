package team.mino.core.designsystem.util.modifier.fade

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
 */
fun Modifier.horizontalFadingEdge(edgeWidth: Dp = 24.dp): Modifier =
    this
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithCache {
            val edgeWidthPx = edgeWidth.toPx()
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
