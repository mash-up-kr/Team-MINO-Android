package team.mino.feature.room.placedetail.vm

import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentSet
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.core.common.android.extension.launchSafely
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.repository.PlaceCommentRepository
import team.mino.core.domain.repository.PlaceRepository
import team.mino.core.domain.usecase.GetRoomPickerRoomsUseCase
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.domainErrorEmitter
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import team.mino.feature.room.placedetail.model.PlaceCommentUiModel
import team.mino.feature.room.placedetail.model.PlaceHeaderMode
import team.mino.feature.room.placedetail.model.RoomPickerItem
import team.mino.feature.room.placedetail.model.toUiModel
import team.mino.feature.room.placedetail.model.toUiModels
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * 장소 상세 화면의 ViewModel.
 *
 * 진입 인자는 `pinId` 하나이고 화면으로 드릴링하지 않는다 — 상태의 초기값으로 넣고, 이후 모든 서버 호출이
 * 그 값을 키로 쓴다.
 *
 * 이 화면은 자기 Navigation 목적지를 갖지 않고 방 목록 화면 안에서 선택된 핀으로 전환되므로
 * `SavedStateHandle.toRoute()`가 성립하지 않는다 — `pinId`는 진입점이 `@AssistedInject`로 직접 넘겨준다
 * (`RoomDetailViewModel`과 같은 형태).
 *
 * 실패는 성격에 따라 통로가 갈린다(`docs/conventions/error_handling.md` §5). 화면이 그릴 것 자체를 못 받은
 * 주 데이터 조회 둘(핀 상세·최신 코멘트)의 실패는 [PlaceDetailUiState.loadError]에 담아 화면 전체를 재시도
 * 가능한 오류로 바꾸고, 코멘트 등록·삭제·다른 방 공유처럼 사용자가 일으킨 일회성 실패는 [DomainErrorEmitter]로
 * 방출해 `PlaceDetailRoute`가 스낵바로 표시한다.
 *
 * **이전 페이지 추가 로드와 방 목록 조회는 후자로 분류한다.** §5가 「첫 적용 화면에서 결정한다」로 열어 둔
 * 경계 사례인데, 둘 다 본문이 이미 그려진 뒤의 실패라 화면을 통째로 오류로 바꾸면 읽고 있던 것까지 사라진다 —
 * 사용자가 잃는 것 없이 같은 자리에서 다시 시도할 수 있으므로 알림 하나로 끝낸다.
 *
 * 어느 통로든 문구는 만들지 않는다. 리프만 넘기고 문구 매핑은 화면·Route가 한다.
 *
 * 「경과일 초기화 확인」만 두 통로 어느 쪽도 타지 않는다. [PlaceRepository.recordAccess]가 실패를 삼키는
 * 계약이라 여기서 결과를 볼 것이 없다(spec EC-022).
 *
 * @param clock 코멘트 표기의 기준 시각을 얻는 곳. 목록 상태를 다시 만들 때마다 여기서 「지금」을 읽어
 *  [PlaceDetailUiState.commentsObservedAt]에 함께 싣는다(§6.1) — 화면이 컴포지션마다 시각을 읽으면
 *  spec EC-028이 지켜지는지 확인할 수단이 없어진다.
 */
