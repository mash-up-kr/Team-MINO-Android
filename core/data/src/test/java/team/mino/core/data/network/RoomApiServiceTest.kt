package team.mino.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import team.mino.core.data.network.service.RoomApiService

/**
 * `MinoResponse<List<RoomSummaryResponse>>`는 제네릭 DTO라 실제로 직렬화기가 해결되는지는 컴파일이 보증하지 않는다.
 * 봉투 벗기기가 런타임에 성립하는지와, 서버가 아직 내려주지 않는 필드가 기본값으로 읽히는지를 확인한다.
 */
class RoomApiServiceTest {
    private fun service(engine: MockEngine): RoomApiService =
        RoomApiService(
            HttpClient(engine) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            },
        )

    private fun jsonEngine(
        body: String,
        onRequest: (HttpRequestData) -> Unit = {},
    ): MockEngine =
        MockEngine { request ->
            onRequest(request)
            respond(
                content = body,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

    @Test
    fun `data 봉투를 벗기고 알맹이만 반환한다`() =
        runTest {
            val engine =
                jsonEngine(
                    """
                    {"data":[
                      {"id":"r1","type":"personal","name":"내 장소","description":null,"color":"black",
                       "ownerId":"u1","createdAt":"2026-08-27T00:00:00Z","pinCount":12,"memberCount":1,
                       "thumbnailList":["https://a","https://b"]},
                      {"id":"r2","type":"shared","name":"맛집 탐방","description":"설명","color":"blue",
                       "ownerId":"u2","createdAt":"2026-08-27T00:00:00Z","pinCount":0,"memberCount":3,
                       "thumbnailList":[]}
                    ]}
                    """.trimIndent(),
                )

            val rooms = service(engine).listRooms()

            assertEquals(listOf("r1", "r2"), rooms.map { it.id })
            assertEquals(listOf("personal", "shared"), rooms.map { it.type })
            assertEquals(12, rooms[0].pinCount)
            assertEquals(listOf("https://a", "https://b"), rooms[0].thumbnailList)
            assertEquals(null, rooms[0].description)
        }

    @Test
    fun `서버가 thumbnailList를 내려주지 않아도 빈 목록으로 읽는다`() =
        runTest {
            val engine =
                jsonEngine(
                    """
                    {"data":[{"id":"r1","type":"shared","name":"맛집 탐방","description":null,
                              "color":"black","ownerId":"u1","createdAt":"2026-08-27T00:00:00Z",
                              "pinCount":3,"memberCount":2}]}
                    """.trimIndent(),
                )

            val rooms = service(engine).listRooms()

            assertEquals(emptyList<String>(), rooms.single().thumbnailList)
        }

    @Test
    fun `방 목록은 쿼리 파라미터 없이 요청한다`() =
        runTest {
            var requested: HttpRequestData? = null
            val engine = jsonEngine("""{"data":[]}""") { requested = it }

            service(engine).listRooms()

            assertEquals("/api/v1/rooms", requested?.url?.encodedPath)
            assertEquals("", requested?.url?.encodedQuery)
        }
}
