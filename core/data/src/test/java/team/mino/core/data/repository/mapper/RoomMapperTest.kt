package team.mino.core.data.repository.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.network.dto.response.RoomSummaryResponse
import team.mino.core.domain.model.RoomThumbnail
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class RoomMapperTest {
    @Test
    fun `type이 personal이면 isPersonal이 true다`() {
        val room = response(type = "personal").toDomain()

        assertTrue(room.isPersonal)
    }

    @Test
    fun `type이 personal이 아니면 isPersonal이 false다`() {
        val room = response(type = "group").toDomain()

        assertEquals(false, room.isPersonal)
    }

    @Test
    fun `pinCount가 0이면 색상+캐릭터 썸네일이다`() {
        val room = response(pinCount = 0, color = "RED").toDomain()

        assertEquals(RoomThumbnail.ColorAndCharacter(color = "RED"), room.thumbnail)
    }

    @Test
    fun `pinCount가 1 이상이면 콜라주 썸네일이고 최대 4장으로 잘린다`() {
        val room = response(pinCount = 10).toDomain()

        val thumbnail = room.thumbnail as RoomThumbnail.Collage
        assertEquals(4, thumbnail.imageUrls.size)
    }

    @Test
    fun `pinCount가 1이면 콜라주 이미지가 1장이다`() {
        val room = response(pinCount = 1).toDomain()

        val thumbnail = room.thumbnail as RoomThumbnail.Collage
        assertEquals(1, thumbnail.imageUrls.size)
    }

    @Test
    fun `멤버가 4명 이하면 전부 보이고 overflow가 없다`() {
        val room = response(memberCount = 4).toDomain()

        assertEquals(4, room.memberSummary.visibleAvatarUrls.size)
        assertEquals(0, room.memberSummary.overflowCount)
    }

    @Test
    fun `멤버가 5명 이상이면 아바타 3개와 overflow로 나뉜다`() {
        val room = response(memberCount = 7).toDomain()

        assertEquals(3, room.memberSummary.visibleAvatarUrls.size)
        assertEquals(4, room.memberSummary.overflowCount)
    }

    @Test
    fun `draft API에 없는 필드는 플레이스홀더로 채운다`() {
        val room = response().toDomain()

        assertEquals(null, room.lastPlaceSavedAt)
        assertEquals(0, room.commentCount)
    }

    private fun response(
        type: String = "group",
        color: String? = null,
        pinCount: Int = 0,
        memberCount: Int = 1,
    ): RoomSummaryResponse =
        RoomSummaryResponse(
            id = "room-1",
            type = type,
            name = "테스트 방",
            description = null,
            color = color,
            ownerId = "owner-1",
            inviteCode = "invite-1",
            createdAt = "2026-08-25T00:00:00Z",
            pinCount = pinCount,
            memberCount = memberCount,
        )
}
