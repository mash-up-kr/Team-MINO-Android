package team.mino.feature.room.placedetail.screen

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxScope
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
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_ROOM_ID
import team.mino.feature.room.placedetail.vm.PlaceDetailIntent
import team.mino.feature.room.placedetail.vm.PlaceDetailSideEffect
import team.mino.feature.room.placedetail.vm.PlaceDetailViewModel
import team.mino.feature.room.showShareCompleted

/**
 * [PlaceDetailScreen]의 연결부. ViewModel을 얻어 상태를 구독하고 인텐트를 넘긴다.
 *
 * 장소 상세는 자기 Navigation 목적지를 갖지 않고 `RoomListRoute`가 `selectedPinId != null`일 때
 * `RoomListScreen`의 슬롯으로 직접 호출한다 — [pinId]는 `PlaceDetailViewModel.Factory`에
 * `@AssistedInject`로 직접 넘긴다(`RoomDetailRoute`와 같은 형태).
 *
 * [BoxScope] 확장인 이유: 호출부가 지도(`RoomListMap`)를 담은 같은 `Box` 안에서 이 Route를 그려야 지도가
 * 리스트↔상세 전환에도 살아남는다 — 그 `Box`의 [BoxScope]를 그대로 받아 [PlaceDetailScreen]의 시트·컨트롤이
 * `Modifier.align`으로 위치를 잡을 수 있게 한다.
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
 * @param onCurrentLocationClick 현재 위치 버튼 클릭. 지도를 그리는 것이 이 화면이 아니라 호출부이므로
 *   [PlaceDetailViewModel]로 보내지 않고 그대로 올려보낸다 — 카메라를 실제로 움직이는 주체가 지도를 가진
 *   쪽 하나로 정해져야 버튼이 먹는다(`docs/specs/place-detail/research.md` D25).
 * @param onSwitchRoom 「지금 보고 있는 방」을 바꾼다(spec FR-025). 두 값을 함께 넘기는 것이 계약이다 —
 *   받는 쪽이 `pinId`·`roomId`를 한 번에 갈아 끼워야 마커 양식·코멘트·[나가기] 목적지가 서로 다른 방을
 *   가리키는 순간이 생기지 않는다(spec SC-009).
 */
@Composable
internal fun BoxScope.PlaceDetailRoute(
    pinId: String,
    onExit: () -> Unit,
    onOpenExternalMap: (mapUrl: String?, query: String) -> Unit,
    onOpenSourceLink: (url: String) -> Unit,
    onCurrentLocationClick: () -> Unit,
    onSwitchRoom: (pinId: String, roomId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PlaceDetailViewModel = hiltViewModel<PlaceDetailViewModel, PlaceDetailViewModel.Factory>(
        key = pinId,
        creationCallback = { factory -> factory.create(pinId) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current
    val activity = checkNotNull(LocalActivity.current) { "PlaceDetailRoute는 Activity 컨텍스트 안에서만 그려진다." }

    // 공유 시트의 [새 방 만들기] — 돌아온 결과에서 방 id만 받아 넘긴다(spec EC-020). 방 상세의
    // `shareCreateRoomResultLauncher`와 같은 계약이다
    // (docs/specs/group-room-form/contracts/room-form-launcher.md §3).
    val createRoomResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val createdRoomId = result.data?.getStringExtra(EXTRA_ROOM_FORM_RESULT_ROOM_ID)
        viewModel.processIntent(PlaceDetailIntent.OnShareRoomFormResult(createdRoomId))
    }

    // 코멘트의 편집 버퍼는 화면이 아니라 여기가 든다. 상한을 자르는 것은 입력 컴포넌트라 버퍼가 이미 잘린 값을
    // 갖고 있고, 그 변화를 그대로 ViewModel로 옮긴다.
    val commentState = rememberTextFieldState()

    LaunchedEffect(commentState, viewModel) {
        snapshotFlow { commentState.text.toString() }
            .collect { viewModel.processIntent(PlaceDetailIntent.OnCommentDraftChange(it)) }
    }

    // 등록에 성공하면 ViewModel은 자기 초안만 비운다. 버퍼를 함께 비우지 않으면 올라간 코멘트가 입력창에 그대로
    // 남아 두 번 보낸 것처럼 보인다. 사용자가 지워서 비는 경우에는 버퍼가 이미 비어 있어 이 정리가 아무 일도 하지 않는다.
    // 키를 초안 자체가 아니라 「비었는가」로 잡는다 — 글자마다 이펙트를 다시 세울 이유가 없고, 비고 차는
    // 순간에만 정리가 필요하다.
    val isCommentDraftEmpty = state.commentDraft.isEmpty()
    LaunchedEffect(isCommentDraftEmpty) {
        if (isCommentDraftEmpty) commentState.clearText()
    }

    // 시트가 떠 있으면 뒤로가기가 그 시트만 닫는다. 딤 바깥 탭·아래로 끌기와 같은 처리이며(spec EC-021 · EC-025),
    // 시트가 없을 때만 화면을 끝내는 경로로 간다. 두 시트는 함께 떠 있지 않으므로 순서가 판정을 바꾸지 않는다.
    BackHandler {
        val dismissIntent = when {
            state.shareSheet != null -> PlaceDetailIntent.OnShareSheetDismiss
            state.savedRoomsSheet != null -> PlaceDetailIntent.OnSavedRoomsSheetDismiss
            else -> PlaceDetailIntent.OnExitClick
        }
        viewModel.processIntent(dismissIntent)
    }

    CollectSideEffect(viewModel.sideEffect) { effect ->
        when (effect) {
            PlaceDetailSideEffect.Exit -> onExit()
            is PlaceDetailSideEffect.OpenExternalMap -> onOpenExternalMap(effect.mapUrl, effect.query)
            is PlaceDetailSideEffect.OpenSourceLink -> onOpenSourceLink(effect.url)
            // 문구와 3초 노출은 방 상세와 한 곳에서 나온다(spec TS-033, `ShareCompletedToast.kt`).
            PlaceDetailSideEffect.ShowShareCompleted ->
                scope.launch { snackbarHostState.showShareCompleted(resources) }

            PlaceDetailSideEffect.OpenCreateRoomForm ->
                viewModel.roomFormLauncher.launch(activity, resultLauncher = createRoomResultLauncher)

            // 방을 바꾸는 것은 이 화면의 상태 변경이 아니라 위쪽 화면의 것이다 — 새 pinId가 내려오면 이 Route가
            // 그 key로 ViewModel을 새로 세우고, 그 결과로 코멘트 초안·캐러셀·시트 단계가 초기화된다(spec TS-047).
            is PlaceDetailSideEffect.SwitchRoom -> onSwitchRoom(effect.pinId, effect.roomId)
        }
    }

    // 에러 수집기가 받는 것은 ViewModel이 아니라 에러 방출자다. 그 자리에 ViewModel을 그대로 놓으면
    // 하위 Composable로 ViewModel을 흘려보내는 것과 구분되지 않으므로 넘길 능력만 남겨 타입을 좁힌다.
    val errorEmitter: DomainErrorEmitter = viewModel
    CollectDomainError(errorEmitter) { error ->
        scope.launch { snackbarHostState.showSnackbar(resources.getString(placeDetailErrorMessageRes(error))) }
    }

    PlaceDetailScreen(
        state = state,
        commentState = commentState,
        onIntent = viewModel::processIntent,
        onCurrentLocationClick = onCurrentLocationClick,
        modifier = modifier,
    )
}
