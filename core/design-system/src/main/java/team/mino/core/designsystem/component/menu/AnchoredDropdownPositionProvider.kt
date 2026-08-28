package team.mino.core.designsystem.component.menu

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider

/**
 * 앵커(트리거) 바로 아래(또는 위)에 [alignEnd] 기준으로 정렬해 붙이는 드롭다운 메뉴·팝업의 공통 위치
 * 계산. [MinoMenu] 소비처(더보기 메뉴, 정렬 메뉴 등)가 각자 `PopupPositionProvider`를 새로 구현하던 것을
 * 한 곳으로 모은다 — 앵커 대비 오프셋 규칙이 갈리면 그중 하나만 고쳐지는 사고를 막는다.
 *
 * @param alignEnd `true`면 앵커 오른쪽 끝에 팝업 오른쪽 끝을 맞추고, `false`면 앵커 왼쪽 끝에 팝업
 *   왼쪽 끝을 맞춘다.
 * @param expandUpward `true`면 앵커 위쪽으로, `false`면(기본) 앵커 아래쪽으로 [Gap]만큼 띄워 펼친다.
 */
class AnchoredDropdownPositionProvider(
    density: Density,
    private val alignEnd: Boolean,
    private val expandUpward: Boolean = false,
) : PopupPositionProvider {
    private val gapPx = with(density) { Gap.toPx() }.toInt()

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = if (alignEnd) anchorBounds.right - popupContentSize.width else anchorBounds.left
        val y = if (expandUpward) {
            anchorBounds.top - gapPx - popupContentSize.height
        } else {
            anchorBounds.bottom + gapPx
        }
        return IntOffset(x, y)
    }

    private companion object {
        val Gap = 8.dp
    }
}
