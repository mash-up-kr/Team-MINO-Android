package team.mino.feature.sharereceiver.picker.vm

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentSet
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.core.common.android.extension.launchSafely
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.model.SharedPlaceSaveRequest
import team.mino.core.domain.repository.AnonymousAuthRepository
import team.mino.core.domain.repository.SharedPlaceRepository
import team.mino.core.domain.usecase.GetRoomPickerRoomsUseCase
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import team.mino.feature.sharereceiver.picker.model.RoomPickerItem
import team.mino.feature.sharereceiver.picker.model.SheetStep
import team.mino.feature.sharereceiver.picker.model.toRoomPickerItem
import javax.inject.Inject

/**
 * 공유받은 장소를 어느 방에 저장할지 고르는 시트의 ViewModel.
 *
 * 진입점이 `EXTRA_TEXT`에서 뽑아 넘긴 URL을 [SavedStateHandle]로 받는다. 여기서 다시 뽑지 않는다 —
 * 추출은 Activity의 몫이고(`contracts/share-intent.md` §2.1), 이 화면은 이미 확정된 URL 하나를 들고 시작한다.
 *
 * 로딩 슬롯을 두지 않는다. 시트는 세션 확인·목록 조회를 기다리지 않고 이미 떠 있으며 카드 자리만 나중에
 * 채워진다(UX-009 · `data-model.md` §6). 링크 분석도 [저장하기] 이후라 기다릴 것이 없다(FR-017).
 *
 * 저장은 예약까지만 확정한다. 성공·중복·실패는 사용자가 떠난 뒤에 갈리며 알림함으로 전달되므로
 * (FR-014·FR-015) 이 화면은 그 결과를 알지 못한다.
 */
