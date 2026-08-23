package team.mino.core.data.auth

import kotlinx.coroutines.yield

internal class FakeAnonymousAuthProvider(
    var existingUserId: String? = null,
) : AnonymousAuthProvider {
    var issuedUserId: String = "issued-user-id"
    var signInCount = 0
    var signInError: Throwable? = null

    override suspend fun currentUserId(): String? {
        // 확인과 발급 사이에 다른 코루틴이 끼어들 수 있는 실제 suspend 지점을 재현한다 (동시성 테스트 전제)
        yield()
        return existingUserId
    }

    override suspend fun signInAnonymously(): String {
        yield()
        signInCount++
        signInError?.let { throw it }
        existingUserId = issuedUserId
        return issuedUserId
    }
}
