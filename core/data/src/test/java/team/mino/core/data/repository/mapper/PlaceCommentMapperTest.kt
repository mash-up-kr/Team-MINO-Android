@file:OptIn(ExperimentalTime::class)

package team.mino.core.data.repository.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.network.dto.response.AvatarResponse
import team.mino.core.data.network.dto.response.CommentAuthorResponse
import team.mino.core.data.network.dto.response.CommentPageResponse
import team.mino.core.data.network.dto.response.CommentResponse
import team.mino.core.data.network.dto.response.PaginationResponse
import team.mino.core.domain.model.RoomColor
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 이 매퍼가 지키는 것은 넷이다 — `hasNext`를 `hasOlder`로 뒤집어 드는 것, 페이지 안 순서를 건드리지 않는 것,
 * `createdAt`을 절대 시각으로만 옮기는 것(표기 환산 없음), 그리고 어긋난 유저 식별자 키·팔레트 밖 아바타 색을
 * 흡수하는 것이다.
 */
class PlaceCommentMapperTest {
    @Test
    fun `서버 hasNext를 hasOlder로 바꿔 든다`() {
        val page = pageResponse(hasNext = true).toDomain()

        assertTrue(page.hasOlder)
        assertEquals(0, page.page)
    }

    @Test
    fun `더 받을 이전 페이지가 없으면 hasOlder가 false다`() {
        assertFalse(pageResponse(hasNext = false).toDomain().hasOlder)
    }

    @Test
    fun `페이지 안 순서를 뒤집지 않는다`() {
        val page =
            pageResponse(
                comments =
                    listOf(
                        commentResponse(id = "c1", createdAt = "2026-09-01T10:00:00Z"),
                        commentResponse(id = "c2", createdAt = "2026-09-01T09:00:00Z"),
                    ),
            ).toDomain()

        assertEquals(listOf("c1", "c2"), page.comments.map { it.id })
    }

    @Test
    fun `createdAt을 절대 시각 그대로 옮긴다`() {
        val comment = commentResponse(createdAt = "2026-09-01T12:34:56Z").toDomain()

        assertEquals(Instant.parse("2026-09-01T12:34:56Z"), comment.createdAt)
    }

    @Test
    fun `작성자 식별자는 서버 id를 userId로 옮긴다`() {
        val comment = commentResponse(authorId = "u-1").toDomain()

        assertEquals("u-1", comment.author.userId)
    }

    @Test
    fun `아바타 색을 13색 팔레트로 읽는다`() {
        val comment = commentResponse(avatarColor = "light_blue").toDomain()

        assertEquals(RoomColor.LIGHT_BLUE, comment.author.avatarColor)
    }

    @Test
    fun `팔레트에 없는 아바타 색은 null로 떨어뜨린다`() {
        assertNull(commentResponse(avatarColor = "chartreuse").toDomain().author.avatarColor)
    }

    @Test
    fun `아바타가 없으면 avatarColor는 null이다`() {
        assertNull(commentResponse(avatar = null).toDomain().author.avatarColor)
    }

    private fun pageResponse(
        comments: List<CommentResponse> = listOf(commentResponse()),
        page: Int = 0,
        hasNext: Boolean = false,
    ): CommentPageResponse =
        CommentPageResponse(
            data = comments,
            pagination = PaginationResponse(page = page, pageSize = 20, hasNext = hasNext),
        )

    private fun commentResponse(
        id: String = "c1",
        createdAt: String = "2026-09-01T12:00:00Z",
        authorId: String = "u1",
        avatarColor: String = "red",
        avatar: AvatarResponse? = AvatarResponse(color = avatarColor),
    ): CommentResponse =
        CommentResponse(
            id = id,
            content = "여기 진짜 맛있어요",
            createdAt = createdAt,
            author = CommentAuthorResponse(id = authorId, nickname = "지은", avatar = avatar),
            canDelete = true,
        )
}
