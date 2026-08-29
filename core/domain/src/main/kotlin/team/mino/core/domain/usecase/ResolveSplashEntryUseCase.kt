package team.mino.core.domain.usecase

import team.mino.core.domain.model.SplashEntry
import team.mino.core.domain.repository.OnboardingProgressRepository
import team.mino.core.domain.repository.ProfileRegistrationRepository
import javax.inject.Inject

/**
 * 프로필 등록 여부와 온보딩 완료 표시를 함께 보고 스플래시 다음 진입 지점을 정한다.
 * 판정 표는 `docs/specs/splash-screen/contracts/splash-entry-decision.md` §2가 소유한다 —
 * **둘 중 하나라도 없으면 온보딩이다.**
 *
 * 세션은 확보하지 않는다 — 호출자가 [EnsureAnonymousSessionUseCase]로 먼저 확보한다.
 *
 * 어느 근거의 실패도 잡지 않는다. 로컬 조회 실패를 [SplashEntry.Main]으로 뭉개면 안 되고,
 * 서버 조회 실패를 [SplashEntry.Onboarding]으로 뭉개면 기존 사용자가 온보딩으로 떨어진다.
 */
class ResolveSplashEntryUseCase @Inject constructor(
    private val profileRegistrationRepository: ProfileRegistrationRepository,
    private val onboardingProgressRepository: OnboardingProgressRepository,
) {
    suspend operator fun invoke(): SplashEntry {
        // 두 조회를 지역 변수로 각각 받아 호출 순서를 코드에 고정한다. isRegistered()는 순수 조회로 읽히지만
        // 미등록 판정 시 프로필 로컬 캐시를 비우는 부수 효과를 갖고, :feature:profile의 등록/수정 분기
        // (ProfileEntryPoint.needsRefresh)가 그 보장에 기댄다. 완료 표시를 먼저 읽거나 `&&`로 이어 붙여
        // 단축 평가로 이 호출을 건너뛰면 컴파일도 이 UseCase의 테스트도 통과하면서 프로필 저장이 깨진다
        // (docs/adr/2026-08-29-onboarding-entry-decision-owned-by-onboarding.md §결정 3항).
        val isProfileRegistered = profileRegistrationRepository.isRegistered()
        val isOnboardingCompleted = onboardingProgressRepository.getProgress().isCompleted

        return if (isProfileRegistered && isOnboardingCompleted) SplashEntry.Main else SplashEntry.Onboarding
    }
}
