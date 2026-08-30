package team.mino.core.data.datasource

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import team.mino.core.data.network.EMPTY_DECK_BODY
import team.mino.core.data.network.jsonEngine
import team.mino.core.data.network.service.DeckApiService
import team.mino.core.domain.model.DeckSort

/**
 * 도메인 [DeckSort]가 서버 문자열로 옮겨지는 자리는 여기뿐이다 —
 * `docs/specs/home-deck-exploration/contracts/deck-api.md` §2.1. 틀리면 홈이 고른 정렬과 다른 덱이
 * 조용히 내려오므로 세 값을 모두 확인한다.
 */
class DeckRemoteDataSourceImplTest {
    @Test
    fun `정렬 3종이 각각의 서버 문자열로 나간다`() =
        runTest {
            val sent =
                DeckSort.entries.map { sort ->
                    var requested: HttpRequestData? = null
                    val dataSource = dataSource { requested = it }

                    dataSource.getCards(ROOM_ID, sort, lat = LAT, lng = LNG)

                    requested?.url?.parameters?.get("sort")
                }

            assertEquals(listOf("ggukPick", "latest", "nearby"), sent)
        }

    @Test
    fun `roomId를 경로에 넣어 요청한다`() =
        runTest {
            var requested: HttpRequestData? = null
            val dataSource = dataSource { requested = it }

            dataSource.getCards(ROOM_ID, DeckSort.GGUK_PICK)

            assertEquals("/api/v1/rooms/$ROOM_ID/cards", requested?.url?.encodedPath)
        }

    private fun dataSource(onRequest: (HttpRequestData) -> Unit): DeckRemoteDataSourceImpl =
        DeckRemoteDataSourceImpl(
            DeckApiService(
                HttpClient(jsonEngine(EMPTY_DECK_BODY, onRequest = onRequest)) {
                    expectSuccess = true
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                },
            ),
        )

    private companion object {
        const val ROOM_ID = "room-1"

        const val LAT = 37.5563
        const val LNG = 126.9236
    }
}
