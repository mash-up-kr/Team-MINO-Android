package team.mino.core.designsystem.component.roomcolorchip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.PreviewRow
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.designsystem.util.preview.previewValues

/**
 * 방 색상 칩 컴포넌트셋 12개의 속성 축을 옮긴 프리뷰.
 *
 * Figma는 색마다 컴포넌트셋을 따로 두고 각 셋이 `state` 축(off·on)만 갖는다. 코드는 색을
 * [MinoRoomColor] 파라미터로 받는 한 컴포넌트라, 색을 하나의 축으로 묶어 아래 두 블록으로 옮겼다.
 *
 * 그리드 배치는 칩의 몫이 아니라 호출부의 몫이므로, `color` 블록의 줄바꿈은 Figma 문서 페이지의
 * 나열을 읽기 좋게 끊은 것일 뿐 컴포넌트가 정한 배치가 아니다.
 */
@UiModePreviews
@Composable
private fun RoomColorChipPreview() {
    PreviewPage {
        PreviewProperty(name = "state", values = "off · on") {
            PreviewRow {
                MinoRoomColorChip(color = MinoRoomColor.Red, selected = false, onSelect = {})
                MinoRoomColorChip(color = MinoRoomColor.Red, selected = true, onSelect = {})
            }
        }
        PreviewProperty(name = "color", values = MinoRoomColor.entries.previewValues()) {
            MinoRoomColor.entries.chunked(PREVIEW_COLUMN_COUNT).forEach { rowColors ->
                PreviewRow {
                    rowColors.forEach { color ->
                        SelectableRoomColorChip(color = color)
                    }
                }
            }
        }
    }
}

/** Figma 인스턴스 하나에 대응하는 프리뷰 항목. 선택 상태를 각자 들고 있어 눌러 볼 수 있다. */
@Composable
private fun SelectableRoomColorChip(
    color: MinoRoomColor,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf(false) }

    MinoRoomColorChip(
        modifier = modifier,
        color = color,
        selected = selected,
        onSelect = { selected = !selected },
    )
}

private const val PREVIEW_COLUMN_COUNT = 4
