package team.mino.core.data.datasource

import team.mino.core.data.network.dto.request.CommentCreateRequest
import team.mino.core.data.network.dto.response.CommentPageResponse
import team.mino.core.data.network.dto.response.CommentResponse

/**
 * 장소 코멘트의 원격 출처. 계약은 `docs/specs/place-detail/contracts/comment-api.md`가 소유한다.
 *
 * 핀 자체를 다루는 [PinRemoteDataSource]와 가르는 기준은 생애다 — 코멘트는 핀 상세와 따로 조회·추가·삭제되고
 * 페이지 단위로 더 받는다(`docs/specs/place-detail/research.md` D8).
 *
 * 소비자는 같은 모듈의 `PlaceCommentRepositoryImpl` 하나뿐이며, `internal`로 닫혀 있어 전송용 DTO가
 * 도메인 표면에 올라가지 않는다.
 */
internal interface CommentRemoteDataSource {
    /**
     * [pinId] 핀의 코멘트 [page]쪽을 가져온다.
     *
     * **이 함수만 봉투를 벗긴 알맹이가 아니라 [CommentPageResponse] 전체를 돌려준다.** 서버가 `data`와
     * 나란히 `pagination`을 싣고, 도메인이 그 `hasNext`를 읽기 때문이다 — 사정은 그 DTO의 KDoc에 있다.
     *
     * [page] 0이 최신 페이지다. 역방향 페이징을 화면 순서로 뒤집지 않는다(같은 계약 §1.2).
     */
    suspend fun getComments(
        pinId: String,
        page: Int,
    ): CommentPageResponse

    /**
     * [pinId] 핀에 코멘트를 남기고 만들어진 코멘트를 돌려준다(FR-014).
     *
     * 돌려받은 것을 목록 끝에 붙이고 목록을 다시 조회하지 않는다 — 그 판단은 위 레이어의 몫이고 이 함수는
     * 만들어진 항목을 흘리기만 한다.
     */
    suspend fun createComment(
        pinId: String,
        request: CommentCreateRequest,
    ): CommentResponse

    /**
     * [pinId] 핀의 [commentId] 코멘트를 지운다.
     *
     * 되돌리기 수단이 없어 삭제된 항목을 돌려받을 이유가 없다(EC-013). **권한을 여기서 따지지 않는다** —
     * 호출 자체가 `canDelete == true`인 코멘트에서만 일어난다.
     */
    suspend fun deleteComment(
        pinId: String,
        commentId: String,
    )
}
