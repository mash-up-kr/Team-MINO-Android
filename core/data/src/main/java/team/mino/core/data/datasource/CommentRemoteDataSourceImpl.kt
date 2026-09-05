package team.mino.core.data.datasource

import team.mino.core.data.network.dto.request.CommentCreateRequest
import team.mino.core.data.network.dto.response.CommentPageResponse
import team.mino.core.data.network.dto.response.CommentResponse
import team.mino.core.data.network.service.CommentApiService
import javax.inject.Inject

/**
 * [CommentRemoteDataSource]의 유일한 구현. `CommentApiService`(Ktor) 호출을 그대로 위임한다 —
 * 변환·비즈니스 로직은 두지 않는다(`core/data/README.md` §5).
 */
internal class CommentRemoteDataSourceImpl @Inject constructor(
    private val service: CommentApiService,
) : CommentRemoteDataSource {
    override suspend fun getComments(
        pinId: String,
        page: Int,
    ): CommentPageResponse = service.getComments(pinId, page)

    override suspend fun createComment(
        pinId: String,
        request: CommentCreateRequest,
    ): CommentResponse = service.createComment(pinId, request)

    override suspend fun deleteComment(
        pinId: String,
        commentId: String,
    ) = service.deleteComment(pinId, commentId)
}
