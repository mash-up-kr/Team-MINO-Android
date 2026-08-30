package team.mino.feature.sharereceiver.picker.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.actionarea.ActionAreaAction
import team.mino.core.designsystem.component.actionarea.MinoActionArea
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.sharereceiver.R

/**
 * 시트 맨 아래에 고정으로 놓이는 저장 액션. 목록 밖에 있어 스크롤에 따라 움직이지 않고(UX-004),
 * 방이 하나도 없는 상태에서도 그대로 보인다(FR-013).
 *
 * **선택을 들지 않는다.** 어떤 방이 몇 개 골라졌는지는 알지 않고 [enabled] 하나만 받는다 —
 * 그 판단은 상태를 가진 쪽이 한다(FR-009).
 *
 * 비활성일 때 [onSaveClick]을 걸러내지 않는다. 버튼이 클릭 자체를 받지 않아 눌러도 아무 반응이 없다(UX-002).
 *
 * 높이를 정하지 않는다 — 시트 안에서 얼마를 차지할지는 시트를 조립하는 쪽이 정한다.
 * 하단 시스템 인셋도 같은 이유로 여기서 얹지 않고 [modifier]로 받는다.
 */
@Composable
internal fun RoomPickerActionArea(
    enabled: Boolean,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MinoActionArea(
        modifier = modifier.fillMaxWidth(),
        mainAction = ActionAreaAction(
            text = stringResource(R.string.sharereceiver_action_save),
            onClick = onSaveClick,
            enabled = enabled,
        ),
        // 목록이 이 영역 밑으로 지나가며 잘리므로, 배경과 그 위 페이드가 함께 필요하다.
        sticky = true,
    )
}

@UiModePreviews
@Composable
private fun RoomPickerActionAreaPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PreviewStateSpacing),
        ) {
            RoomPickerActionArea(enabled = false, onSaveClick = {})
            RoomPickerActionArea(enabled = true, onSaveClick = {})
        }
    }
}

private val PreviewStateSpacing = 8.dp
