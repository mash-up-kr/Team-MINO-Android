package team.mino.core.data.repository.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import team.mino.core.data.network.dto.response.RoomResponse
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomDraft

/**
 * 색 식별자 표가 서버 `enum`과 어긋나지 않는지 확인한다.
 *
 * `enum` 밖의 값을 보내면 서버가 요청을 거절하므로 이 표의 어긋남은 컴파일이 아니라 런타임 실패로 나타난다.
 * 아래 [SERVER_ENUM]은 `RoomMapper`의 표에서 파생하지 않고 contracts/room-api.md §2의 13색을 손으로 옮긴 것이다 —
 * 두 표가 같은 출처를 보면 어긋남을 잡을 수 없다.
 */
class RoomMapperTest {
    private companion object {
        /** contracts/room-api.md §2 「색 어휘 (서버 `enum` · 확정)」 표. 서버 스키마의 선언 순서 그대로다. */
        val SERVER_ENUM: List<Pair<RoomColor, String>> =
            listOf(
                RoomColor.RED to "red",
                RoomColor.RED_ORANGE to "red_orange",
                RoomColor.ORANGE to "orange",
                RoomColor.LIME to "lime",
                RoomColor.GREEN to "green",
                RoomColor.CYAN to "cyan",
                RoomColor.VIOLET to "violet",
                RoomColor.PINK to "pink",
                RoomColor.BLUE to "blue",
                RoomColor.BROWN to "brown",
                RoomColor.LIGHT_BLUE to "light_blue",
                RoomColor.PURPLE to "purple",
                RoomColor.GRAY to "gray",
            )
    }

    private fun draft(color: RoomColor?) = RoomDraft(name = "맛집 탐방", description = "동네 맛집", color = color)

    private fun response(color: String) =
        RoomResponse(
            id = "r1",
            name = "맛집 탐방",
            description = "동네 맛집",
            color = color,
            ownerId = "u1",
        )

    @Test
    fun `13색이 서버 enum과 같은 식별자로 나간다`() {
        val actual = SERVER_ENUM.map { (color, _) -> color to draft(color).toRequest().color }

        assertEquals(SERVER_ENUM, actual)
    }

    @Test
    fun `표가 도메인 색 전부를 덮는다`() {
        assertEquals(RoomColor.entries.toList(), SERVER_ENUM.map { it.first })
    }

    @Test
    fun `식별자에 중복이 없다`() {
        val identifiers = SERVER_ENUM.map { it.second }

        assertEquals(identifiers.size, identifiers.toSet().size)
    }

    @Test
    fun `색을 고르지 않으면 gray로 확정해 보낸다`() {
        assertEquals("gray", draft(color = null).toRequest().color)
    }

    @Test
    fun `13색을 서버 식별자에서 다시 읽는다`() {
        val actual = SERVER_ENUM.map { (_, identifier) -> response(identifier).toDomain().color to identifier }

        assertEquals(SERVER_ENUM, actual)
    }

    @Test
    fun `모르는 식별자는 GRAY로 읽는다`() {
        assertEquals(RoomColor.GRAY, response("#FF0000").toDomain().color)
        assertEquals(RoomColor.GRAY, response("grey").toDomain().color)
        assertEquals(RoomColor.GRAY, response("").toDomain().color)
    }

    @Test
    fun `대소문자가 다른 식별자는 아는 색으로 치지 않는다`() {
        assertEquals(RoomColor.GRAY, response("RED").toDomain().color)
    }
}
