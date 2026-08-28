package team.mino.core.designsystem.component.scrollbar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 * 목록의 스크롤 위치를 목록 오른쪽 끝에 표시하는 막대(Figma `Scroll Bar/Scroll Bar`).
 *
 * 목록 위에 겹쳐 놓이므로 자체 배경도 트랙 배경도 그리지 않는다 — 칠하는 것은 썸뿐이다.
 * 썸의 길이와 위치는 [scrollState]가 알려주는 뷰포트/콘텐츠 비율에서 나온다.
 *
 * **표시 전용이다.** 썸을 끌어 목록을 움직이는 상호작용은 갖지 않으며, 스크롤 여부에 따라
 * 나타났다 사라지지도 않는다. 스크롤할 것이 없으면 썸이 트랙을 가득 채운다.
 *
 * 높이는 호출부가 정한다. 별도 제약이 없으면 부모가 주는 높이를 모두 채운다.
 */
@Composable
fun MinoScrollBar(
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val thumbColor = MinoScrollBarDefaults.thumbColor

    Canvas(
        modifier = modifier
            .width(MinoScrollBarDefaults.width)
            .fillMaxHeight()
            .padding(MinoScrollBarDefaults.trackPadding),
    ) {
        val thumb = scrollState.thumbRange() ?: return@Canvas
        val thumbTop = size.height * thumb.start
        val thumbHeight = size.height * (thumb.endInclusive - thumb.start)

        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(x = 0f, y = thumbTop),
            size = Size(width = size.width, height = thumbHeight),
            // 트랙 폭의 절반이면 양 끝이 반원이 되어 캡슐 모양이 된다.
            cornerRadius = CornerRadius(size.width / 2f),
        )
    }
}

/**
 * 썸이 차지할 구간을 트랙 전체(0f..1f) 기준 비율로 돌려준다. 그릴 것이 없으면 null.
 *
 * `LazyColumn`은 보이지 않는 항목의 크기를 모르므로, 보이는 항목의 평균 크기로 전체 콘텐츠
 * 길이를 어림한다. 항목 높이가 균일하면 정확하고, 들쭉날쭉하면 스크롤에 따라 썸 길이가 조금씩
 * 흔들린다.
 */
private fun LazyListState.thumbRange(): ClosedFloatingPointRange<Float>? {
    val layoutInfo = layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return null

    val viewportSize = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
    if (viewportSize <= 0) return null

    val itemStride = visibleItems.sumOf { it.size } / visibleItems.size + layoutInfo.mainAxisItemSpacing
    val contentSize = itemStride * layoutInfo.totalItemsCount - layoutInfo.mainAxisItemSpacing
    if (contentSize <= viewportSize) return FullTrack

    val scrolled = firstVisibleItemIndex * itemStride + firstVisibleItemScrollOffset
    val thumbFraction = viewportSize.toFloat() / contentSize
    val startFraction = (scrolled.toFloat() / contentSize).coerceIn(0f, 1f - thumbFraction)

    return startFraction..(startFraction + thumbFraction)
}

private val FullTrack = 0f..1f