@HiltViewModel
internal class ShareReceiverViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val getRoomPickerRooms: GetRoomPickerRoomsUseCase,
        private val sharedPlaceRepository: SharedPlaceRepository,
        private val anonymousAuthRepository: AnonymousAuthRepository,
        private val resources: Resources,
    ) :
    ViewModel(),
        MviContainer<ShareReceiverUiState, ShareReceiverSideEffect> by mviContainer(ShareReceiverUiState()) {
        /**
         * URL 없는 공유는 진입점이 이미 걸러 내고 시트를 띄우지 않으므로(`contracts/share-intent.md` §2.1),
         * 여기까지 온 인텐트에 URL이 없는 것은 배선 버그다. 빈 문자열로 눙치면 그 버그가 저장 요청을 타고
         * 서버까지 흘러간다.
         */
        private val sharedUrl: String
            get() =
                checkNotNull(savedStateHandle.get<String>(KEY_SHARED_URL)) {
                    "진입점이 공유 URL을 넘기지 않았다."
                }

        init {
            loadRooms()
        }

        fun processIntent(intent: ShareReceiverIntent) {
            when (intent) {
                is ShareReceiverIntent.ToggleRoom -> toggleRoom(intent.roomId)
                is ShareReceiverIntent.ChangeStep -> changeStep(intent.step)
                ShareReceiverIntent.Save -> save()
                ShareReceiverIntent.Dismiss -> dismiss()
                is ShareReceiverIntent.SharedUrlReplaced -> replaceSharedUrl(intent.url)
            }
        }

        /**
         * 세션을 **확인만** 하고 그 뒤에 방 목록을 조회한다.
         *
         * 없으면 확보하지 않고 그대로 물러난다(FR-019 · `research.md` R-012·R-020). 확보를 부르면 네트워크
         * 왕복이 시트 앞을 막고, 그 대기는 스플래시를 거치지 않는 이 진입점의 전제(UX-010)를 깬다.
         *
         * 순서를 뒤집지 않는다. 신원 증명 없이 나간 요청은 `MinoIdentityProofPlugin`의 `checkNotNull`에 걸려
         * 도메인 예외가 아니라 프로그래머 버그로 터진다(`contracts/room-list-api.md` §5).
         *
         * 세션 없음과 조회 실패는 같은 빈 목록으로 수렴한다(R-006 · FR-013). 둘을 가르는 슬롯을 두지 않으므로
         * 여기서 갈리는 것은 조회를 하느냐뿐이다.
         */
        private fun loadRooms() {
            launchSafely {
                if (anonymousAuthRepository.currentSession() == null) return@launchSafely
                runCatchingDomain { getRoomPickerRooms() }
                    .onSuccess { rooms -> updateState { copy(rooms = rooms.toPickerItems()) } }
                    .onDomainFailure { updateState { copy(rooms = persistentListOf()) } }
            }
        }

        /**
         * 선택은 상태 한 곳에만 있다 — 카드가 자기 선택 여부를 들지 않으므로 목록이 다시 그려져도
         * 흩어지지 않는다(FR-007 · UX-003).
         *
         * 목록을 다시 조회하지 않는다. 고르는 동안 카드가 사라지거나 순서가 바뀌면 방금 누른 자리가 달라진다.
         */
        private fun toggleRoom(roomId: String) {
            updateState {
                val selected = selectedRoomIds.toPersistentSet()
                copy(
                    selectedRoomIds = if (roomId in selected) selected.remove(roomId) else selected.add(roomId),
                )
            }
        }

        /**
         * 단계만 갈아 끼운다. 선택도 목록도 건드리지 않으므로 시트를 올렸다 내려도 고른 방이 그대로다(TS-016).
         *
         * 단계가 얼마나 높은지는 여기서 알지 않는다 — 앵커는 시트가 소유한다
         * (`contracts/room-picker-sheet-ui.md` §3.1).
         */
        private fun changeStep(step: SheetStep) {
            updateState { copy(sheetStep = step) }
        }

        /**
         * 시트가 떠 있는 동안 도착한 새 공유로 저장 대상 링크를 갈아 끼운다(EC-013 · `research.md` R-024).
         *
         * [SavedStateHandle]을 덮는 것이 곧 [sharedUrl]을 바꾸는 것이다 — 값을 따로 들지 않으므로 프로세스가
         * 재생성돼도 되살아나는 값과 지금 저장될 값이 갈리지 않는다.
         *
         * 선택은 비운다. 앞선 공유를 위해 고른 방이 남아 있으면 사용자가 고르지 않은 방에 새 링크가 저장된다.
         *
         * **방 목록은 다시 조회하지 않는다.** 바뀐 것은 링크뿐이고, 다시 받으면 두 번째 공유에서만 카드가
         * 늦게 차 SC-001·UX-009가 깨진다.
         */
        private fun replaceSharedUrl(url: String) {
            savedStateHandle[KEY_SHARED_URL] = url
            updateState { copy(selectedRoomIds = persistentSetOf()) }
        }

        /**
         * 고른 방 전부를 한 번의 예약으로 확정한다(FR-010). 방 개수와 무관하게 요청도 워커도 하나이며,
         * 방 단위 분해는 서버가 한다(`research.md` R-021).
         *
         * 코루틴으로 감싸 기다리지 않는다 — 예약은 즉시 반환하고, 토스트가 사라지면 곧바로 물러나야 한다
         * (FR-011 · UX-006).
         */
        private fun save() {
            sharedPlaceRepository.scheduleSave(
                SharedPlaceSaveRequest(
                    url = sharedUrl,
                    roomIds = state.value.selectedRoomIds.toList(),
                ),
            )
            launchSafely { postSideEffect(ShareReceiverSideEffect.SavedAndFinish) }
        }

        /** 저장하지 않고 끝낸다. 공유받은 링크를 남겨 두지도 않는다(FR-012 · FR-013 · EC-001). */
        private fun dismiss() {
            launchSafely { postSideEffect(ShareReceiverSideEffect.Finish) }
        }

        private fun List<RoomSummary>.toPickerItems(): ImmutableList<RoomPickerItem> =
            map { it.toRoomPickerItem(resources) }.toImmutableList()

        internal companion object {
            /**
             * 진입점이 넘긴 공유 URL의 [SavedStateHandle] 키.
             *
             * 진입점과 이 화면이 같은 상수를 보게 해, 전달 수단이 달라지면 조용히 `null`을 읽는 대신
             * 컴파일이 깨지게 한다.
             */
            const val KEY_SHARED_URL = "sharedUrl"
        }
    }
