package team.mino.core.domain.repository

import team.mino.core.domain.model.PlaceComment
import team.mino.core.domain.model.PlaceCommentPage

/**
 * 장소 코멘트의 조회·작성·삭제 계약.
 *
 * 세 함수 모두 1회성 요청이라 `Flow`를 흘리지 않으며, 실패를 `Result`로 감싸지 않고 `MinoDomainException`으로
 * 던진다. 취소는 그대로 전파한다.
 *
 * 페이지네이션 상태를 갖지 않는다 — 다음에 어느 페이지를 요청할지는 호출자가 정한다.
 */
interface PlaceCommentRepository {
    /**
     * 코멘트 한 페이지를 가져온다.
     *
     * **[page] 0이 최신 페이지다.** 서버가 역방향 페이징이라 페이지 번호가 커질수록 더 오래된 코멘트가 온다.
     * 페이지 안은 오래된 것이 먼저 오며, **이 계약은 그 순서를 뒤집지 않는다** — 페이지 사이의 배치는 화면이
     * 정한다(`docs/specs/place-detail/research.md` D11).
     *
     * `pageSize`를 지정하지 않고 서버 기본값을 쓴다.
     *
     * [PlaceCommentPage.hasOlder]는 "더 받을 이전 페이지가 있는지"만 말한다. 전체 개수나 페이지 수를
     * 도메인에 올리지 않는다.
     */
    suspend fun getComments(
        pinId: String,
        page: Int,
    ): PlaceCommentPage

    /**
     * 코멘트를 남기고 **만들어진 코멘트를 돌려준다**(FR-014).
     *
     * 반환값이 있는 것은 목록을 다시 조회하지 않기 위해서다. 돌려받은 항목을 어디에 붙일지는 화면이 정한다.
     *
     * [content]를 이 계약이 다듬지 않는다. 앞뒤 공백 제거는 서버가 하고, 200자 상한은 입력 단계에서
     * 이미 막힌다(FR-012).
     */
    suspend fun addComment(
        pinId: String,
        content: String,
    ): PlaceComment

    /**
     * 코멘트를 지운다(FR-015).
     *
     * 반환값이 없다. 되돌리기 수단을 두지 않으므로 삭제된 항목을 돌려줄 이유가 없다(EC-013).
     *
     * **권한을 판정하지 않는다.** 호출 자체가 [PlaceComment.canDelete]가 `true`인 코멘트에서만 일어난다
     * (`docs/specs/place-detail/research.md` D6).
     */
    suspend fun deleteComment(
        pinId: String,
        commentId: String,
    )
}
