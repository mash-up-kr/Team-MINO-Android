package team.mino.core.data.network.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import team.mino.core.data.network.dto.response.InvitationPreviewResponse
import team.mino.core.data.network.dto.response.InvitationResponse
import team.mino.core.data.network.dto.response.MinoResponse
import javax.inject.Inject

/**
 * `invitation` 태그의 엔드포인트를 호출하는 서비스. `Authorization: Bearer`는 `minoIdentityProofPlugin`이
 * 붙이므로 이 서비스는 토큰을 다루지 않고, 비2xx는 `convertDomainException`이 `MinoDomainException`으로
 * 바꿔 던지므로 여기서 잡지 않는다.
 *
 * `room` 태그의 [RoomApiService]와 나뉘어 있는 것은 의도다 — 서비스의 단위는 서버 리소스(OpenAPI 태그)이고
 * 초대는 방과 다른 태그다(`docs/adr/2026-08-28-api-service-owned-per-server-tag.md`).
 *
 * 계약은 `docs/specs/onboarding-flow/contracts/invite-link.md` §1이 소유한다.
 */
internal class InvitationApiService @Inject constructor(
    private val client: HttpClient,
) {
    /**
     * [roomId] 방의 초대 코드를 발급받는다. 요청 본문은 없고 경로 파라미터만 쓴다.
     *
     * **멱등은 서버가 보장한다** — 멤버당 초대가 하나여서 이미 발급했다면 같은 코드가 돌아오고 만료도 없으므로,
     * 이 서비스는 코드를 캐시하지도 재발급 경로를 두지도 않는다(계약 §1.1).
     *
     * 실패 응답의 `errorCode`(`NOT_ROOM_MEMBER`·`PERSONAL_ROOM_NOT_ALLOWED`·`ROOM_NOT_FOUND` 등)는 읽지 않는다.
     * 호출자가 코드별로 다르게 행동하지 않으므로 상태 코드로 매핑된 `MinoDomainException`이면 충분하고,
     * `errorCode`를 도메인 리프로 세우지도 않는다
     * (계약 §5 · `docs/adr/2026-08-28-error-body-type-and-no-error-code-leaf.md`).
     */
    suspend fun issueInvitation(roomId: String): InvitationResponse =
        client
            .post("api/v1/rooms/$roomId/invitations")
            .body<MinoResponse<InvitationResponse>>()
            .data

    /**
     * [code]가 가리키는 방을 참여 전에 미리 들여다본다. 인증이 필요 없는 조회다.
     *
     * 없는 코드는 `404`로 `MinoDomainException`이 전파된다. 여기서 잡지 않는다.
     */
    suspend fun previewInvitation(code: String): InvitationPreviewResponse =
        client
            .get("api/v1/invitations/$code")
            .body<MinoResponse<InvitationPreviewResponse>>()
            .data
}