@OptIn(ExperimentalTime::class)
@HiltViewModel(assistedFactory = PlaceDetailViewModel.Factory::class)
internal class PlaceDetailViewModel @AssistedInject constructor(
    @Assisted private val pinId: String,
    private val clock: Clock,
    private val placeRepository: PlaceRepository,
    private val placeCommentRepository: PlaceCommentRepository,
    private val getRoomPickerRooms: GetRoomPickerRoomsUseCase,
) : ViewModel(),
    MviContainer<PlaceDetailUiState, PlaceDetailSideEffect> by mviContainer(
        PlaceDetailUiState(pinId = pinId),
    ),
    DomainErrorEmitter by domainErrorEmitter() {
    @AssistedFactory
    internal interface Factory {
        fun create(pinId: String): PlaceDetailViewModel
    }

    /**
     * 세 갈래를 따로 띄운다.
     *
     * 기록은 나머지 둘의 성패와 무관하고, 핀 상세와 코멘트는 서로를 기다릴 이유가 없다. 한 코루틴에 묶어
     * 순차로 부르면 가장 느린 응답이 나머지의 렌더를 붙잡는다
     * (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §5).
     *
     * 방 목록만 이 자리에 없다. 질의 키를 핀 상세 응답이 쥐고 있어 [loadPlace]가 이어서 부른다.
     */
    init {
        recordAccess()
        loadPlace()
        loadLatestComments()
    }

    fun processIntent(intent: PlaceDetailIntent) {
        when (intent) {
            is PlaceDetailIntent.OnSheetLevelChange -> updateState { copy(sheetLevel = intent.level) }
            is PlaceDetailIntent.OnHeaderExpansionChange -> changeHeaderMode(intent.isExpanded)
            PlaceDetailIntent.OnExitClick -> exit()
            is PlaceDetailIntent.OnCarouselPageChange -> updateState { copy(carouselPage = intent.page) }
            PlaceDetailIntent.OnOpenMapClick -> openExternalMap()
            PlaceDetailIntent.OnOpenSourceClick -> openSourceLink()
            is PlaceDetailIntent.OnCommentDraftChange -> updateState { copy(commentDraft = intent.value) }
            PlaceDetailIntent.OnSubmitCommentClick -> submitComment()
            is PlaceDetailIntent.OnDeleteCommentClick -> deleteComment(intent.commentId)
            PlaceDetailIntent.OnLoadOlderComments -> loadOlderComments()
            PlaceDetailIntent.OnShareClick -> openShareSheet()
            is PlaceDetailIntent.OnShareRoomToggle -> toggleShareRoom(intent.roomId)
            PlaceDetailIntent.OnShareConfirmClick -> confirmShare()
            PlaceDetailIntent.OnShareSheetDismiss -> updateState { copy(shareSheet = null) }
            PlaceDetailIntent.OnSavedRoomsClick -> openSavedRoomsSheet()
            is PlaceDetailIntent.OnSavedRoomSelected -> switchRoom(intent.pinId, intent.roomId)
            PlaceDetailIntent.OnSavedRoomsSheetDismiss -> updateState { copy(savedRoomsSheet = null) }
            PlaceDetailIntent.OnRetryLoadClick -> retryLoad()
        }
    }

    /**
     * 열었다는 사실만 던지고 잊는다.
     *
     * 결과를 기다리지 않으므로 다른 조회가 이 왕복만큼 늦어지지 않고, 상태를 건드리지 않으므로 실패해도
     * 화면에 흔적이 남지 않는다(spec EC-022). 잡지 않는 것은 계약이 이미 삼킨 뒤라서다 —
     * 여기서 `try`를 두면 그 계약이 호출부로 새어 나온다.
     *
     * 디바운스·중복 제거를 두지 않는다. 짧은 간격의 재진입도 그대로 한 건씩 쌓여야 한다(spec EC-023).
     */
    private fun recordAccess() {
        launchSafely { placeRepository.recordAccess(pinId) }
    }

    /**
     * 오류 화면의 [다시 시도]. 오류를 거두고 주 데이터 셋을 처음부터 다시 태운다.
     *
     * 이미 도착한 조회까지 되풀이한다. 둘 중 하나만 깨져도 화면 전체가 오류로 덮여 남은 하나의 결과는
     * 사용자에게 닿지 못했으므로, 무엇이 성공했는지를 따로 기억해 두었다가 골라 부를 이유가 없다.
     *
     * 방 목록을 여기서 따로 부르지 않는다. [loadPlace]에 매달려 있어 핀 상세와 함께 되살아난다.
     *
     * 오류를 먼저 거두는 것이 재클릭도 함께 막는다 — 버튼이 선 자리가 그 순간 사라진다.
     *
     * [recordAccess]는 다시 부르지 않는다. 열었다는 사실은 진입 때 이미 한 건 올라갔고, 재시도는
     * 새로운 진입이 아니다(spec EC-023).
     */
    private fun retryLoad() {
        updateState { copy(loadError = null) }
        loadPlace()
        loadLatestComments()
    }

    /**
     * 핀 상세를 받고, 그 응답이 준 `placeId`로 방 목록 조회를 이어 붙인다.
     *
     * 둘을 잇는 것은 순서가 필요해서가 아니라 **질의 키가 여기서 나오기 때문이다.** 방마다 이 장소가 이미
     * 있는지를 묻는 키는 `pinId`가 아니라 `placeId`다(`docs/specs/place-detail/contracts/place-api.md` §1.1).
     *
     * 상태를 먼저 올리고 부른다. 본문은 핀 상세만으로 그려지므로 뒤이은 조회가 그 렌더를 붙잡지 않는다.
     */
    private fun loadPlace() {
        launchSafely {
            runCatchingDomain { placeRepository.getPlaceDetail(pinId) }
                .onSuccess { place ->
                    updateState { copy(place = place) }
                    loadSavedRooms(place.placeId)
                }.onDomainFailure { error -> updateState { copy(loadError = error) } }
        }
    }

    /**
     * 최신 페이지를 받아 목록의 바닥으로 삼는다.
     *
     * 서버가 역방향 페이징이라 `page 0`이 가장 최신이고, 위로 올릴수록 더 오래된 페이지가 온다
     * (`docs/specs/place-detail/research.md` D11). 페이지 안의 순서는 뒤집지 않는다 — 이미 오래된 것이 먼저다.
     */
    private fun loadLatestComments() {
        launchSafely {
            runCatchingDomain { placeCommentRepository.getComments(pinId, LATEST_COMMENT_PAGE) }
                .onSuccess { page ->
                    val observedAt = clock.now()
                    updateState {
                        copy(
                            comments = page.comments.toUiModels(),
                            commentsObservedAt = observedAt,
                            commentPage = page.page,
                            hasOlderComments = page.hasOlder,
                        )
                    }
                }.onDomainFailure { error -> updateState { copy(loadError = error) } }
        }
    }

    /**
     * 방 목록을 이 장소의 저장 여부와 함께 받아 [PlaceDetailUiState.savedRooms]에 싣는다.
     *
     * **한 번만 부른다.** 같은 결과를 공유 시트의 이미 저장된 방 표시(spec FR-018·FR-022)와 [저장된 방]
     * 버튼·시트가 나눠 쓴다(`docs/specs/place-detail/contracts/place-detail-main-contract.md` §3.1).
     *
     * 실패를 [PlaceDetailUiState.loadError]로 보내지 않는다. 핀 상세가 도착한 뒤에 붙는 조회라 그때는 본문이
     * 이미 그려져 있고, 부가 기능인 공유 시트가 깨졌다고 읽고 있던 장소와 코멘트까지 걷어 갈 수는 없다.
     * 알림 하나로 끝내고 목록은 비운 채로 둔다.
     *
     * 정렬하지 않는다. 개인방 최상단 고정은 [GetRoomPickerRoomsUseCase]의 몫이다.
     */
    private suspend fun loadSavedRooms(placeId: String) {
        runCatchingDomain { getRoomPickerRooms(placeId) }
            .onSuccess { loaded -> updateState { copy(savedRooms = loaded.toPickerItems()) } }
            .onDomainFailure(::emitDomainError)
    }

    /**
     * 화면이 판정한 헤더 밀도를 그대로 싣는다.
     *
     * **판정을 여기서 하지 않는다.** 언제 접히는지는 스크롤 위치와 두 헤더의 높이를 함께 재야 나오는 값이라
     * 그것을 가진 화면이 판정한다(spec FR-008 · EC-007, `docs/specs/place-detail/research.md` D5).
     * 시트 단계는 어느 쪽에서도 보지 않는다 — `Full`이어도 최상단이면 확장형이다.
     */
    private fun changeHeaderMode(isExpanded: Boolean) {
        updateState {
            copy(headerMode = if (isExpanded) PlaceHeaderMode.EXPANDED else PlaceHeaderMode.COLLAPSED)
        }
    }

    /** 화면을 끝낸다. 어디로 돌아갈지는 진입점이 안다(spec FR-009). */
    private fun exit() {
        launchSafely { postSideEffect(PlaceDetailSideEffect.Exit) }
    }

    /**
     * 아직 장소가 도착하지 않았으면 열 곳이 없다. 버튼이 그 전에는 그려지지 않으므로 도달하지 않는 분기다.
     *
     * 어느 앱으로 열지 고르지 않는다 — 링크와 검색어만 실어 보내고 판단은 받는 쪽의 몫이다(spec FR-016).
     */
    private fun openExternalMap() {
        val place = state.value.place ?: return
        launchSafely { postSideEffect(PlaceDetailSideEffect.OpenExternalMap(place.mapUrl, place.name)) }
    }

    /** 링크 없는 장소에서는 [원문보기]가 비활성이라(spec EC-017) 여기까지 오지 않는다. */
    private fun openSourceLink() {
        val url = state.value.place?.sourceUrl ?: return
        launchSafely { postSideEffect(PlaceDetailSideEffect.OpenSourceLink(url)) }
    }

    /**
     * 등록된 코멘트를 **목록 맨 아래에 덧붙인다.** 목록을 다시 조회하지 않는 것은 그 사이 들어온 페이지
     * 경계가 흔들려 읽던 자리가 튀는 것을 막기 위해서다(spec FR-014 · UX-007).
     *
     * 입력은 성공했을 때만 비운다. 실패한 전송이 사용자가 쓴 글까지 가져가면 다시 칠 수밖에 없다.
     *
     * 앞뒤 공백을 여기서 다듬지 않는다 — `PlaceCommentRepository.addComment`가 서버의 몫이라고 못박았다.
     *
     * 기준 시각을 함께 새로 읽어야 방금 올린 코멘트가 `방금`으로 뜬다(spec TS-054). 오래된 기준으로 판정하면
     * 등록 직후인데도 이전 목록을 그릴 때의 경과가 그대로 남는다.
     */
    private fun submitComment() {
        val current = state.value
        if (!current.isSubmitEnabled) return
        val content = current.commentDraft
        updateState { copy(isSubmittingComment = true) }
        launchSafely {
            runCatchingDomain { placeCommentRepository.addComment(pinId, content) }
                .onSuccess { created ->
                    val observedAt = clock.now()
                    updateState {
                        copy(
                            comments = (comments + created.toUiModel()).toImmutableList(),
                            commentsObservedAt = observedAt,
                            commentDraft = "",
                        )
                    }
                }.onDomainFailure(::emitDomainError)
            updateState { copy(isSubmittingComment = false) }
        }
    }

    /**
     * 확인 절차 없이 곧바로 지운다(spec FR-015). 되돌리기 수단이 없다는 것도 spec EC-013의 결정이다.
     *
     * 지운 뒤 0건이 되면 빈 상태가 되는 것은 목록이 비는 것으로 이미 성립한다(spec EC-014) — 별도 플래그를
     * 두면 목록과 그 플래그가 갈릴 수 있다.
     *
     * [PlaceCommentUiModel.canDelete]가 `false`인 항목에는 [⋮]가 붙지 않아 여기까지 오지 않는다.
     * 권한을 다시 판정하지 않는다(`docs/specs/place-detail/research.md` D6).
     */
    private fun deleteComment(commentId: String) {
        launchSafely {
            runCatchingDomain { placeCommentRepository.deleteComment(pinId, commentId) }
                .onSuccess {
                    val observedAt = clock.now()
                    updateState {
                        copy(
                            comments = comments.filterNot { it.id == commentId }.toImmutableList(),
                            commentsObservedAt = observedAt,
                        )
                    }
                }.onDomainFailure(::emitDomainError)
        }
    }

    /**
     * 받은 페이지를 목록 **앞**에 붙인다. 서버의 페이지 번호가 커질수록 더 오래된 코멘트이고 화면은 오래된
     * 것이 위이므로, 방향이 반대인 두 축이 여기서 맞춰진다(`docs/specs/place-detail/research.md` D11).
     *
     * 더 받을 것이 없거나 이미 받는 중이면 물러난다 — 최상단에 닿았다는 신호는 스크롤이 멈춰 있어도
     * 거듭 들어온다.
     *
     * 실패해도 [PlaceDetailUiState.commentPage]가 그대로라 같은 페이지를 다시 시도할 수 있다.
     */
    private fun loadOlderComments() {
        val current = state.value
        if (!current.hasOlderComments || current.isLoadingOlderComments) return
        val olderPage = current.commentPage + 1
        updateState { copy(isLoadingOlderComments = true) }
        launchSafely {
            runCatchingDomain { placeCommentRepository.getComments(pinId, olderPage) }
                .onSuccess { page ->
                    val observedAt = clock.now()
                    updateState {
                        copy(
                            comments = (page.comments.toUiModels() + comments).toImmutableList(),
                            commentsObservedAt = observedAt,
                            commentPage = page.page,
                            hasOlderComments = page.hasOlder,
                        )
                    }
                }.onDomainFailure(::emitDomainError)
            updateState { copy(isLoadingOlderComments = false) }
        }
    }

    /**
     * 시트를 연다. 열면서 방 목록을 다시 조회하지 않는다 — 진입 때 받아 둔 [PlaceDetailUiState.savedRooms]를
     * 그대로 쓰므로 시트가 빈 채로 떴다가 채워지는 구간이 없다.
     *
     * 선택은 열 때마다 비운 채로 시작한다. 닫으면 상태째 사라지므로(`shareSheet = null`) 지난번에 고른
     * 방이 되살아나지 않는다.
     */
    private fun openShareSheet() {
        updateState { copy(shareSheet = ShareSheetUiState(rooms = savedRooms)) }
    }

    /**
     * 같은 방을 다시 누르면 선택이 풀린다. 선택을 시트 상태 한 곳에만 두어 목록이 다시 그려져도 흩어지지 않는다.
     *
     * 이미 저장된 방을 여기서 걸러 내지 않는다. 그 카드는 체크된 채 비활성이라 눌리지 않는다는 것이
     * 계약의 전제다(spec FR-018 · FR-022).
     */
    private fun toggleShareRoom(roomId: String) {
        updateState {
            val sheet = shareSheet ?: return@updateState this
            val selected = sheet.selectedRoomIds.toPersistentSet()
            copy(
                shareSheet =
                    sheet.copy(
                        selectedRoomIds =
                            if (roomId in selected) selected.remove(roomId) else selected.add(roomId),
                    ),
            )
        }
    }

    /**
     * 고른 방 전부를 한 번의 요청으로 보낸다. 성공하면 시트를 닫고 알림만 남긴다 — 공유한 방으로 따라가지
     * 않고 이 장소 상세가 시트 단계 그대로 남는 것이 spec FR-018·TS-033의 규정이다.
     *
     * 실패는 시트를 닫지 않는다. 다시 누를 수 있게 잠금만 풀고, 고른 방은 그대로 둔다.
     */
    private fun confirmShare() {
        val sheet = state.value.shareSheet ?: return
        if (!sheet.isShareEnabled) return
        val roomIds = sheet.selectedRoomIds.toList()
        updateState { copy(shareSheet = shareSheet?.copy(isSubmitting = true)) }
        launchSafely {
            runCatchingDomain { placeRepository.duplicatePin(pinId, roomIds) }
                .onSuccess {
                    updateState { copy(shareSheet = null) }
                    postSideEffect(PlaceDetailSideEffect.ShowShareCompleted)
                }.onDomainFailure { error ->
                    updateState { copy(shareSheet = shareSheet?.copy(isSubmitting = false)) }
                    emitDomainError(error)
                }
        }
    }

    /**
     * [저장된 방] 시트를 연다. 공유 시트와 같이 진입 때 받아 둔 [PlaceDetailUiState.savedRooms]를 그대로 쓰고
     * 다시 조회하지 않는다.
     *
     * **지금 보고 있는 방을 여기서 뺀다**(spec FR-024 · TS-042 · EC-026). 그리는 쪽이 아니라 목록을 만드는
     * 이 자리에서 빼는 것이 계약의 규정이며
     * (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §3.3), 시트가 든 목록이 곧
     * 「옮겨 갈 수 있는 방」과 같아진다(spec UX-012).
     *
     * 거르는 기준이 `hasPlace`가 아니라 [RoomPickerItem.matchedPinId]인 것은 두 가지를 한 번에 걸러 내기
     * 때문이다 — 저장돼 있지 않은 방은 그 값이 `null`이고(도메인에서 둘은 같은 뜻이다), 지금 보고 있는 핀을
     * 가진 방은 그 값이 [pinId]와 같다. 전환 대상이 없는 카드가 시트에 서지 않는 것도 같은 판정으로 성립한다.
     *
     * 버튼이 비활성인 동안에는 눌리지 않지만(spec EC-024) 판정을 다시 확인한다 — 활성 여부를 아는 곳과
     * 시트를 여는 곳이 갈라져 있어서다.
     */
    private fun openSavedRoomsSheet() {
        val current = state.value
        if (!current.isSavedRoomsVisible) return
        val rooms = current.savedRooms.filter { it.matchedPinId != null && it.matchedPinId != pinId }
        updateState {
            copy(savedRoomsSheet = SavedRoomsSheetUiState(rooms = rooms.toImmutableList()))
        }
    }

    /**
     * 고른 방으로 옮겨 간다. 시트를 닫고 전환을 위로 올린다(spec FR-025 · TS-043).
     *
     * 이 ViewModel이 갱신할 것은 시트를 닫는 것뿐이다. 화면이 새 핀으로 다시 서는 것은 방을 쥔 쪽이
     * [PlaceDetailSideEffect.SwitchRoom]을 받아 처리하며, 코멘트 초안·캐러셀·시트 단계가 초기화되는 것도
     * (spec TS-047) 그 결과로 이 ViewModel이 새로 만들어지면서 자연히 성립한다 — 여기서 필드를 하나씩
     * 되돌리지 않는다.
     */
    private fun switchRoom(
        targetPinId: String,
        roomId: String,
    ) {
        updateState { copy(savedRoomsSheet = null) }
        launchSafely { postSideEffect(PlaceDetailSideEffect.SwitchRoom(targetPinId, roomId)) }
    }

    private fun List<RoomSummary>.toPickerItems(): ImmutableList<RoomPickerItem> =
        map { it.toPickerItem() }.toImmutableList()

    /**
     * `hasPlace`의 `null`을 `false`로 접는다.
     *
     * 도메인에서 `null`은 「저장돼 있지 않다」가 아니라 「물어보지 않았다」이지만([RoomSummary.hasPlace]),
     * 이 목록은 언제나 `placeId`를 실어 물은 결과라 그 값이 나올 자리가 없다. 카드는 체크 여부 하나만 그리므로
     * (spec FR-018 · FR-022) 세 값을 그대로 나르지 않고 여기서 둘로 접는다.
     *
     * [RoomSummary.matchedPinId]는 그대로 옮긴다. 저장돼 있지 않은 방에서 그 값을 지우는 것은 이미
     * Mapper가 한 일이라(`docs/specs/place-detail/contracts/place-api.md` §4.2) 여기서 다시 막지 않는다.
     *
     * 설명이 없는 방은 빈 문자열로 오지만 카드는 `null`로 접는다 — 두 표현의 경계가 여기다.
     */
    private fun RoomSummary.toPickerItem(): RoomPickerItem =
        RoomPickerItem(
            id = id,
            name = name,
            description = description.takeIf { it.isNotBlank() },
            placeCount = placeCount,
            thumbnailImageUrls = thumbnailImageUrls.toImmutableList(),
            color = color,
            hasPlace = hasPlace == true,
            matchedPinId = matchedPinId,
        )

    private companion object {
        /** 서버가 역방향 페이징이라 0이 가장 최신 페이지다. */
        const val LATEST_COMMENT_PAGE = 0
    }
}
