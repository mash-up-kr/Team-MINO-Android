package team.mino.core.domain.usecase

import team.mino.core.domain.model.AnonymousSession
import team.mino.core.domain.repository.AnonymousAuthRepository
import javax.inject.Inject

class EnsureAnonymousSessionUseCase @Inject constructor(
    private val anonymousAuthRepository: AnonymousAuthRepository,
) {
    suspend operator fun invoke(): AnonymousSession = anonymousAuthRepository.ensureSession()
}
