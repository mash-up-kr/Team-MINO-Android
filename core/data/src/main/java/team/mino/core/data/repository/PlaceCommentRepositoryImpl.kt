package team.mino.core.data.repository

import team.mino.core.data.datasource.CommentRemoteDataSource
import team.mino.core.data.network.dto.request.CommentCreateRequest
import team.mino.core.data.repository.mapper.toDomain
import team.mino.core.domain.model.PlaceComment
import team.mino.core.domain.model.PlaceCommentPage
import team.mino.core.domain.repository.PlaceCommentRepository
import javax.inject.Inject

/**
 * [PlaceCommentRepository]의 구현 — 계약은 `docs/specs/place-detail/contracts/place-repository.md` §2가
 * 소유한다.
 *
 * 페이지네이션 상태를 갖지 않는다. 다음에 어느 페이지를 부를지 정하는 것은 호출자이며, 이 클래스는 요청받은
 * 페이지를 그대로 옮긴다. 순서도 건드리지 않는다 — 역방향 페이징을 화면 순서로 뒤집는 자리는 없다
 * (`docs/specs/place-detail/research.md` D11).
 *
 * 예외를 잡지 않는다 — 매핑은 `HttpClient`의 `convertDomainException`이 전역 수행하고 실패는
 * `MinoDomainException`으로 전파된다(`core/data/README.md` §6).
 */
internal class PlaceCommentRepositoryImpl @Inject constructor(
    private val commentRemoteDataSource: CommentRemoteDataSource,
) : PlaceCommentRepository {
    /**
     * `pageSize`를 싣지 않아 서버 기본값(20)을 쓴다. 페이지 크기를 도메인이 정하면 서버 기본값이 바뀔 때
     * 두 곳이 어긋난다.
     */
    override suspend fun getComments(
        pinId: String,
        page: Int,
    ): PlaceCommentPage = commentRemoteDataSource.getComments(pinId, page).toDomain()

    /**
     * [content]를 다듬지 않는다 — 앞뒤 공백 제거는 서버가 하고 200자 상한은 입력 단계에서 막힌다(FR-012).
     * 여기서 `trim()`을 더하면 서버와 두 벌의 규칙이 생긴다.
     *
     * 만들어진 코멘트를 돌려주므로 목록을 다시 조회하지 않는다(FR-014).
     */
    override suspend fun addComment(
        pinId: String,
        content: String,
    ): PlaceComment = commentRemoteDataSource.createComment(pinId, CommentCreateRequest(content = content)).toDomain()

    /** 권한을 판정하지 않는다 — 호출 자체가 `canDelete == true`인 코멘트에서만 일어난다(EC-013, D6). */
    override suspend fun deleteComment(
        pinId: String,
        commentId: String,
    ) = commentRemoteDataSource.deleteComment(pinId, commentId)
}
