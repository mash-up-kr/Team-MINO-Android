package team.mino.core.data.repository

import team.mino.core.data.datasource.OnboardingProgressLocalDataSource
import team.mino.core.domain.model.OnboardingProgress
import team.mino.core.domain.model.OnboardingStep
import team.mino.core.domain.repository.OnboardingProgressRepository
import javax.inject.Inject

/**
 * 로컬 저장 값을 [OnboardingProgress]로 조립한다. Mapper를 두지 않는다 — DTO가 없는 로컬 전용 경로라
 * 변환이 이 클래스 하나에서 끝난다.
 *
 * 저장 실패는 잡지 않는다. `docs/conventions/error_handling.md` §3의 화이트리스트 밖이라 그대로
 * 전파되어 CEH로 간다.
 */
internal class OnboardingProgressRepositoryImpl @Inject constructor(
    private val onboardingProgressLocalDataSource: OnboardingProgressLocalDataSource,
) : OnboardingProgressRepository {
    /**
     * **열어야 할 스텝을 판정하지 않는다.** 저장된 값을 그대로 조립할 뿐이라
     * `lastStep == INVITE`인데 `createdRoomId == null`인 어긋난 조합도 그대로 돌려준다 —
     * 그 조합을 떨어뜨리는 판정은 `ResolveOnboardingStepUseCase`가 소유한다
     * (`docs/specs/onboarding-flow/contracts/onboarding-progress.md` §3).
     */
    override suspend fun getProgress(): OnboardingProgress {
        val entry = onboardingProgressLocalDataSource.getProgress()
        return OnboardingProgress(
            lastStep = entry.lastStepName.toOnboardingStep(),
            createdRoomId = entry.createdRoomId,
            isCompleted = entry.isCompleted,
        )
    }

    override suspend fun setCurrentStep(step: OnboardingStep) {
        onboardingProgressLocalDataSource.setLastStepName(step.name)
    }

    override suspend fun setCreatedRoomId(roomId: String) {
        onboardingProgressLocalDataSource.setCreatedRoomId(roomId)
    }

    override suspend fun markCompleted() {
        onboardingProgressLocalDataSource.markCompleted()
    }

    /**
     * 저장된 적 없음(`null`)과 어느 스텝도 가리키지 못하는 값을 한 지점에서 흡수한다. 던지지 않고
     * [OnboardingStep.PROFILE]로 떨어뜨린다 — 낡은 값 하나 때문에 홈으로 튕기는 것보다 온보딩을
     * 처음부터 태우는 편이 안전하다(`docs/specs/onboarding-flow/data-model.md` §4.1).
     */
    private fun String?.toOnboardingStep(): OnboardingStep =
        OnboardingStep.entries.firstOrNull { it.name == this } ?: OnboardingStep.PROFILE
}
