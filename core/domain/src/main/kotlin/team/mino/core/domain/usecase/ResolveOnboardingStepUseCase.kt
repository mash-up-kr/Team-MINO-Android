package team.mino.core.domain.usecase

import team.mino.core.domain.model.OnboardingProgress
import team.mino.core.domain.model.OnboardingStep
import javax.inject.Inject

/**
 * 저장된 진행 상태에서 **열어야 할 스텝**을 판정한다
 * (`docs/specs/onboarding-flow/contracts/onboarding-progress.md` §3).
 *
 * 조회는 호출자가 하고 이 함수는 판정만 한다 — Repository를 주입받지 않는 순수 함수이며 `suspend`가 아니다.
 *
 * [OnboardingProgress.isCompleted]는 보지 않는다. 완료된 설치는 온보딩을 열지 않아 여기 도달하지 않으며,
 * 그 판정은 [ResolveSplashEntryUseCase]가 소유한다.
 */
class ResolveOnboardingStepUseCase @Inject constructor() {
    operator fun invoke(progress: OnboardingProgress): OnboardingStep =
        when (progress.lastStep) {
            // 방이 없으면 초대할 대상도 없다(FR-004·SC-004). 저장 값이 손상되거나 스텝 구성이 바뀌면
            // 만들어질 수 있는 조합이며, 이 방어 규칙이 판정을 UseCase로 세운 이유다.
            OnboardingStep.INVITE ->
                if (progress.createdRoomId != null) OnboardingStep.INVITE else OnboardingStep.TUTORIAL

            else -> progress.lastStep
        }
}
