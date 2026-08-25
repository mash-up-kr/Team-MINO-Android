package team.mino.core.data.auth

import com.google.firebase.auth.FirebaseAuth
import team.mino.core.data.auth.extension.awaitDomain
import javax.inject.Inject

internal class IdTokenProviderImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : IdTokenProvider {
    /**
     * `forceRefresh = false`는 유효한 캐시 토큰을 그대로 쓰고 만료가 임박했을 때만 SDK가 갱신하게 한다.
     * 앱은 토큰도 갱신 수단도 저장하지 않고 요청 시점마다 받아 쓴다 — research.md R-010.
     */
    override suspend fun getIdToken(): String? =
        firebaseAuth.currentUser
            ?.getIdToken(false)
            ?.awaitDomain()
            ?.token
}
