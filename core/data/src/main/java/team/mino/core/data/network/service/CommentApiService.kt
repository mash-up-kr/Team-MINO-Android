package team.mino.core.data.network.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import team.mino.core.data.network.dto.request.CommentCreateRequest
import team.mino.core.data.network.dto.response.CommentPageResponse
import team.mino.core.data.network.dto.response.CommentResponse
import team.mino.core.data.network.dto.response.MinoResponse
import javax.inject.Inject

/**
 * 장소 코멘트 엔드포인트를 호출하는 서비스. 계약은
 * `docs/specs/place-detail/contracts/comment-api.md`가 소유한다.
 *
 * 핀 자체를 다루는 [PinApiService]와 가르는 기준은 생애다 — 코멘트는 핀 상세와 따로 조회·추가·삭제되고
 * 페이지 단위로 더 받는다(`docs/specs/place-detail/research.md` D8).
 *
 * 신원 증명 헤더는 `MinoIdentityProofPlugin`이 싣고, 예외는 `convertDomainException`이
 * `MinoDomainException`으로 바꿔 던지므로 여기서 잡지 않는다.
 */
internal class CommentApiService @Inject constructor(
    private val client: HttpClient,
) {
    /**
     * [pinId] 핀의 코멘트 [page]쪽을 가져온다 — 같은 계약 §1.
     *
     * **이 함수만 [MinoResponse]로 봉투를 벗기지 않는다.** 서버가 `data`와 **나란히** `pagination`을
     * 싣기 때문에(`{ "data": [...], "pagination": {...} }`) 알맹이만 남기면 `hasNext`가 사라진다.
     * [CommentPageResponse]로 본문 전체를 받는 이유와 봉투 ADR
     * (`docs/adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md`)과의 관계는 그 DTO의 KDoc에 있다.
     *
     * **`pageSize`를 싣지 않는다.** 서버 기본값 20을 쓴다(같은 계약 §1.2).
     *
     * [page] 0이 최신 페이지다. 역방향 페이징을 화면 순서로 뒤집는 것은 이 서비스가 하지 않는다.
     */
    suspend fun getComments(
        pinId: String,
        page: Int,
    ): CommentPageResponse =
        client
            .get("api/v1/pins/$pinId/comments") {
                parameter("page", page)
            }.body<CommentPageResponse>()

    /**
     * [pinId] 핀에 코멘트를 남기고 만들어진 코멘트를 돌려준다 — 같은 계약 §2.
     *
     * 응답 `201`의 `data`가 목록 항목과 같은 스키마여서 [CommentResponse]를 그대로 쓴다. 단건이라 여기서는
     * 봉투를 벗긴다 — `pagination`이 함께 오는 것은 목록뿐이다.
     *
     * 돌려받은 것을 목록 끝에 붙이고 목록을 다시 조회하지 않는다(FR-014). 그 판단은 위 레이어의 몫이고
     * 이 서비스는 만들어진 항목을 흘리기만 한다.
     */
    suspend fun createComment(
        pinId: String,
        request: CommentCreateRequest,
    ): CommentResponse =
        client
            .post("api/v1/pins/$pinId/comments") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<MinoResponse<CommentResponse>>()
            .data

    /**
     * [pinId] 핀의 [commentId] 코멘트를 지운다 — 같은 계약 §3.
     *
     * 응답 본문을 읽지 않아 반환값이 없다. 되돌리기 수단이 없어 삭제된 항목을 돌려받을 이유가 없다(EC-013).
     *
     * **권한을 여기서 따지지 않는다.** 호출 자체가 `canDelete == true`인 코멘트에서만 일어난다.
     */
    suspend fun deleteComment(
        pinId: String,
        commentId: String,
    ) {
        client.delete("api/v1/pins/$pinId/comments/$commentId")
    }
}
