package team.mino.feature.home.main.model

import androidx.compose.runtime.Immutable

/**
 * 「방 선택 시트」(`다른 방 저장`)가 열려 있는 동안의 상태.
 *
 * `HomeUiState.savePicker`가 `null`이 아니면 시트가 열린 것이고, [selectedRoomIds]가 비어 있으면
 * `저장하기`가 비활성이다(spec EC-018).
 *
 * @property pinId 저장 대상 카드.
 * @property selectedRoomIds 체크된 방들. 같은 방을 다시 탭하면 여기서 빠진다.
 */
@Immutable
internal data class SavePickerState(
    val pinId: String,
    val selectedRoomIds: Set<String> = emptySet(),
)
