package team.mino.feature.placedetail.main.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
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
import team.mino.core.domain.usecase.ResolvePlaceRoomColorUseCase
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.domainErrorEmitter
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import team.mino.feature.placedetail.PlaceDetailMain
import team.mino.feature.placedetail.main.model.PlaceCommentUiModel
import team.mino.feature.placedetail.main.model.PlaceHeaderMode
import team.mino.feature.placedetail.main.model.RoomPickerItem
import team.mino.feature.placedetail.main.model.toUiModel
import team.mino.feature.placedetail.main.model.toUiModels
import javax.inject.Inject

/**
 * 장소 상세 화면의 ViewModel.
 *
 * 진입 인자는 `pinId` 하나이고 화면으로 드릴링하지 않는다 — 라우트에서 복원해 상태의 초기값으로 넣고,
 * 이후 모든 서버 호출이 그 값을 키로 쓴다.
 *
 * 실패는 성격에 따라 통로가 갈린다(`docs/conventions/error_handling.md` §5). 화면이 그릴 것 자체를 못 받은
 * 주 데이터 조회 셋(핀 상세·최신 코멘트·방 목록)의 실패는 [PlaceDetailUiState.loadError]에 담아 화면 전체를
 * 재시도 가능한 오류로 바꾸고, 코멘트 등록·삭제·다른 방 공유처럼 사용자가 일으킨 일회성 실패는
 * [DomainErrorEmitter]로 방출해 `PlaceDetailRoute`가 스낵바로 표시한다.
 *
 * **이전 페이지 추가 로드는 후자로 분류한다.** §5가 「첫 적용 화면에서 결정한다」로 열어 둔 경계 사례인데,
 * 목록이 이미 그려진 뒤의 실패라 화면을 통째로 오류로 바꾸면 읽고 있던 코멘트까지 사라진다 — 사용자가 잃는
 * 것 없이 같은 자리에서 다시 시도할 수 있으므로 알림 하나로 끝낸다.
 *
 * 어느 통로든 문구는 만들지 않는다. 리프만 넘기고 문구 매핑은 화면·Route가 한다.
 *
 * 「경과일 초기화 확인」만 두 통로 어느 쪽도 타지 않는다. [PlaceRepository.recordAccess]가 실패를 삼키는
 * 계약이라 여기서 결과를 볼 것이 없다(spec EC-022).
 */
