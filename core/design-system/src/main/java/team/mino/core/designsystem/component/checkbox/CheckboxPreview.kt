package team.mino.core.designsystem.component.checkbox

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

/**
 * [MinoCheckbox]의 속성 축 프리뷰.
 *
 * 컴포넌트 정의가 있는 라이브러리 파일에 편집 권한이 없어 Figma 문서 페이지의 블록 순서·속성명을
 * 확인하지 못했다. 헤딩은 Figma 속성명이 아니라 코드 파라미터명이며, 권한이 열리면 문서 페이지에
 * 맞춰 다시 정렬한다.
 *
 * 만들지 않은 축:
 * - `Interaction`(프레스 오버레이) — Android에서는 리플이 대신하므로 블록으로 두지 않는다.
 * - `enabled`의 색 — Figma가 비활성 색을 정의하지 않아 입력 차단만 다르다. 아래 블록은 색이 아니라
 *   눌리는지 여부를 보는 축이다.
 */
@UiModePreviews
@Composable
private fun CheckboxPreview() {
    PreviewPage {
        PreviewProperty(name = "checked", values = "false · true") {
            PreviewRow {
                MinoCheckbox(checked = false, onCheckedChange = {})
                MinoCheckbox(checked = true, onCheckedChange = {})
            }
        }
        PreviewProperty(name = "enabled", values = "true · false") {
            PreviewRow {
                ToggleableCheckbox(enabled = true)
                ToggleableCheckbox(enabled = false)
            }
        }
    }
}

/** 인스턴스 하나에 대응하는 프리뷰 항목. 체크 상태를 각자 들고 있어 눌러 볼 수 있다. */
@Composable
private fun ToggleableCheckbox(
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var checked by remember { mutableStateOf(false) }

    MinoCheckbox(
        modifier = modifier,
        checked = checked,
        onCheckedChange = { checked = it },
        enabled = enabled,
    )
}
