package team.mino.feature.roomform.form.screen

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import team.mino.core.common.ui.architecture.CollectSideEffect
import team.mino.core.common.ui.error.CollectDomainError
import team.mino.core.common.ui.scaffold.LocalSnackbarHostState
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.MinoDomainException
import team.mino.feature.roomform.R
import team.mino.feature.roomform.form.vm.RoomFormIntent
import team.mino.feature.roomform.form.vm.RoomFormOutcome
import team.mino.feature.roomform.form.vm.RoomFormSideEffect
import team.mino.feature.roomform.form.vm.RoomFormViewModel

/**
 * [RoomFormScreen]의 연결부. ViewModel을 얻어 상태를 구독하고 인텐트를 넘긴다.
 *
 * 폼이 끝났다는 신호는 콜백으로 올려보낸다 — 그 다음에 어디로 갈지는 진입점이 정하고
 * 이 화면은 무슨 일이 있었는지만 알린다.
 *
 * 제출 실패는 상태가 아니라 ViewModel 인스턴스별 채널로 오므로 셸이 아니라 여기서 수집해
 * 셸이 내려준 스낵바 호스트에 띄운다(에러 처리 규약 §5·§6).
 *
 * @param onFinish 폼 종료 신호. 결과를 Activity까지 올려 `setResult`·`finish`로 옮긴다.
 */
@Composable
internal fun RoomFormRoute(
    onFinish: (RoomFormOutcome) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoomFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current

    // 방 설명의 편집 버퍼는 화면이 아니라 여기가 든다. 상한을 자르는 것은 입력 컴포넌트라
    // 버퍼가 이미 잘린 값을 갖고 있고, 그 변화를 그대로 ViewModel로 옮긴다.
    val descriptionState = rememberTextFieldState()

    // 편집 초기값을 이미 받았는지. 버퍼와 같은 저장소에 실어 프로세스 사망을 함께 넘긴다 —
    // 이 표시가 복원되지 않으면 아래 주입이 복원된 버퍼를 조회값으로 덮어쓴다.
    var descriptionFilled by rememberSaveable { mutableStateOf(false) }

    // 지연 연산자를 끼우지 않는다 — 미리보기 카드가 입력과 같은 프레임에서 갱신되어야 한다.
    LaunchedEffect(descriptionState, viewModel) {
        snapshotFlow { descriptionState.text.toString() }
            .collect { viewModel.processIntent(RoomFormIntent.DescriptionChanged(it)) }
    }

    // 편집 진입 조회가 값을 들고 오면 버퍼와 상태 중 한쪽으로 맞춘다. 어느 쪽이 이기는지는
    // 이 버퍼가 초기값을 이미 받아 본 적 있는지로 갈린다.
    //
    // 처음이면 조회값이 이긴다 — 넣지 않으면 편집 폼의 설명이 빈 채로 열린다.
    // 이미 받아 본 버퍼가 조회를 다시 만나는 것은 프로세스가 죽었다 살아난 경우뿐이라 버퍼가 이긴다.
    // 복원된 것은 사용자가 쓰던 값이고, 조회값으로 덮으면 그것이 사라진다. 이때 버퍼를 상태로
    // 되돌려 두지 않으면 화면에 보이는 설명과 저장될 설명이 갈린다.
    // 재시도로 조회가 다시 성공해도 같은 갈래를 타 사용자가 고치던 값을 덮지 않는다.
    val loadedDescription = state.initial?.description
    LaunchedEffect(descriptionState, loadedDescription) {
        if (loadedDescription == null) return@LaunchedEffect
        if (descriptionFilled) {
            viewModel.processIntent(RoomFormIntent.DescriptionChanged(descriptionState.text.toString()))
        } else {
            descriptionFilled = true
            descriptionState.setTextAndPlaceCursorAtEnd(loadedDescription)
        }
    }

    // 시스템 뒤로가기를 항상 가로챈다. 세 갈래의 순서가 곧 계약이라 when의 위아래를 바꾸면 안 된다.
    //
    // 모달이 떠 있으면 [취소]와 같게 모달만 닫는다. 확인 모달은 자기 창을 띄우고 그 창이 뒤로가기를
    // 먼저 받아 `onDismiss`로 돌려주므로 이 갈래까지 오지 않는 것이 보통이지만, 두 경로가 함께
    // 불려도 닫는 일이 두 번 일어날 뿐 결과가 달라지지 않는다. 이 갈래를 빼면 창이 이벤트를 놓쳤을 때
    // 모달을 닫으려던 제스처가 폼 이탈로 이어진다.
    //
    // 온보딩은 켜 둔 채 아무 일도 하지 않아 제스처를 삼킨다 — 이 스텝을 벗어나는 수단은 [건너뛰기]
    // 하나이므로, 꺼 두면 기본 동작이 Activity를 닫아 이전 온보딩 스텝으로 되돌아간다.
    BackHandler {
        when {
            state.dialog != null -> viewModel.processIntent(RoomFormIntent.DialogDismissed)
            state.isOnboarding -> Unit
            else -> viewModel.processIntent(RoomFormIntent.BackClicked)
        }
    }

    CollectSideEffect(viewModel.sideEffect) { effect ->
        when (effect) {
            is RoomFormSideEffect.Finish -> onFinish(effect.outcome)
        }
    }

    // 에러 수집기가 받는 것은 ViewModel이 아니라 에러 방출자다. 그 자리에 ViewModel을 그대로 놓으면
    // 하위 Composable로 ViewModel을 흘려보내는 것과 구분되지 않으므로 넘길 능력만 남겨 타입을 좁힌다.
    val errorEmitter: DomainErrorEmitter = viewModel
    CollectDomainError(errorEmitter) { error ->
        scope.launch { snackbarHostState.showSnackbar(resources.getString(messageResOf(error))) }
    }

    RoomFormScreen(
        state = state,
        descriptionState = descriptionState,
        onIntent = viewModel::processIntent,
        modifier = modifier,
    )
}

/**
 * 저장 실패 문구. 리프를 구분하지 않고 한 줄로 안내한다 — 사용자가 할 수 있는 일이 재시도로 같아
 * 원인을 갈라 봐야 행동이 달라지지 않는다. 서버가 붙어 갈래별 대응이 생기면 그때 나눈다.
 *
 * `else`를 두지 않아 리프가 늘면 컴파일이 멈추고 여기서 다시 판단하게 된다.
 * 공통 매퍼를 두지 않는 이유는 에러 처리 규약 §8이 소유한다.
 */
@StringRes
private fun messageResOf(error: MinoDomainException): Int =
    when (error) {
        is MinoDomainException.Network,
        is MinoDomainException.Http,
        is MinoDomainException.Auth,
        -> R.string.roomform_error_save_failed
    }
