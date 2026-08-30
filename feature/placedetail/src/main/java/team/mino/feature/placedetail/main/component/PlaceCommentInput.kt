package team.mino.feature.placedetail.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.button.MinoButton
import team.mino.core.designsystem.component.textinput.MinoTextArea
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.placedetail.R

/**
 * 코멘트를 쓰고 올리는 자리. 입력창과 [등록]이 한 묶음이며 목록의 마지막 코멘트 아래에 놓인다(spec EC-015).
 *
 * **화면 하단에 고정되지 않는다.** 시트 콘텐츠와 함께 스크롤되므로 뜨는 배경을 여기서 그리지 않는다.
 * 제스처 내비게이션 바를 피하는 하단 인셋도 마찬가지다 — 그 인셋은 화면 바닥에 닿는 시트 콘텐츠의 바닥이
 * 소유한다(`PlaceDetailScreen`). 이 묶음이 들면 놓이는 자리마다 여백을 데리고 다니게 된다.
 *
 * **200자를 넘겨 받지 않는다.** 201자째를 거르고 카운터를 `200/200`으로 세우는 것은 [MinoTextArea]이므로
 * (spec FR-012·EC-011) 이 자리에서 다시 자르지 않는다. 카운터의 `N/200` 표기와 실시간 갱신도 같은 컴포넌트가
 * 준다(spec UX-006·TS-020).
 *
 * **[등록]의 활성 조건을 여기서 판정하지 않는다.** 공백만 남은 입력을 막는 것은 `PlaceDetailUiState.isSubmitEnabled`
 * 하나뿐이라(spec FR-013·EC-012) 이 컴포저블은 그 결과만 받는다 — 두 곳에서 판정하면 버튼의 겉모습과 실제 처리가
 * 갈린다.
 *
 * **가로 여백을 스스로 갖지 않는다.** 입력창과 [등록]이 `친구들의 코멘트` 영역과 같은 여백선에 서므로 호출부가
 * 섹션에 준 가로 패딩을 그대로 물려받는다.
 *
 * @param state 입력 버퍼. 소유자는 호출부(Route)이며, 등록이 성공해 `PlaceDetailUiState.commentDraft`가 비면
 *   버퍼도 함께 비우는 것 역시 그 소유자의 몫이다 — 이 컴포저블은 버퍼를 읽고 그리기만 한다.
 * @param isSubmitEnabled [등록]의 활성 여부. 전송이 도는 동안에도 꺼져 같은 코멘트가 두 번 올라가지 않는다.
 * @param onSubmitClick [등록]. 눌린 순간의 본문은 ViewModel이 이미 들고 있으므로 여기서 실어 보내지 않는다.
 */
@Composable
internal fun PlaceCommentInput(
    state: TextFieldState,
    isSubmitEnabled: Boolean,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        MinoTextArea(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(R.string.placedetail_comment_input_placeholder),
            maxLength = COMMENT_MAX_LENGTH,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = ActionVerticalPadding),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MinoButton(
                text = stringResource(R.string.placedetail_comment_submit),
                onClick = onSubmitClick,
                enabled = isSubmitEnabled,
            )
        }
    }
}

/** spec FR-012의 코멘트 상한. 세는 것도 넘는 입력을 막는 것도 [MinoTextArea]가 한다. */
private const val COMMENT_MAX_LENGTH = 200

// Figma Margin/Action/Normal Vertical 변수 대응 — 토큰 미존재
private val ActionVerticalPadding = 20.dp

@UiModePreviews
@Composable
private fun PlaceCommentInputPreview() {
    MinoAndroidAppTheme {
        PlaceCommentInput(
            state = rememberTextFieldState(),
            isSubmitEnabled = false,
            onSubmitClick = {},
        )
    }
}

/** 쓰는 중 — 카운터가 따라 오르고 [등록]이 열린다. */
@UiModePreviews
@Composable
private fun PlaceCommentInputFilledPreview() {
    MinoAndroidAppTheme {
        PlaceCommentInput(
            state = rememberTextFieldState("여기 분위기 정말 좋았어요."),
            isSubmitEnabled = true,
            onSubmitClick = {},
        )
    }
}
