package team.mino.feature.roomform.form.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.core.common.android.extension.launchSafely
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomDraft
import team.mino.core.domain.repository.RoomRepository
import team.mino.core.domain.usecase.CreateRoomUseCase
import team.mino.core.domain.usecase.ValidateRoomNameUseCase
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.domainErrorEmitter
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import team.mino.feature.roomform.RoomForm
import team.mino.feature.roomform.form.model.RoomFormDialog
import team.mino.feature.roomform.form.model.RoomFormMode
import javax.inject.Inject

/**
 * 공동방 생성·편집 폼의 ViewModel.
 *
 * 진입 맥락(편집 대상 `roomId`·온보딩 여부)은 화면으로 드릴링하지 않고 여기서 라우트 인자로 복원해
 * 상태의 초기값으로 넣는다.
 *
 * 실패는 성격에 따라 통로가 갈린다 — 편집 진입 조회 실패는 화면 전체를 재시도 가능한 오류로 바꾸므로
 * [RoomFormUiState.loadError]에 담고, 생성·편집 제출처럼 사용자가 일으킨 일회성 실패는
 * [DomainErrorEmitter]로 방출해 `RoomFormRoute`가 스낵바로 표시한다.
 */
@HiltViewModel
internal class RoomFormViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val roomRepository: RoomRepository,
        private val validateRoomName: ValidateRoomNameUseCase,
        private val createRoom: CreateRoomUseCase,
    ) :
    ViewModel(),
        MviContainer<RoomFormUiState, RoomFormSideEffect> by mviContainer(RoomFormUiState()),
        DomainErrorEmitter by domainErrorEmitter() {
        init {
            restoreEntry(savedStateHandle.toRoute<RoomForm>())
            loadRoomForEdit()
        }

        /**
         * 라우트 인자를 상태의 초기값으로 옮긴다.
         *
         * 편집 진입이어도 여기서 조회를 걸지 않는다 — `roomId`의 유무로 모드만 가르고,
         * 값을 채우는 것은 편집 진입 로드가 맡는다.
         */
        private fun restoreEntry(route: RoomForm) {
            updateState {
                copy(
                    mode = route.roomId?.let(RoomFormMode::Edit) ?: RoomFormMode.Create,
                    isOnboarding = route.isOnboarding,
                )
            }
        }

        /**
         * 아직 비어 있는 분기는 각 사용자 스토리 구현이 채운다.
         * 여기서 한 곳에 몰아 구현하지 않는 것은 스토리별로 독립 검증하기 위해서다.
         */
        fun processIntent(intent: RoomFormIntent) {
            when (intent) {
                is RoomFormIntent.NameChanged -> changeName(intent.value)
                is RoomFormIntent.DescriptionChanged -> changeDescription(intent.value)
                is RoomFormIntent.ColorSelected -> selectColor(intent.color)
                RoomFormIntent.SubmitClicked -> submit()
                RoomFormIntent.SaveConfirmed -> saveNewRoom()
                RoomFormIntent.BackClicked -> goBack()
                RoomFormIntent.ExitConfirmed -> finish(RoomFormOutcome.Cancelled)
                RoomFormIntent.DialogDismissed -> dismissDialog()
                RoomFormIntent.SkipClicked -> finish(RoomFormOutcome.Skipped)
                RoomFormIntent.RetryLoad -> loadRoomForEdit()
            }
        }

        /**
         * 편집 진입의 초기값 조회. 생성 진입에는 불러올 방이 없어 아무 일도 하지 않는다.
         *
         * 이미 채워진 폼은 다시 불러오지 않는다 — 재시도는 실패해서 비어 있는 폼의 몫이고,
         * 채워진 뒤에 한 번 더 덮으면 사용자가 고치던 값이 조용히 되돌아간다.
         *
         * [RoomFormUiState.values]와 [RoomFormUiState.initial]을 같은 값으로 함께 채운다 —
         * 둘이 갈리면 손대지도 않은 폼이 열리자마자 변경된 폼이 된다.
         * 이름 판정도 여기서 받는다. 받지 않으면 아무것도 고칠 필요가 없는 편집 폼의 CTA가 비활성으로 열린다.
         */
        private fun loadRoomForEdit() {
            val current = state.value
            val mode = current.mode
            if (mode !is RoomFormMode.Edit || current.isLoading || current.initial != null) return
            updateState { copy(isLoading = true, loadError = null) }
            launchSafely {
                runCatchingDomain { roomRepository.getRoom(mode.roomId) }
                    .onSuccess { room ->
                        val loaded =
                            RoomFormValues(
                                name = room.name,
                                description = room.description,
                                color = room.color,
                            )
                        updateState {
                            copy(
                                values = loaded,
                                initial = loaded,
                                nameValidation = validateRoomName(room.name),
                            )
                        }
                    }.onDomainFailure { updateState { copy(loadError = it) } }
                updateState { copy(isLoading = false) }
            }
        }

        /**
         * 방 이름의 상한을 여기서 자른다 — 이름 필드에는 카운터가 없어 입력 컴포넌트가 상한을 모른다.
         * 상한을 넘겨 붙여넣은 값도 요청 전체를 버리지 않고 앞부분만 남긴다.
         *
         * 판정은 잘라낸 값으로 다시 받는다. 원본으로 판정하면 화면에 없는 글자가 오류를 만든다.
         */
        private fun changeName(value: String) {
            val name = value.take(NAME_MAX_LENGTH)
            updateState {
                copy(
                    values = values.copy(name = name),
                    nameValidation = validateRoomName(name),
                )
            }
        }

        /** 방 설명의 상한은 입력 컴포넌트가 이미 걸었다. 여기서 다시 자르면 자르는 주체가 둘이 된다. */
        private fun changeDescription(value: String) {
            updateState { copy(values = values.copy(description = value)) }
        }

        /** 색은 단일 선택이라 교체만 한다. 같은 색을 다시 골라도 해제되지 않는다. */
        private fun selectColor(color: RoomColor) {
            updateState { copy(values = values.copy(color = color)) }
        }

        /**
         * 생성 경로의 CTA는 저장이 아니라 확인 요청이다 — 방은 모달의 [저장하기]를 받은 뒤에야 만들어진다.
         * 편집 경로에는 그 확인이 없어 CTA가 곧 저장이다.
         *
         * 저장할 수 없는 폼과 이미 제출 중인 폼을 [RoomFormUiState.canSubmit] 하나가 함께 막는다.
         * 제출 중에도 모달이 뜨면 이미 나간 요청 위에 확인 창이 다시 얹힌다.
         */
        private fun submit() {
            val current = state.value
            if (!current.canSubmit) return
            when (val mode = current.mode) {
                RoomFormMode.Create -> updateState { copy(dialog = RoomFormDialog.Save) }
                is RoomFormMode.Edit -> saveEditedRoom(mode.roomId)
            }
        }

        /**
         * 확인을 받은 뒤의 생성. 모달을 닫으면서 제출 중으로 넘어가 재클릭이 두 번째 요청을 만들지 못하게 한다.
         *
         * 미선택 색을 확정하는 것은 [CreateRoomUseCase]라 여기서 채우지 않는다.
         * 실패는 폼을 되돌리지 않는다 — 입력값을 남긴 채 알림만 내보내고, 닫은 모달을 다시 열지 않는다.
         */
        private fun saveNewRoom() {
            val current = state.value
            if (!current.canSubmit) return
            val draft = current.values.toDraft()
            updateState { copy(dialog = null, isSubmitting = true) }
            submit(draft) { RoomFormOutcome.Created(createRoom(it).id) }
        }

        /**
         * 편집 경로의 저장. 확인 모달이 없어 CTA가 곧 요청이므로 재클릭은 제출 중 표시 하나가 막는다.
         *
         * 전용 UseCase를 거치지 않는 것은 확정할 기본값도 적용할 규칙도 없어서다 —
         * 폼에 색을 해제하는 수단이 없어 편집 경로의 색은 미선택으로 내려가지 않는다.
         *
         * 실패는 폼을 되돌리지 않는다. 입력값을 남긴 채 알림만 내보낸다.
         */
        private fun saveEditedRoom(roomId: String) {
            val draft = state.value.values.toDraft()
            updateState { copy(isSubmitting = true) }
            submit(draft) {
                roomRepository.updateRoom(roomId, it)
                RoomFormOutcome.Updated(roomId)
            }
        }

        /**
         * 생성·편집이 공유하는 제출 통로. 어느 경로든 성공하면 결과와 함께 폼을 끝내고, 실패는 알림으로만 내보낸다.
         *
         * 제출 중 표시를 켜는 것은 호출자다 — 켜면서 함께 거둘 것(생성 경로의 모달)이 경로마다 달라서다.
         * 끄는 것은 성패와 무관하게 같아 여기서 한 번만 한다.
         */
        private fun submit(
            draft: RoomDraft,
            request: suspend (RoomDraft) -> RoomFormOutcome,
        ) {
            launchSafely {
                runCatchingDomain { request(draft) }
                    .onSuccess { postSideEffect(RoomFormSideEffect.Finish(it)) }
                    .onDomainFailure(::emitDomainError)
                updateState { copy(isSubmitting = false) }
            }
        }

        /**
         * 뒤로가기. 잃을 것이 없는 폼은 붙잡지 않고 곧바로 끝낸다 —
         * 확인 단계를 하나 더 두면 빈 폼에서도 나가는 데 두 번의 조작이 든다.
         *
         * 무엇을 잃는지는 진입 맥락마다 다르므로([RoomFormUiState.needsExitConfirm]) 안내 문구도 갈린다.
         * 슬롯이 하나라 종류를 틀리면 생성용 문구가 편집 폼의 뒤로가기에 뜬다.
         */
        private fun goBack() {
            val current = state.value
            if (!current.needsExitConfirm) {
                finish(RoomFormOutcome.Cancelled)
                return
            }
            val dialog =
                when (current.mode) {
                    RoomFormMode.Create -> RoomFormDialog.ExitCreate
                    is RoomFormMode.Edit -> RoomFormDialog.ExitEdit
                }
            updateState { copy(dialog = dialog) }
        }

        /**
         * 폼을 끝낸다. 뜬 모달을 함께 거둬 다음 진입이 남은 모달을 물려받지 않게 한다.
         *
         * 어디로 갈지는 정하지 않는다 — 결과만 알리고 그 다음은 진입점의 몫이다.
         */
        private fun finish(outcome: RoomFormOutcome) {
            updateState { copy(dialog = null) }
            launchSafely { postSideEffect(RoomFormSideEffect.Finish(outcome)) }
        }

        /** 모달만 닫는다. 다른 상태를 건드리면 취소가 곧 입력 소실이 된다. */
        private fun dismissDialog() {
            updateState { copy(dialog = null) }
        }

        /**
         * 폼의 현재 값을 저장 요청의 입력으로 옮긴다.
         *
         * 앞뒤 공백을 여기서 턴다 — [RoomDraft]가 KDoc으로 선언한 불변식이라 만드는 자리가 둘로 갈리면
         * 한쪽만 지키는 일이 생긴다. 미선택 색은 그대로 넘긴다. 확정하는 것은 저장 경로의 몫이다.
         */
        private fun RoomFormValues.toDraft(): RoomDraft =
            RoomDraft(
                name = name.trim(),
                description = description,
                color = color,
            )

        private companion object {
            /** 방 이름의 허용 문자는 모두 코드 유닛 하나라 [String.length]가 화면 글자 수와 같다. */
            const val NAME_MAX_LENGTH = 15
        }
    }
