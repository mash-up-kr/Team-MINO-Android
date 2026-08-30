package team.mino.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.PlaceDetail
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.model.RoomType

/**
 * 핀의 `roomId`와 방 목록을 짝지어 색을 정하는 규칙만 본다(FR-002 · `research.md` D15).
 *
 * **없는 색을 지어내지 않는지**가 이 테스트의 핵심이다. 짝이 없거나 재료가 덜 도착한 경우가 모두 `null`로
 * 수렴해야 하며, 그 `null`을 화면이 "마커를 그리지 않음"으로 읽는다.
 */
class ResolvePlaceRoomColorUseCaseTest {
    private val resolvePlaceRoomColor = ResolvePlaceRoomColorUseCase()

    @Test
    fun `핀이 속한 방의 색을 돌려준다`() {
        val color =
            resolvePlaceRoomColor(
                place = placeDetail(roomId = "room-2"),
                rooms = listOf(room("room-1", RoomColor.RED), room("room-2", RoomColor.CYAN)),
            )

        assertEquals(RoomColor.CYAN, color)
    }

    @Test
    fun `목록에 짝이 되는 방이 없으면 기본색을 만들지 않고 null이다`() {
        val color =
            resolvePlaceRoomColor(
                place = placeDetail(roomId = "room-gone"),
                rooms = listOf(room("room-1", RoomColor.RED)),
            )

        assertNull(color)
    }

    @Test
    fun `방 목록이 아직 없으면 null이다`() {
        val color = resolvePlaceRoomColor(place = placeDetail(roomId = "room-1"), rooms = emptyList())

        assertNull(color)
    }

    @Test
    fun `핀 상세가 아직 없으면 null이다`() {
        val color = resolvePlaceRoomColor(place = null, rooms = listOf(room("room-1", RoomColor.RED)))

        assertNull(color)
    }

    private fun placeDetail(roomId: String): PlaceDetail =
        PlaceDetail(
            pinId = "pin-1",
            roomId = roomId,
            placeId = "place-1",
            name = "성수동 카페",
            address = "서울 성동구",
            location = GeoPoint(latitude = 37.5, longitude = 127.0),
            imageUrls = emptyList(),
            registrant = null,
            sourceUrl = null,
            mapUrl = null,
        )

    private fun room(
        id: String,
        color: RoomColor,
    ): RoomSummary =
        RoomSummary(
            id = id,
            name = "민호야 잘하자",
            description = "",
            type = RoomType.GROUP,
            color = color,
            placeCount = 0,
            thumbnailImageUrls = emptyList(),
        )
}