@HiltViewModel
internal class PlaceDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val placeRepository: PlaceRepository,
        private val placeCommentRepository: PlaceCommentRepository,
        private val getRoomPickerRooms: GetRoomPickerRoomsUseCase,
        private val resolvePlaceRoomColor: ResolvePlaceRoomColorUseCase,
    ) :
    ViewModel(),
        MviContainer<PlaceDetailUiState, PlaceDetailSideEffect> by mviContainer(
            PlaceDetailUiState(pinId = savedStateHandle.toRoute<PlaceDetailMain>().pinId),
        ),
        DomainErrorEmitter by domainErrorEmitter() {
        /**
         * 방 목록. 상태가 아니라 여기에 두는 것은 **공유 시트가 닫혀 있는 동안에도 필요하기 때문**이다 —
         * `PlaceDetailUiState.shareSheet`는 `null`이 곧 닫힘이라 목록을 계속 담아 둘 자리가 아니고,
         * 마커 색은 시트를 한 번도 열지 않아도 그려져야 한다.
         *
         * UI 모델이 아니라 도메인 모델로 들고 있는다. [ResolvePlaceRoomColorUseCase]가 이 목록을 재료로 받는데
         * 도메인은 화면의 [RoomPickerItem]을 알 수 없으므로, 화면용 변환은 시트를 열 때 그 자리에서 한다.
         *
         * 화면이 읽는 값이 아니라 상태를 만드는 재료다. 시트를 열 때와 [resolveRoomColor]가 색을 찾을 때
         * 각각 상태로 옮겨진다.
         */
        private var rooms: List<RoomSummary> = emptyList()

        private val pinId: String get() = state.value.pinId

        /**
         * 네 갈래를 따로 띄운다.
         *
         * 기록은 나머지 셋의 성패와 무관하고, 핀 상세·코멘트·방 목록은 서로를 기다릴 이유가 없다. 한 코루틴에
         * 묶어 순차로 부르면 가장 느린 응답이 나머지 둘의 렌더를 붙잡는다
         * (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §5).
         */
        init {
            recordAccess()
            loadPlace()
            loadLatestComments()
            loadRooms()
        }

        fun processIntent(intent: PlaceDetailIntent) {
            when (intent) {
                is PlaceDetailIntent.OnSheetLevelChange -> updateState { copy(sheetLevel = intent.level) }
                is PlaceDetailIntent.OnScrollOffsetChange -> changeHeaderMode(intent.isAtTop)
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
         * 이미 도착한 조회까지 되풀이한다. 셋 중 하나만 깨져도 화면 전체가 오류로 덮여 남은 둘의 결과는
         * 사용자에게 닿지 못했으므로, 무엇이 성공했는지를 따로 기억해 두었다가 골라 부를 이유가 없다.
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
            loadRooms()
        }

        private fun loadPlace() {
            launchSafely {
                runCatchingDomain { placeRepository.getPlaceDetail(pinId) }
                    .onSuccess { place ->
                        updateState { copy(place = place) }
                        resolveRoomColor()
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
                        updateState {
                            copy(
                                comments = page.comments.toUiModels(),
                                commentPage = page.page,
                                hasOlderComments = page.hasOlder,
                            )
                        }
                    }.onDomainFailure { error -> updateState { copy(loadError = error) } }
            }
        }

        /**
         * 마커 색과 공유 시트 목록을 한 번의 조회로 함께 채운다
         * (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §5.1).
         *
         * 이 조회가 깨지면 마커도 공유 시트도 채울 것이 없다. 빈 시트가 열리게 두지 않고 화면의 주 데이터로
         * 함께 묶어 오류 화면으로 보낸다 — 목록을 비워 두는 것은 실패를 성공한 빈 결과로 위장하는 꼴이다.
         */
        private fun loadRooms() {
            launchSafely {
                runCatchingDomain { getRoomPickerRooms() }
                    .onSuccess { loaded ->
                        rooms = loaded
                        resolveRoomColor()
                    }.onDomainFailure { error -> updateState { copy(loadError = error) } }
            }
        }

        /**
         * 핀 상세와 방 목록이 **둘 다** 도착해야 색이 정해지므로 어느 쪽이 먼저 와도 같은 자리에서 다시 판정한다.
         *
         * 판정 자체는 [ResolvePlaceRoomColorUseCase]의 몫이다. 두 조회 결과를 합치는 규칙이라 화면 계층에
         * 두지 않는다(`core/domain/README.md` §4). 짝이 되는 방을 못 찾을 때 `null`로 두는 것도 그 UseCase가
         * 정한다(`docs/specs/place-detail/research.md` D15).
         */
        private fun resolveRoomColor() {
            updateState { copy(roomColor = resolvePlaceRoomColor(place, rooms)) }
        }

        /**
         * 최상단에서 벗어나면 헤더를 접는다.
         *
         * 시트 단계를 함께 보지 않는다 — `Full`이어도 최상단이면 확장형이고, 콘텐츠가 짧아 스크롤이 없으면
         * 계속 확장형이다(spec FR-008 · EC-007, `docs/specs/place-detail/research.md` D5).
         */
        private fun changeHeaderMode(isAtTop: Boolean) {
            updateState {
                copy(headerMode = if (isAtTop) PlaceHeaderMode.EXPANDED else PlaceHeaderMode.COLLAPSED)
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
         */
        private fun submitComment() {
            val current = state.value
            if (!current.isSubmitEnabled) return
            val content = current.commentDraft
            updateState { copy(isSubmittingComment = true) }
            launchSafely {
                runCatchingDomain { placeCommentRepository.addComment(pinId, content) }
                    .onSuccess { created ->
                        updateState {
                            copy(
                                comments = (comments + created.toUiModel()).toImmutableList(),
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
                        updateState {
                            copy(comments = comments.filterNot { it.id == commentId }.toImmutableList())
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
                        updateState {
                            copy(
                                comments = (page.comments.toUiModels() + comments).toImmutableList(),
                                commentPage = page.page,
                                hasOlderComments = page.hasOlder,
                            )
                        }
                    }.onDomainFailure(::emitDomainError)
                updateState { copy(isLoadingOlderComments = false) }
            }
        }

        /**
         * 시트를 연다. 열면서 방 목록을 다시 조회하지 않는다 — 진입 때 받아 둔 것을 그대로 쓰므로 시트가 빈 채로
         * 떴다가 채워지는 구간이 없다.
         *
         * 선택은 열 때마다 비운 채로 시작한다. 닫으면 상태째 사라지므로(`shareSheet = null`) 지난번에 고른
         * 방이 되살아나지 않는다.
         */
        private fun openShareSheet() {
            updateState { copy(shareSheet = ShareSheetUiState(rooms = rooms.toPickerItems())) }
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

        private fun List<RoomSummary>.toPickerItems(): ImmutableList<RoomPickerItem> =
            map { it.toPickerItem() }.toImmutableList()

        /**
         * `hasPlace`를 채울 근거가 아직 없어 전부 `false`다.
         *
         * 이번 라운드는 인자 없는 `getRooms()`를 쓰고 `RoomSummary`에 그 필드가 없다. 지어낸 값을 넣는 대신
         * 「이미 저장된 방」 표시만 빠진 채로 두며, `getRooms(placeId)` 확장이 오면 이 한 줄이 바뀐다
         * (`docs/specs/place-detail/research.md` D15 · `tasks.md` 미결 3).
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
                hasPlace = false,
            )

        private companion object {
            /** 서버가 역방향 페이징이라 0이 가장 최신 페이지다. */
            const val LATEST_COMMENT_PAGE = 0
        }
    }
