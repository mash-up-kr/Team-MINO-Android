package team.mino.core.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import team.mino.core.data.auth.AnonymousAuthProvider
import team.mino.core.domain.model.AnonymousSession
import team.mino.core.domain.repository.AnonymousAuthRepository
import javax.inject.Inject

internal class AnonymousAuthRepositoryImpl @Inject constructor(
    private val anonymousAuthProvider: AnonymousAuthProvider,
) : AnonymousAuthRepository {
    // 확인과 발급 사이에 다른 호출이 끼어들어 세션이 두 번 발급되는 것을 막는다.
    private val mutex = Mutex()

    override suspend fun ensureSession(): AnonymousSession {
        // 잠금 밖 빠른 경로 — 이미 확보된 세션은 인증 제공자와의 왕복 없이 돌려준다.
        anonymousAuthProvider.currentUserId()?.let { return AnonymousSession(userId = it) }

        return mutex.withLock {
            // 잠금을 기다리는 사이 선행 호출이 이미 발급받았을 수 있어 다시 확인한다.
            val userId = anonymousAuthProvider.currentUserId()
                ?: anonymousAuthProvider.signInAnonymously()
            AnonymousSession(userId = userId)
        }
    }
}
