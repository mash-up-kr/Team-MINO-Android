package team.mino.feature.roomform.form.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomNameValidation
import team.mino.feature.roomform.form.model.RoomFormMode
import team.mino.feature.roomform.form.vm.RoomFormUiState
import team.mino.feature.roomform.form.vm.RoomFormValues

/*
 * RoomFormScreen의 상태별 렌더 프리뷰.
 *
 * 화면은 stateless라 상태를 직접 만들어 넘긴다. CTA 활성 여부는 RoomFormUiState가 이름 판정에서
 * 계산하므로 여기서는 그 계산을 흉내 내지 않고 입력값과 판정만 짝을 맞춰 둔다.
 *
 * 방 설명은 화면 밖(Route)이 소유하는 편집 버퍼라, 프리뷰도 같은 자리에서 만들어 넘긴다.
 */

/** 생성으로 막 들어온 폼. 세 입력이 모두 비었고 CTA가 잠겨 있되, 이름 필드는 오류로 그려지지 않는다. */
@UiModePreviews
@Composable
private fun RoomFormScreenEmptyPreview() {
    RoomFormScreenPreviewContainer(RoomFormUiState())
}

/** 이름·설명·색을 모두 채운 폼. 이름이 유효해져 CTA가 열린다. */
@UiModePreviews
@Composable
private fun RoomFormScreenFilledPreview() {
    RoomFormScreenPreviewContainer(
        RoomFormUiState(
            values = FILLED_VALUES,
            nameValidation = RoomNameValidation.Valid,
        ),
    )
}

/**
 * 기존 방을 열어 편집하는 폼. 상단 타이틀과 CTA 라벨이 생성과 갈린다.
 *
 * 진입 스냅샷이 현재 값과 같아 아직 변경이 없는 시점이다.
 */
@UiModePreviews
@Composable
private fun RoomFormScreenEditPreview() {
    RoomFormScreenPreviewContainer(
        RoomFormUiState(
            mode = RoomFormMode.Edit(roomId = EDIT_ROOM_ID),
            values = EDIT_VALUES,
            initial = EDIT_VALUES,
            nameValidation = RoomNameValidation.Valid,
        ),
    )
}

@Composable
private fun RoomFormScreenPreviewContainer(
    state: RoomFormUiState,
    modifier: Modifier = Modifier,
) {
    MinoAndroidAppTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MinoAndroidTheme.colors.backgroundNormalNormal),
        ) {
            RoomFormScreen(
                state = state,
                descriptionState = rememberTextFieldState(state.values.description),
                onIntent = {},
            )
        }
    }
}

private val FILLED_VALUES = RoomFormValues(
    name = "민호야 잘하자",
    description = "팀 회식 장소 모음",
    color = RoomColor.CYAN,
)

// 편집 장은 값을 달리 준다. 채운 폼과 같은 그림이 두 번 나오면 무엇이 달라졌는지 보이지 않는다.
private val EDIT_VALUES = RoomFormValues(
    name = "주말 산책 코스",
    description = "걷기 좋은 길만 모아요",
    color = RoomColor.LIME,
)

private const val EDIT_ROOM_ID = "room-1"
