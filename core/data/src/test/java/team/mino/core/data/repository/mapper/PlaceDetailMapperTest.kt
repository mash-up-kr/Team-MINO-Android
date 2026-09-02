package team.mino.core.data.repository.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.network.dto.response.AvatarResponse
import team.mino.core.data.network.dto.response.PinDetailCreatedByResponse
import team.mino.core.data.network.dto.response.PinDetailResponse
import team.mino.core.data.network.dto.response.PlaceResponse
import team.mino.core.domain.model.RoomColor

/**
 * 이 매퍼가 지키는 것은 넷이다 — 핀 식별자와 장소 식별자를 각자 제자리에서 드는 것, 방 식별자를 흘리지 않는 것,
 * 비어 있는 값을 매퍼가 메우지 않는 것, 그리고 어긋난 유저 식별자 키·팔레트 밖 아바타 색을 흡수하는 것이다.
 */
class PlaceDetailMapperTest {
    @Test
    fun `핀 식별자와 장소 식별자를 각각 제자리에서 든다`() {
        val detail = pinDetailResponse(pinId = "pin-1", placeId = "place-1").toDomain()

        assertEquals("pin-1", detail.pinId)
        assertEquals("place-1", detail.placeId)
    }

    @Test
    fun `방 식별자를 함께 옮긴다`() {
        assertEquals("room-1", pinDetailResponse(roomId = "room-1").toDomain().roomId)
    }

    @Test
    fun `좌표를 GeoPoint로 옮긴다`() {
        val location = pinDetailResponse(lat = 37.5665, lng = 126.9780).toDomain().location

        assertEquals(37.5665, location.latitude, 0.0)
        assertEquals(126.9780, location.longitude, 0.0)
    }

    @Test
    fun `이미지가 비면 빈 목록 그대로 둔다`() {
        assertTrue(pinDetailResponse(images = emptyList()).toDomain().imageUrls.isEmpty())
    }

    @Test
    fun `출처 링크와 지도 링크가 없으면 그대로 null이다`() {
        val detail = pinDetailResponse(sourceUrl = null, mapUrl = null).toDomain()

        assertNull(detail.sourceUrl)
        assertNull(detail.mapUrl)
    }

    @Test
    fun `등록자가 없으면 registrant가 null이다`() {
        assertNull(pinDetailResponse(createdBy = null).toDomain().registrant)
    }

    @Test
    fun `등록자 식별자는 서버 userId를 그대로 옮긴다`() {
        val registrant = pinDetailResponse(userId = "u-1").toDomain().registrant

        assertEquals("u-1", registrant?.userId)
        assertEquals("지은", registrant?.nickname)
    }

    @Test
    fun `아바타 색을 13색 팔레트로 읽는다`() {
        assertEquals(
            RoomColor.LIGHT_BLUE,
            pinDetailResponse(avatarColor = "light_blue").toDomain().registrant?.avatarColor,
        )
    }

    @Test
    fun `팔레트에 없는 아바타 색은 null로 떨어뜨린다`() {
        assertNull(pinDetailResponse(avatarColor = "chartreuse").toDomain().registrant?.avatarColor)
    }

    @Test
    fun `아바타가 없어도 등록자는 남는다`() {
        val registrant = pinDetailResponse(avatar = null).toDomain().registrant

        assertEquals("지은", registrant?.nickname)
        assertNull(registrant?.avatarColor)
    }

    private fun pinDetailResponse(
        pinId: String = "pin-1",
        roomId: String = "room-1",
        placeId: String = "place-1",
        lat: Double = 37.5665,
        lng: Double = 126.9780,
        mapUrl: String? = "https://map.kakao.com/1",
        images: List<String> = listOf("https://img/1.jpg"),
        sourceUrl: String? = "https://blog/1",
        userId: String = "u1",
        avatarColor: String = "red",
        avatar: AvatarResponse? = AvatarResponse(color = avatarColor),
        createdBy: PinDetailCreatedByResponse? =
            PinDetailCreatedByResponse(userId = userId, nickname = "지은", avatar = avatar),
    ): PinDetailResponse =
        PinDetailResponse(
            id = pinId,
            roomId = roomId,
            place =
                PlaceResponse(
                    id = placeId,
                    provider = "kakao",
                    providerPlaceId = "kakao-1",
                    name = "성수동 카페",
                    address = "서울 성동구 연무장길 1",
                    lat = lat,
                    lng = lng,
                    mapUrl = mapUrl,
                    createdAt = "2026-09-01T12:00:00Z",
                    updatedAt = "2026-09-01T12:00:00Z",
                ),
            images = images,
            createdBy = createdBy,
            createdAt = "2026-09-01T12:00:00Z",
            sourceUrl = sourceUrl,
        )
}
