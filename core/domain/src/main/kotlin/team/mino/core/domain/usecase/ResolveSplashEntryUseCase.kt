package team.mino.core.domain.usecase

import team.mino.core.domain.model.SplashEntry
import team.mino.core.domain.repository.ProfileRegistrationRepository
import javax.inject.Inject

/**
 * 프로필 등록 여부로 스플래시 다음 진입 지점을 정한다.
 *
 * 세션은 확보하지 않는다 — 호출자가 [EnsureAnonymousSessionUseCase]로 먼저 확보한다.
 */
class ResolveSplashEntryUseCase @Inject constructor(
    private val profileRegistrationRepository: ProfileRegistrationRepository,
) {
    suspend operator fun invoke(): SplashEntry =
        if (profileRegistrationRepository.isRegistered()) SplashEntry.Main else SplashEntry.Onboarding
}
