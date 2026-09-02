package team.mino.core.data.repository.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import team.mino.core.data.network.dto.response.RoomSummaryResponse

/**
 * 저장 여부 두 필드의 판정만 본다 — 나머지 매핑(색·타입·썸네일)은 `RoomMapperTest`가 이미 덮는다.
 *
 * `hasPlace`의 `null`을 `false`로 메우지 않는 것과, `matchedPinId`를 `hasPlace == true`에서만 남기는 것이
 * 이 파일이 지키는 두 규칙이다(`docs/specs/place-detail/contracts/place-api.md` §4.2).
 */
class RoomSummaryMapperTest {
    @Test
    fun `showHasPlaceId를 지정하지 않은 응답은 두 필드가 모두 null이다`() {
        val summary = response().toDomain()

        assertNull(summary.hasPlace)
        assertNull(summary.matchedPinId)
    }

    @Test
    fun `저장된 방은 hasPlace와 matchedPinId를 그대로 싣는다`() {
        val summary = response(hasPlace = true, matchedPinId = "pin-1").toDomain()

        assertEquals(true, summary.hasPlace)
        assertEquals("pin-1", summary.matchedPinId)
    }

    @Test
    fun `저장돼 있지 않은 방에 서버가 matchedPinId를 실어 보내도 지운다`() {
        val summary = response(hasPlace = false, matchedPinId = "pin-1").toDomain()

        assertEquals(false, summary.hasPlace)
        assertNull(summary.matchedPinId)
    }

    @Test
    fun `물어보지 않은 방에 서버가 matchedPinId를 실어 보내도 지운다`() {
        val summary = response(hasPlace = null, matchedPinId = "pin-1").toDomain()

        assertNull(summary.hasPlace)
        assertNull(summary.matchedPinId)
    }

    @Test
    fun `hasPlace가 false여도 placeCount는 건드리지 않는다`() {
        val summary = response(hasPlace = false, pinCount = 12).toDomain()

        assertEquals(12, summary.placeCount)
    }

    private fun response(
        hasPlace: Boolean? = null,
        matchedPinId: String? = null,
        pinCount: Int = 0,
    ): RoomSummaryResponse =
        RoomSummaryResponse(
            id = "r1",
            name = "맛집 탐방",
            description = null,
            type = "shared",
            color = "gray",
            ownerId = "u1",
            pinCount = pinCount,
            memberCount = 2,
            hasPlace = hasPlace,
            matchedPinId = matchedPinId,
        )
}
