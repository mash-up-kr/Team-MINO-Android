package team.mino.feature.sharereceiver.picker.screen

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.sharereceiver.R
import team.mino.feature.sharereceiver.picker.component.RoomPickerActionArea
import team.mino.feature.sharereceiver.picker.component.RoomPickerEmpty
import team.mino.feature.sharereceiver.picker.component.RoomPickerHeader
import team.mino.feature.sharereceiver.picker.component.RoomPickerList
import team.mino.feature.sharereceiver.picker.component.RoomPickerSheet
import team.mino.feature.sharereceiver.picker.model.RoomPickerItem
import team.mino.feature.sharereceiver.picker.vm.ShareReceiverIntent
import team.mino.feature.sharereceiver.picker.vm.ShareReceiverUiState

/**
 * 공유받은 장소를 어느 방에 저장할지 고르는 시트. 앱 화면 없이 이 시트가 화면 전체를 대신한다
 * (`research.md` R-008).
 *
 * 헤더와 액션 영역은 상태와 무관하게 늘 그 자리에 있고, 그 사이의 한 자리만 방 목록과 안내로 갈린다 —
 * 저장할 방이 없는 사용자도 같은 모습의 시트를 본다(FR-013).
 *
 * 불러오는 중을 따로 그리지 않는다. 시트는 조회를 기다리지 않고 이미 떠 있으며, 아직 아무것도 오지 않은
 * 시점은 방이 없는 것과 같은 화면이다(UX-009).
 *
 * 시트가 멈춰 선 단계는 시트가 아니라 [state]가 갖는다. 끌어 옮긴 결과를 의도로 올려 되받으므로 단계가
 * 바뀌어도 선택은 같은 상태 안에 그대로 남는다(TS-016).
 */
@Composable
internal fun ShareReceiverScreen(
    state: ShareReceiverUiState,
    onIntent: (ShareReceiverIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    RoomPickerSheet(
        onDismissRequest = { onIntent(ShareReceiverIntent.Dismiss) },
        modifier = modifier,
        step = state.sheetStep,
        roomCount = state.rooms.size,
        onStepChange = { step -> onIntent(ShareReceiverIntent.ChangeStep(step)) },
    ) {
        RoomPickerHeader()
        // 남는 자리를 목록이 가져간다. 헤더·액션 영역은 자기 높이를 지켜 스크롤에서 빠진다(UX-004).
        if (state.isEmpty) {
            RoomPickerEmpty(modifier = Modifier.weight(1f))
        } else {
            RoomPickerList(
                rooms = state.rooms,
                selectedRoomIds = state.selectedRoomIds,
                onRoomToggle = { roomId -> onIntent(ShareReceiverIntent.ToggleRoom(roomId)) },
                modifier = Modifier.weight(1f),
            )
        }
        // 시트는 화면 맨 아래에 붙으므로 시스템 바를 피하는 건 맨 밑 요소인 액션 영역 몫이다.
        // 컴포넌트도 시트도 인셋을 소비하지 않아 여기서 한 번만 얹힌다(`MinoActionArea` KDoc).
        RoomPickerActionArea(
            enabled = state.isSaveEnabled,
            onSaveClick = { onIntent(ShareReceiverIntent.Save) },
            modifier = Modifier.navigationBarsPadding(),
        )
    }
}

@UiModePreviews
@Composable
private fun ShareReceiverScreenPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        ShareReceiverScreen(
            state = ShareReceiverUiState(
                rooms = persistentListOf(
                    RoomPickerItem(
                        id = "personal",
                        name = "내 장소",
                        description = null,
                        placeCountLabel = stringResource(R.string.sharereceiver_room_place_count, 0),
                        thumbnailImageUrls = persistentListOf(),
                        color = null,
                    ),
                    RoomPickerItem(
                        id = "shared-1",
                        name = "민호야 잘하자",
                        description = null,
                        placeCountLabel = stringResource(R.string.sharereceiver_room_place_count, 9),
                        thumbnailImageUrls = persistentListOf(),
                        color = MinoRoomColor.Cyan,
                    ),
                    RoomPickerItem(
                        id = "shared-2",
                        name = "매쉬업 화이팅",
                        description = "팀원 모두가 좋아할 만한 술집 모음",
                        placeCountLabel = stringResource(R.string.sharereceiver_room_place_count, 2),
                        thumbnailImageUrls = persistentListOf(),
                        color = MinoRoomColor.Orange,
                    ),
                ),
                selectedRoomIds = persistentSetOf("shared-1"),
            ),
            onIntent = {},
            modifier = modifier,
        )
    }
}

@UiModePreviews
@Composable
private fun ShareReceiverScreenEmptyPreview(modifier: Modifier = Modifier) {
    MinoAndroidAppTheme {
        ShareReceiverScreen(
            state = ShareReceiverUiState(),
            onIntent = {},
            modifier = modifier,
        )
    }
}
