package team.mino.feature.placedetail.main.screen

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import team.mino.feature.placedetail.R
import team.mino.feature.placedetail.main.vm.PlaceDetailIntent
import team.mino.feature.placedetail.main.vm.PlaceDetailSideEffect
import team.mino.feature.placedetail.main.vm.PlaceDetailViewModel

/**
 * [PlaceDetailScreen]의 연결부. ViewModel을 얻어 상태를 구독하고 인텐트를 넘긴다.
 *
 * **화면 밖으로 나가는 일은 콜백으로 올려보낸다.** 끝내기·외부 지도·원문 링크는 모두 Activity가 실행하므로
 * 이 자리에서는 무슨 일이 일어나야 하는지만 알린다(`docs/architecture/feature-navigation.md` 1장).
 *
 * **오류 문구를 만들지 않는다.** 사용자 액션의 일회성 실패는 ViewModel이 위임한 방출자를 통해 오고, 여기서 셸이
 * 내려준 스낵바 호스트에 띄운다(`docs/conventions/error_handling.md` §5·§6). 주 데이터 조회 실패는 이 통로가
 * 아니라 상태로 와서 화면이 오류 화면으로 그린다.
 *
 * @param onExit 화면을 끝낸다. 끝낸 뒤 어디에 남을지는 진입점이 안다(spec FR-009).
 * @param onOpenExternalMap 외부 지도로 장소를 연다. 링크가 없으면 검색어로 대신 연다(spec FR-016).
 * @param onOpenSourceLink 장소의 원문 링크를 연다(spec FR-017).
 */
@Composable
internal fun PlaceDetailRoute(
    onExit: () -> Unit,
    onOpenExternalMap: (mapUrl: String?, query: String) -> Unit,
    onOpenSourceLink: (url: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current

    // 코멘트의 편집 버퍼는 화면이 아니라 여기가 든다. 상한을 자르는 것은 입력 컴포넌트라 버퍼가 이미 잘린 값을
    // 갖고 있고, 그 변화를 그대로 ViewModel로 옮긴다.
    val commentState = rememberTextFieldState()

    LaunchedEffect(commentState, viewModel) {
        snapshotFlow { commentState.text.toString() }
            .collect { viewModel.processIntent(PlaceDetailIntent.OnCommentDraftChange(it)) }
    }

    // 등록에 성공하면 ViewModel은 자기 초안만 비운다. 버퍼를 함께 비우지 않으면 올라간 코멘트가 입력창에 그대로
    // 남아 두 번 보낸 것처럼 보인다. 사용자가 지워서 비는 경우에는 버퍼가 이미 비어 있어 이 정리가 아무 일도 하지 않는다.
    val commentDraft = state.commentDraft
    LaunchedEffect(commentDraft) {
        if (commentDraft.isEmpty()) commentState.clearText()
    }

    // 시트가 떠 있으면 뒤로가기가 그 시트만 닫는다. 딤 바깥 탭·아래로 끌기와 같은 처리이며(spec EC-021),
    // 시트가 없을 때만 화면을 끝내는 경로로 간다.
    BackHandler {
        if (state.shareSheet != null) {
            viewModel.processIntent(PlaceDetailIntent.OnShareSheetDismiss)
        } else {
            viewModel.processIntent(PlaceDetailIntent.OnExitClick)
        }
    }

    CollectSideEffect(viewModel.sideEffect) { effect ->
        when (effect) {
            PlaceDetailSideEffect.Exit -> onExit()
            is PlaceDetailSideEffect.OpenExternalMap -> onOpenExternalMap(effect.mapUrl, effect.query)
            is PlaceDetailSideEffect.OpenSourceLink -> onOpenSourceLink(effect.url)
            PlaceDetailSideEffect.ShowShareCompleted ->
                scope.launch {
                    snackbarHostState.showSnackbar(resources.getString(R.string.placedetail_share_completed))
                }
        }
    }

    // 에러 수집기가 받는 것은 ViewModel이 아니라 에러 방출자다. 그 자리에 ViewModel을 그대로 놓으면
    // 하위 Composable로 ViewModel을 흘려보내는 것과 구분되지 않으므로 넘길 능력만 남겨 타입을 좁힌다.
    val errorEmitter: DomainErrorEmitter = viewModel
    CollectDomainError(errorEmitter) { error ->
        scope.launch { snackbarHostState.showSnackbar(resources.getString(messageResOf(error))) }
    }

    PlaceDetailScreen(
        state = state,
        commentState = commentState,
        onIntent = viewModel::processIntent,
        modifier = modifier,
    )
}

/**
 * 액션 실패의 스낵바 문구. 리프를 구분하지 않고 한 줄로 안내한다 — 재시도든 무엇이든 원인을 갈라 봐야
 * 사용자가 할 수 있는 일이 달라지지 않는다.
 *
 * `else`를 두지 않아 리프가 늘면 컴파일이 멈추고 여기서 다시 판단하게 된다.
 * 공통 매퍼를 두지 않는 이유는 `docs/conventions/error_handling.md` §8이 소유한다.
 */
@StringRes
private fun messageResOf(error: MinoDomainException): Int =
    when (error) {
        is MinoDomainException.Network,
        is MinoDomainException.Http,
        is MinoDomainException.Auth,
        -> R.string.placedetail_error_generic
    }
