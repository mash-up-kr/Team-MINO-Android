package team.mino.core.data.network.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import team.mino.core.data.network.dto.request.PinCreateRequest
import team.mino.core.data.network.dto.request.PinDuplicateRequest
import team.mino.core.data.network.dto.response.MinoResponse
import team.mino.core.data.network.dto.response.PinDetailResponse
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
     * [pinId] 핀 하나의 상세를 조회한다 —
     * `docs/specs/place-detail/contracts/place-api.md` §1.
     *
     * 목록(`GET /api/v1/pins`)이 쓰는 `PinResponse`가 아니라 [PinDetailResponse]로 받는다. 같은 엔드포인트가
     * 아니고, 이 응답에만 `sourceUrl`과 닉네임·아바타까지 실린 `createdBy`가 온다.
     *
     * 화면이 읽지 않는 필드까지 그대로 흘린다 — 무엇을 도메인에 올릴지는 Mapper가 정한다(같은 계약 §1.2).
     */
    suspend fun getPinDetail(pinId: String): PinDetailResponse =
        client
            .get("api/v1/pins/$pinId")
            .body<MinoResponse<PinDetailResponse>>()
            .data

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

    /**
     * [pinId] 장소의 「경과일 초기화 확인」을 알린다 —
     * `docs/specs/home-deck-exploration/contracts/deck-api.md` §3.2.
     *
     * 본문이 없는 `POST`다. 응답 `{ "data": { "ok": true } }`도 읽지 않는다 — 성공 여부는 상태 코드가
     * 이미 말하고 `ok`에서 더 얻을 값이 없다. 서버는 append-only 로그에 한 줄을 더할 뿐이다.
     */
    suspend fun recordPinAccess(pinId: String) {
        client.post("api/v1/pins/$pinId/accesses")
    }

    /**
     * [pinId] 장소를 [request]의 방들에 복제한다 — 같은 계약 §3.3.
     *
     * **`409`를 여기서 잡지 않는다.** 대상 방에 같은 장소가 있으면 서버가 전체를 거절하는데, 그것은
     * 사용자에게 알려야 할 실패다. `expectSuccess = true`가 만든 예외를 `convertDomainException`이
     * `MinoDomainException.Http`로 바꿔 스낵바까지 올려 보낸다.
     */
    suspend fun duplicatePin(
        pinId: String,
        request: PinDuplicateRequest,
    ) {
        client.post("api/v1/pins/$pinId/duplicate") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
