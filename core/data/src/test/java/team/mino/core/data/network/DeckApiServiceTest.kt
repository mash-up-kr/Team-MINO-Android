package team.mino.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.network.service.DeckApiService

/**
 * `MinoResponse<CardFeedResponse>`는 제네릭 DTO라 봉투 벗기기가 런타임에 성립하는지 컴파일이 보증하지 않는다.
 * 그것과, 질의 파라미터가 계약대로 실리는지, 그리고 계약을 벗어난 응답을 흡수하는지를 본다 —
 * `docs/specs/home-deck-exploration/contracts/deck-api.md` §2.
 *
 * 덱의 형태별 응답은 [MIXED_LABEL_DECK_BODY] 등 픽스처가 소유한다. `DeckMockStore`가 재현하던 경우들이
 * 실서버 전환 뒤에도 검사되는 자리가 여기다.
 */
class DeckApiServiceTest {
    private fun service(engine: MockEngine): DeckApiService =
        DeckApiService(
            HttpClient(engine) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            },
        )

    @Test
    fun `봉투를 벗기고 cards를 라벨 4종 순서 그대로 반환한다`() =
        runTest {
            // 이 본문만 `room`을 함께 싣는다(계약 §2.2). 담지 않는 것과 읽다 죽는 것은 다르므로,
            // 흡수에 실패하면 카드를 세기 전에 이 단언들이 먼저 깨진다.
            val engine = jsonEngine(MIXED_LABEL_DECK_BODY)

            val cards = service(engine).getCards(ROOM_ID, SORT_GGUK_PICK)

            assertEquals(listOf("pin-1", "pin-2", "pin-3", "pin-4"), cards.map { it.id })
            assertEquals(
                listOf("worthVisiting", "manySaves", "manyComments", "manyViews"),
                cards.map { it.labelGroup },
            )
            val first = cards.first()
            assertEquals("연남동 감자탕", first.place.name)
            assertEquals("blue", first.createdBy?.avatar?.color)
            assertNull(cards[2].createdBy)
        }

    @Test
    fun `정렬은 쿼리로 싣고 좌표가 없으면 URL에 붙이지 않는다`() =
        runTest {
            var requested: HttpRequestData? = null
            val engine = jsonEngine(EMPTY_DECK_BODY) { requested = it }

            service(engine).getCards(ROOM_ID, SORT_GGUK_PICK)

            assertEquals(HttpMethod.Get, requested?.method)
            assertEquals(cardsPath(ROOM_ID), requested?.url?.encodedPath)
            assertEquals("ggukPick", requested?.url?.parameters?.get("sort"))
            assertTrue(requested?.url?.parameters?.contains("lat") == false)
            assertTrue(requested?.url?.parameters?.contains("lng") == false)
        }

    @Test
    fun `가까운순은 좌표를 함께 싣는다`() =
        runTest {
            var requested: HttpRequestData? = null
            val engine = jsonEngine(EMPTY_DECK_BODY) { requested = it }

            service(engine).getCards(ROOM_ID, SORT_NEARBY, lat = LAT, lng = LNG)

            assertEquals("nearby", requested?.url?.parameters?.get("sort"))
            assertEquals(LAT.toString(), requested?.url?.parameters?.get("lat"))
            assertEquals(LNG.toString(), requested?.url?.parameters?.get("lng"))
        }

    @Test
    fun `후보가 적은 덱은 짧은 채로 그대로 온다`() =
        runTest {
            val engine = jsonEngine(SHORT_DECK_BODY)

            val cards = service(engine).getCards(ROOM_ID, SORT_GGUK_PICK)

            assertEquals(2, cards.size)
        }

    @Test
    fun `후보가 0건이면 빈 덱이다`() =
        runTest {
            val engine = jsonEngine(EMPTY_DECK_BODY)

            val cards = service(engine).getCards(ROOM_ID, SORT_LATEST)

            assertEquals(emptyList<String>(), cards.map { it.id })
        }

    @Test
    fun `지표가 전부 0인 방도 라벨이 비지 않는다`() =
        runTest {
            val engine = jsonEngine(ALL_WORTH_VISITING_DECK_BODY)

            val cards = service(engine).getCards(ROOM_ID, SORT_GGUK_PICK)

            assertEquals(5, cards.size)
            assertTrue(cards.all { it.labelGroup == "worthVisiting" })
        }

    @Test
    fun `아바타가 색을 싣지 않아도 덱 전체가 실패하지 않는다`() =
        runTest {
            val engine = jsonEngine(AVATAR_WITHOUT_COLOR_DECK_BODY)

            val card = service(engine).getCards(ROOM_ID, SORT_GGUK_PICK).single()

            assertEquals("", card.createdBy?.avatar?.color)
        }

    @Test
    fun `필드가 빠진 카드가 있어도 덱 전체가 실패하지 않는다`() =
        runTest {
            val engine = jsonEngine(MISSING_FIELDS_DECK_BODY)

            val card = service(engine).getCards(ROOM_ID, SORT_GGUK_PICK).single()

            assertEquals("", card.id)
            assertEquals("", card.place.name)
            assertEquals("", card.labelGroup)
            assertEquals(listOf("https://img/1"), card.images)
        }

    private companion object {
        const val ROOM_ID = "room-1"

        const val SORT_GGUK_PICK = "ggukPick"
        const val SORT_LATEST = "latest"
        const val SORT_NEARBY = "nearby"

        const val LAT = 37.5563
        const val LNG = 126.9236
    }
}
