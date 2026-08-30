package team.mino.core.data.network

import team.mino.core.data.auth.IdTokenProvider

internal class FakeIdTokenProvider(
    var token: String? = null,
) : IdTokenProvider {
    var error: Throwable? = null

    override suspend fun getIdToken(): String? {
        error?.let { throw it }
        return token
    }
}
