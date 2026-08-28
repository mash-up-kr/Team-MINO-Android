package team.mino.core.data.network.service

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import team.mino.core.data.network.dto.request.PinCreateRequest
import javax.inject.Inject

/**
 * 핀 엔드포인트를 호출하는 서비스.
 *
 * 신원 증명 헤더는 `MinoIdentityProofPlugin`이 싣고, 예외는 `convertDomainException`이
 * `MinoDomainException`으로 바꿔 던지므로 여기서 잡지 않는다.
 */
internal class PinApiService @Inject constructor(
    private val client: HttpClient,
) {
    /**
     * 공유받은 링크에서 장소를 추출해 [request]의 방들에 핀을 추가하도록 요청한다.
     *
     * **방 개수와 무관하게 요청은 1건이다.** 저장 대상 방이 경로가 아니라 본문의 `roomIds`에 실리며,
     * 방마다 갈라 처리하는 것은 서버 몫이다 — 근거는
     * `docs/specs/shared-link-receiver/research.md` R-021.
     *
     * 응답 `202`의 본문은 읽지 않는다 — 스키마가 정의돼 있지 않고 접수 사실 외에 쓸 값이 없다.
     * `expectSuccess = true`가 `202`를 성공으로 판정하므로 반환값이 없다.
     * 실패 판정은 `errorCode`가 아니라 HTTP 상태 코드만 본다 —
     * 근거는 `docs/specs/shared-link-receiver/contracts/shared-place-save-api.md` §1.2.
     */
    suspend fun createPin(request: PinCreateRequest) {
        client.post("api/v1/rooms/pins") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
