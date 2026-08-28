package team.mino.core.data.repository.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import team.mino.core.data.network.dto.response.CardAvatarResponse
import team.mino.core.data.network.dto.response.CardCreatedByResponse
import team.mino.core.data.network.dto.response.CardPlaceResponse
import team.mino.core.data.network.dto.response.CardResponse
import team.mino.core.domain.model.PlaceLabel

/**
 * 서버 값이 계약을 벗어났을 때 덱 전체를 실패시키지 않고 흡수하는지 본다 —
 * `docs/specs/home-deck-exploration/contracts/deck-api.md` §2.2.
 */
class DeckMapperTest {
    @Test
    fun `labelGroup 4종이 각각 대응하는 PlaceLabel로 읽힌다`() {
        val labels =
            listOf("worthVisiting", "manySaves", "manyComments", "manyViews")
                .map { cardResponse(labelGroup = it).toDomain().label }

        assertEquals(
            listOf(
                PlaceLabel.WORTH_VISITING,
                PlaceLabel.MANY_SAVES,
                PlaceLabel.MANY_COMMENTS,
                PlaceLabel.MANY_VIEWS,
            ),
            labels,
        )
    }

    @Test
    fun `모르는 labelGroup은 가볼 만한 곳으로 읽는다`() {
        val card = cardResponse(labelGroup = "someNewLabelFromServer").toDomain()

        assertEquals(PlaceLabel.WORTH_VISITING, card.label)
    }

    @Test
    fun `등록자가 없으면 빈 등록자로 읽는다`() {
        val registrant = cardResponse(createdBy = null).toDomain().registrant

        assertEquals("", registrant.userId)
        assertEquals("", registrant.nickname)
        assertNull(registrant.avatarId)
    }

    @Test
    fun `아바타를 고르지 않은 등록자는 avatarId만 비어 있다`() {
        val registrant =
            cardResponse(
                createdBy = CardCreatedByResponse(userId = "user-1", nickname = "구구", avatar = null),
            ).toDomain().registrant

        assertEquals("user-1", registrant.userId)
        assertEquals("구구", registrant.nickname)
        assertNull(registrant.avatarId)
    }

    @Test
    fun `카드의 나머지 값은 그대로 옮긴다`() {
        val card = cardResponse().toDomain()

        assertEquals("pin-1", card.pinId)
        assertEquals("연남동 감자탕", card.placeName)
        assertEquals("서울 마포구 연남로 21", card.address)
        assertEquals(listOf("https://image/1", "https://image/2"), card.imageUrls)
        assertEquals(7, card.registrant.avatarId)
    }

    private fun cardResponse(
        labelGroup: String = "worthVisiting",
        createdBy: CardCreatedByResponse? =
            CardCreatedByResponse(userId = "user-1", nickname = "구구", avatar = CardAvatarResponse(id = 7)),
    ): CardResponse =
        CardResponse(
            id = "pin-1",
            place = CardPlaceResponse(name = "연남동 감자탕", address = "서울 마포구 연남로 21"),
            images = listOf("https://image/1", "https://image/2"),
            createdBy = createdBy,
            labelGroup = labelGroup,
        )
}
