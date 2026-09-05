package team.mino.feature.onboarding.fake

import kotlinx.coroutines.CompletableDeferred
import team.mino.core.domain.model.OnboardingProgress
import team.mino.core.domain.model.OnboardingStep
import team.mino.core.domain.repository.OnboardingProgressRepository

/**
 * `:feature:onboarding` 테스트용 [OnboardingProgressRepository] 테스트 더블.
 *
 * **호출을 세는 것이 아니라 순서대로 기록한다.** 전이 계약이 정한
 * "저장이 전환보다 앞선다"(`contracts/onboarding-flow-ui.md` §2.4 · EC-019·SC-008)는 횟수로 판정할 수
 * 없기 때문이다 — 어떤 쓰기가 어떤 순서로 들어왔는지, 그리고 그 쓰기가 **끝나기 전에** 스텝이 이미
 * 바뀌지는 않았는지를 봐야 한다. 앞의 것은 [calls]가, 뒤의 것은 [writeGate]가 맡는다.
 *
 * 실패 주입 자리를 두지 않는다. 이 계약의 실패는 도메인 예외가 아니라 버그로 다뤄 그대로 rethrow되며
 * (`OnboardingProgressRepository` KDoc), 온보딩 플로우 ViewModel에 그것을 잡는 분기가 없다.
 * 없는 분기를 위한 스위치를 미리 만들지 않는다.
 */
internal class FakeOnboardingProgressRepository : OnboardingProgressRepository {
    /** [getProgress]가 돌려줄 저장 값. 재개 스텝을 가르는 픽스처다. */
    var progress: OnboardingProgress = OnboardingProgress()

    private val recorded = mutableListOf<Call>()

    /** 들어온 호출을 순서대로 담은 스냅샷. */
    val calls: List<Call> get() = recorded.toList()

    /** 값이 있으면 [getProgress]가 이것이 완료될 때까지 멈춘다 — 읽는 동안의 상태를 붙잡아 둔다. */
    var readGate: CompletableDeferred<Unit>? = null

    /**
     * 값이 있으면 쓰기 세 함수가 이것이 완료될 때까지 멈춘다.
     *
     * 기록은 멈추기 **전에** 남는다 — 그래야 "쓰기가 시작됐지만 아직 끝나지 않은" 순간을 붙잡아
     * 그 사이에 스텝이 먼저 바뀌지 않았는지 볼 수 있다.
     */
    var writeGate: CompletableDeferred<Unit>? = null

    /** 준비 단계에서 생긴 호출을 지운다. 검증 대상 Intent 직전에 부른다. */
    fun clearCalls() {
        recorded.clear()
    }

    override suspend fun getProgress(): OnboardingProgress {
        recorded += Call.GetProgress
        readGate?.await()
        return progress
    }

    override suspend fun setCurrentStep(step: OnboardingStep) {
        recorded += Call.SetCurrentStep(step)
        writeGate?.await()
    }

    override suspend fun setCreatedRoomId(roomId: String) {
        recorded += Call.SetCreatedRoomId(roomId)
        writeGate?.await()
    }

    override suspend fun setInvitedRoomId(roomId: String) {
        recorded += Call.SetInvitedRoomId(roomId)
        writeGate?.await()
    }

    override suspend fun markCompleted() {
        recorded += Call.MarkCompleted
        writeGate?.await()
    }

    /** 이 더블에 들어온 호출 한 건. 인자까지 담아야 저장 값이 계약대로인지 판정할 수 있다. */
    sealed interface Call {
        data object GetProgress : Call

        data class SetCurrentStep(
            val step: OnboardingStep,
        ) : Call

        data class SetCreatedRoomId(
            val roomId: String,
        ) : Call

        data class SetInvitedRoomId(
            val roomId: String,
        ) : Call

        data object MarkCompleted : Call
    }
}
