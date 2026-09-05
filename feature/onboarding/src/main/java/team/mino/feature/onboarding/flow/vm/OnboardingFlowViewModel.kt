package team.mino.feature.onboarding.flow.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.core.common.android.extension.launchSafely
import team.mino.core.domain.model.OnboardingStep
import team.mino.core.domain.repository.OnboardingProgressRepository
import team.mino.core.domain.usecase.JoinRoomByInviteCodeUseCase
import team.mino.core.domain.usecase.ResolveOnboardingStepUseCase
import javax.inject.Inject

/**
 * 온보딩 스텝 전이를 소유한다. 전이 표의 단일 출처는
 * `docs/specs/onboarding-flow/contracts/onboarding-flow-ui.md` §2.4이며 이 클래스는 그 표를 옮긴 것이다.
 *
 * 표 전체에 걸리는 규칙이 둘이다.
 *
 * **저장이 전환보다 앞선다** — 모든 전이에서 쓰기가 끝난 뒤에야 스텝과 SideEffect가 움직인다.
 * 순서를 뒤집으면 기록 직전에 죽은 프로세스가 같은 스텝을 두 번 실행한다(EC-019·SC-008).
 *
 * **중복 조작 가드** — 각 Intent는 현재 스텝이 표의 왼쪽 칸과 같고 **직전 전이가 이미 끝났을 때만**
 * 처리한다([transitionFrom]). 스텝 비교만으로는 부족하다 — 저장이 전환보다 앞서므로 쓰기가 끝나기
 * 전에는 스텝이 아직 왼쪽 칸 그대로이고, 그 사이에 도착한 두 번째 조작이 같은 전이를 한 번 더 태운다.
 * 버튼을 빠르게 두 번 눌러도 같은 스텝이 두 번 열리지 않아야 한다는 것이 EC-003이며
 * (`quickstart.md` §4.5의 4번 시나리오), 이 가드가 있어 화면 쪽에서 버튼을 잠그지 않는다(UX-005).
 *
 * 표의 마지막 두 줄은 전이 뒤에도 스텝이 그대로라 이 가드로 갈리지 않으므로 각각
 * [onRoomFormCanceled]·[onTutorialFinished]에서 따로 판단한다.
 *
 * 저장 실패는 잡지 않는다 — [OnboardingProgressRepository]의 실패는 도메인 예외가 아니라 버그이며
 * (`docs/conventions/error_handling.md` §1) 그대로 [launchSafely]의 CEH로 간다.
 */
