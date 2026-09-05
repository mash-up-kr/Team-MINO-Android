package team.mino.core.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import team.mino.core.data.datasource.CommentRemoteDataSource
import team.mino.core.data.network.dto.request.CommentCreateRequest
import team.mino.core.data.network.dto.response.CommentAuthorResponse
import team.mino.core.data.network.dto.response.CommentPageResponse
import team.mino.core.data.network.dto.response.CommentResponse
import team.mino.core.data.network.dto.response.PaginationResponse
import team.mino.core.errorhandling.MinoDomainException
import java.io.IOException

/**
 * `PlaceCommentRepositoryImpl`이 **더하는 규칙**만 판정한다. `hasNext`→`hasOlder`·`createdAt` 변환은
 * `PlaceCommentMapperTest`가 이미 보증하므로 여기서는 이 경계가 손대지 않기로 한 것들을 본다
 * (`docs/specs/place-detail/contracts/place-repository.md` §2).
 *
 * 1. `addComment`는 `content`를 다듬지 않는다 — 앞뒤 공백 제거는 서버 몫이다(FR-012).
 * 2. `getComments`는 요청받은 페이지를 그대로 묻고 페이지 안 순서를 뒤집지 않는다(D11).
 * 3. 실패를 흡수하지 않는다 — `Result`로 감싸지 않고 `MinoDomainException`으로 던진다.
 */
class PlaceCommentRepositoryImplTest {
    private val commentRemoteDataSource = RecordingCommentRemoteDataSource()
    private val repository = PlaceCommentRepositoryImpl(commentRemoteDataSource)

    @Test
    fun `작성 요청은 입력한 내용을 다듬지 않고 그대로 싣는다`() =
        runTest {
            repository.addComment(pinId = "pin-1", content = "  띄어쓰기 그대로  ")

            assertEquals(
                "trim을 여기서 하면 서버와 두 벌의 규칙이 생긴다",
                CommentCreateRequest(content = "  띄어쓰기 그대로  "),
                commentRemoteDataSource.lastCreateRequest,
            )
        }

    @Test
    fun `작성 응답을 도메인 코멘트로 돌려준다`() =
        runTest {
            commentRemoteDataSource.createdComment = commentResponse(id = "c-new", content = "새 코멘트")

            val comment = repository.addComment(pinId = "pin-1", content = "새 코멘트")

            // 목록을 다시 조회하지 않으려고 만들어진 항목을 돌려받는다(FR-014).
            assertEquals("c-new", comment.id)
            assertEquals("새 코멘트", comment.content)
        }

    @Test
    fun `조회는 요청받은 페이지를 그대로 묻는다`() =
        runTest {
            repository.getComments(pinId = "pin-1", page = 2)

            assertEquals("pin-1", commentRemoteDataSource.lastPinId)
            assertEquals("페이지 선택은 호출자의 몫이라 여기서 보정하지 않는다", 2, commentRemoteDataSource.lastPage)
        }

    @Test
    fun `페이지 안 순서를 뒤집지 않는다`() =
        runTest {
            commentRemoteDataSource.page =
                CommentPageResponse(
                    data = listOf(commentResponse(id = "c1"), commentResponse(id = "c2")),
                    pagination = PaginationResponse(page = 0, pageSize = 20, hasNext = true),
                )

            val page = repository.getComments(pinId = "pin-1", page = 0)

            assertEquals(listOf("c1", "c2"), page.comments.map { it.id })
        }

    @Test
    fun `삭제 실패를 흡수하지 않는다`() =
        runTest {
            val failure = MinoDomainException.Http(code = 403, cause = IOException())
            commentRemoteDataSource.deleteFailure = failure

            val thrown =
                try {
                    repository.deleteComment(pinId = "pin-1", commentId = "c1")
                    null
                } catch (e: MinoDomainException) {
                    e
                }

            assertSame("지워지지 않은 것이 지워진 것으로 보이면 안 된다", failure, thrown)
        }

    private fun commentResponse(
        id: String,
        content: String = "코멘트",
    ): CommentResponse =
        CommentResponse(
            id = id,
            content = content,
            createdAt = "2026-09-01T12:00:00Z",
            author = CommentAuthorResponse(id = "user-1", nickname = "미노"),
            canDelete = true,
        )

    private inner class RecordingCommentRemoteDataSource : CommentRemoteDataSource {
        var page: CommentPageResponse =
            CommentPageResponse(pagination = PaginationResponse(page = 0, pageSize = 20, hasNext = false))
        var createdComment: CommentResponse = commentResponse(id = "c-default")
        var deleteFailure: Throwable? = null

        var lastPinId: String? = null
            private set

        var lastPage: Int? = null
            private set

        var lastCreateRequest: CommentCreateRequest? = null
            private set

        override suspend fun getComments(
            pinId: String,
            page: Int,
        ): CommentPageResponse {
            lastPinId = pinId
            lastPage = page
            return this.page
        }

        override suspend fun createComment(
            pinId: String,
            request: CommentCreateRequest,
        ): CommentResponse {
            lastPinId = pinId
            lastCreateRequest = request
            return createdComment
        }

        override suspend fun deleteComment(
            pinId: String,
            commentId: String,
        ) {
            lastPinId = pinId
            deleteFailure?.let { throw it }
        }
    }
}
