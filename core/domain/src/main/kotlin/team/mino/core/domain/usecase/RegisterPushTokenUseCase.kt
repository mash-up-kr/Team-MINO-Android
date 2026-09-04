package team.mino.core.domain.usecase

import team.mino.core.domain.repository.PushRegistrationRepository
import javax.inject.Inject

/**
 * 앱 시작(Splash)·토큰 갱신 콜백·마이페이지 알림 권한 허용, 세 호출 지점이 공유하는 단일 진입점.
 *
 * 무인자다 — 갱신 콜백이 새 토큰 값을 넘겨받더라도 그 값을 쓰지 않고 다시 조회한다
 * (`docs/specs/push-notification/research.md` D5).
 */
class RegisterPushTokenUseCase @Inject constructor(
    private val pushRegistrationRepository: PushRegistrationRepository,
) {
    suspend operator fun invoke() = pushRegistrationRepository.registerCurrentToken()
}