@HiltViewModel
internal class OnboardingFlowViewModel @Inject constructor(
    private val onboardingProgressRepository: OnboardingProgressRepository,
    private val resolveOnboardingStep: ResolveOnboardingStepUseCase,
    private val joinRoomByInviteCode: JoinRoomByInviteCodeUseCase,
) : ViewModel(),
    MviContainer<OnboardingFlowUiState, OnboardingFlowSideEffect> by mviContainer(OnboardingFlowUiState()) {
    /** 재개 조회를 시작했는지. 구성 변경으로 [OnboardingFlowIntent.Start]가 다시 와도 조회는 한 번만 돈다. */
    private var hasStarted = false

    /**
     * [OnboardingFlowIntent.Start]가 실어 온, 프로필 저장 시점에 자동 참여할 초대 코드(SYS-010).
     * [onProfileSaved]가 소비하는 즉시 `null`로 되돌려, 그 시도가 실패해 정상 흐름(Flow B)으로 폴백한
     * 뒤에는 같은 코드로 다시 시도하지 않는다 — 재시도 조작 수단이 이 화면엔 없다.
     */
    private var pendingInviteCode: String? = null

    /** 진행 중인 전이. 살아 있는 동안 들어온 Intent는 [transitionFrom]이 버린다. */
    private var transitionJob: Job? = null

    /**
     * 완료를 이미 처리했는지. 표의 마지막 줄은 스텝을 바꾸지 않아 스텝 비교로는 두 번째
     * [OnboardingFlowIntent.TutorialFinished]를 걸러내지 못하는데, [건너뛰기]와 CTA는 연타할 수 있는
     * 버튼이라 걸러야 한다. 상태가 아니라 처리 조건이므로 [OnboardingFlowUiState]에 두지 않는다
     * (계약 §2.1의 세 필드가 전부다).
     */
    private var hasFinishedTutorial = false

    fun processIntent(intent: OnboardingFlowIntent) {
        when (intent) {
            is OnboardingFlowIntent.Start -> start(intent.pendingInviteCode)
            OnboardingFlowIntent.ProfileSaved -> onProfileSaved()
            is OnboardingFlowIntent.RoomCreated -> onRoomCreated(intent.roomId)
            OnboardingFlowIntent.RoomFormSkipped -> onRoomFormSkipped()
            OnboardingFlowIntent.RoomFormCanceled -> onRoomFormCanceled()
            OnboardingFlowIntent.InviteClosed -> onInviteClosed()
            OnboardingFlowIntent.TutorialFinished -> onTutorialFinished()
        }
    }

    /**
     * 저장된 진행 상태를 한 번 읽어 재개 지점을 정하고 그 스텝을 연다(FR-023).
     *
     * 진입 자체는 아무것도 저장하지 않으며, 재개 스텝의 판정은 [ResolveOnboardingStepUseCase]가 소유한다 —
     * 여기서 저장 값을 다시 해석하지 않는다.
     */
    private fun start(pendingInviteCode: String?) {
        if (hasStarted) return
        hasStarted = true
        this.pendingInviteCode = pendingInviteCode

        launchSafely {
            val progress = onboardingProgressRepository.getProgress()
            val resumedStep = resolveOnboardingStep(progress)

            updateState {
                copy(
                    isLoading = false,
                    step = resumedStep,
                    createdRoomId = progress.createdRoomId,
                    invitedRoomId = progress.invitedRoomId,
                )
            }
            postSideEffect(resumedStep.entryEffect(progress.createdRoomId))
        }
    }

    /**
     * 프로필 저장 직후. [pendingInviteCode]를 들고 있으면(SYS-010 Flow A, 신규 유저) 공동방 생성
     * 유도(Flow B)로 가지 않고 그 코드로 자동 참여를 먼저 시도한다.
     *
     * 성공하면 공동방 생성·친구 초대 스텝만 건너뛰고 튜토리얼로 간다(Figma 스펙 — 온보딩 → 프로필
     * 설정 → 튜토리얼 → 초대받은 방 상세). 참여한 방은 [OnboardingProgressRepository.setInvitedRoomId]로
     * 남겨 튜토리얼을 마쳤을 때([onTutorialFinished]) 그 방으로 보낼 수 있게 한다.
     * 실패(만료·잘못된 코드 등)는 조용히 폴백한다 — 코드 없이 진입한 것과 같은 정상 흐름(Flow B)으로
     * 진행하고, 이 시도를 다시 걸지 않도록 [pendingInviteCode]를 비운다.
     */
    private fun onProfileSaved() {
        val inviteCode = pendingInviteCode
        if (inviteCode == null) {
            advanceTo(from = OnboardingStep.PROFILE, to = OnboardingStep.ROOM_FORM)
            return
        }

        transitionFrom(OnboardingStep.PROFILE) {
            pendingInviteCode = null

            val roomId = runCatching { joinRoomByInviteCode(inviteCode) }.getOrNull()
            if (roomId != null) {
                moveToStep(OnboardingStep.TUTORIAL) {
                    onboardingProgressRepository.setInvitedRoomId(roomId)
                    updateState { copy(invitedRoomId = roomId) }
                }
                return@transitionFrom
            }

            moveToStep(OnboardingStep.ROOM_FORM)
        }
    }

    private fun onRoomCreated(roomId: String) =
        advanceTo(from = OnboardingStep.ROOM_FORM, to = OnboardingStep.INVITE) {
            onboardingProgressRepository.setCreatedRoomId(roomId)
            updateState { copy(createdRoomId = roomId) }
        }

    /** 방을 만들지 않고 초대 스텝을 통째로 지나간다(FR-003·TS-012). 그래서 방 id 기록이 없다. */
    private fun onRoomFormSkipped() = advanceTo(from = OnboardingStep.ROOM_FORM, to = OnboardingStep.TUTORIAL)

    /**
     * 취소는 전이가 아니다 — 저장도 스텝 변경도 없이 폼만 다시 연다([research.md R-020]).
     *
     * **이 갈래만 중복 가드 밖이라 [transitionFrom]을 쓰지 않는다.** 두 번째 취소는 버튼 연타가 아니라
     * 공동방 폼 Activity가 실제로 한 번 더 닫혔다는 결과이고, 그때 폼을 다시 열지 않으면 온보딩이
     * 빈 화면에 멈춘다.
     */
    private fun onRoomFormCanceled() {
        if (state.value.step != OnboardingStep.ROOM_FORM) return

        launchSafely { postSideEffect(OnboardingFlowSideEffect.LaunchRoomForm) }
    }

    /** 초대를 닫아도 만든 방은 남는다 — 스텝만 넘어가고 `createdRoomId`는 그대로 둔다. */
    private fun onInviteClosed() = advanceTo(from = OnboardingStep.INVITE, to = OnboardingStep.TUTORIAL)

    /**
     * 완료는 스텝이 아니라 완료 표시가 든다. 그래서 이 줄에만 `setCurrentStep`이 없다.
     *
     * [OnboardingFlowUiState.invitedRoomId]가 있으면(SYS-010, 초대로 들어와 참여까지 끝난 온보딩)
     * 평소 홈이 아니라 그 방으로 바로 들어간다.
     */
    private fun onTutorialFinished() {
        if (state.value.step != OnboardingStep.TUTORIAL || hasFinishedTutorial) return
        hasFinishedTutorial = true

        launchSafely {
            onboardingProgressRepository.markCompleted()

            val invitedRoomId = state.value.invitedRoomId
            postSideEffect(
                if (invitedRoomId != null) {
                    OnboardingFlowSideEffect.NavigateToHomeWithRoom(invitedRoomId)
                } else {
                    OnboardingFlowSideEffect.NavigateToHome
                },
            )
        }
    }

    /**
     * [from]에서 [to]로 넘어간다 — 저장이 전환보다 앞서고(EC-019·SC-008), 새 스텝을 여는 지시는
     * [entryEffect]가 낸다. 스텝마다 손으로 적으면 진입 경로와 재개 경로가 서로 다른 사본을 보게 되고,
     * 그 어긋남은 재개했을 때만 드러나 테스트가 잡지 못한다.
     *
     * [beforeStep]은 스텝 기록보다 먼저 남겨야 하는 값이 있을 때만 쓴다(방 id).
     */
    private fun advanceTo(
        from: OnboardingStep,
        to: OnboardingStep,
        beforeStep: suspend () -> Unit = {},
    ) = transitionFrom(from) { moveToStep(to, beforeStep) }

    /**
     * [to]를 열고 기록한다 — [advanceTo]의 몸통이자, 이미 [transitionFrom] 가드 안에서 실행 중인
     * 다른 전이([onProfileSaved]의 참여 실패 폴백)가 재진입 없이 재사용하는 자리다. [transitionFrom]을
     * 다시 부르면 자기 자신이 쥔 [transitionJob]이 아직 살아 있어 가드에 걸려 아무 일도 안 일어난다.
     */
    private suspend fun moveToStep(
        to: OnboardingStep,
        beforeStep: suspend () -> Unit = {},
    ) {
        beforeStep()
        onboardingProgressRepository.setCurrentStep(to)

        updateState { copy(step = to) }
        postSideEffect(to.entryEffect(state.value.createdRoomId))
    }

    /**
     * [step]에 머무르고 앞선 전이가 끝났을 때만 [transition]을 실행한다. 앞 조건이 표의 왼쪽 칸이고,
     * 뒤 조건이 쓰기가 도는 동안 열려 있는 창을 닫는다(EC-003).
     */
    private fun transitionFrom(
        step: OnboardingStep,
        transition: suspend () -> Unit,
    ) {
        if (state.value.step != step || transitionJob?.isActive == true) return

        transitionJob = launchSafely { transition() }
    }

    /**
     * 이 스텝을 여는 전환. 재개 경로에는 폼의 결과 인텐트가 없어 초대 화면에 실을 방 id가
     * 저장된 진행 상태에서 온다.
     */
    private fun OnboardingStep.entryEffect(createdRoomId: String?): OnboardingFlowSideEffect =
        when (this) {
            OnboardingStep.PROFILE -> OnboardingFlowSideEffect.LaunchProfile
            OnboardingStep.ROOM_FORM -> OnboardingFlowSideEffect.LaunchRoomForm
            OnboardingStep.INVITE ->
                OnboardingFlowSideEffect.NavigateToInvite(
                    // 방 없는 INVITE는 ResolveOnboardingStepUseCase가 이미 떨어뜨려 여기 오지 않는다.
                    checkNotNull(createdRoomId) { "방 없이 초대 스텝이 열렸다" },
                )

            OnboardingStep.TUTORIAL -> OnboardingFlowSideEffect.NavigateToTutorial
        }
}
