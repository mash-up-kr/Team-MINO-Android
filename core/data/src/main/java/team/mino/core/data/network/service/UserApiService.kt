package team.mino.core.data.network.service

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import javax.inject.Inject

/**
 * 유저 엔드포인트. `Authorization: Bearer`는 `minoIdentityProofPlugin`이 붙이므로 이 서비스는 토큰을 다루지 않는다.
 */
internal class UserApiService @Inject constructor(
    private val client: HttpClient,
) {
    /**
     * `GET /api/v1/users/me`. **성공 본문을 역직렬화하지 않는다** — 호출자가 쓰는 것은 상태 코드뿐이다
     * (`docs/specs/splash-screen/data-model.md` §3.1).
     *
     * 비2xx는 `convertDomainException`이 `MinoDomainException.Http`로 바꿔 던진다.
     */
    suspend fun getMe() {
        client.get("api/v1/users/me")
    }
}
