package team.mino.core.designsystem.component.switch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import team.mino.core.designsystem.util.preview.PreviewPage
import team.mino.core.designsystem.util.preview.PreviewProperty
import team.mino.core.designsystem.util.preview.PreviewRow
import team.mino.core.designsystem.util.preview.UiModePreviews

/**
 * [MinoSwitch] 카탈로그 프리뷰.
 *
 * Figma 노드 대조는 [MinoSwitch] KDoc 참고. 컴포넌트가 실제로 갖는 두 축(`checked`·`enabled`)만
 * 확인한다.
 */
@UiModePreviews
@Composable
private fun SwitchPreview() {
    PreviewPage {
        PreviewProperty(name = "checked", values = "False · True") {
            PreviewRow {
                InteractiveSwitchPreview(initialChecked = false)
                InteractiveSwitchPreview(initialChecked = true)
            }
        }
        PreviewProperty(name = "enabled", values = "False · True") {
            PreviewRow {
                MinoSwitch(checked = false, onCheckedChange = {}, enabled = false)
                MinoSwitch(checked = true, onCheckedChange = {}, enabled = false)
            }
        }
    }
}

@Composable
private fun InteractiveSwitchPreview(initialChecked: Boolean) {
    var checked by remember { mutableStateOf(initialChecked) }
    MinoSwitch(checked = checked, onCheckedChange = { checked = it })
}
