package team.mino.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import team.mino.core.data.network.dto.request.RoomRequest
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
        status: HttpStatusCode = HttpStatusCode.OK,
        onRequest: (HttpRequestData) -> Unit = {},
    ): MockEngine =
        MockEngine { request ->
            onRequest(request)
            respond(
                content = body,
                status = status,
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

    @Test
    fun `getRoom은 roomId를 경로에 넣어 GET으로 요청한다`() =
        runTest {
            var requested: HttpRequestData? = null
            val engine = jsonEngine(ROOM_BODY) { requested = it }

            service(engine).getRoom(ROOM_ID)

            assertEquals(HttpMethod.Get, requested?.method)
            assertEquals("/api/v1/rooms/$ROOM_ID", requested?.url?.encodedPath)
            assertEquals("", requested?.url?.encodedQuery)
        }

    @Test
    fun `getRoom은 data 봉투를 벗기고 방 하나를 반환한다`() =
        runTest {
            val room = service(jsonEngine(ROOM_BODY)).getRoom(ROOM_ID)

            assertEquals(ROOM_ID, room.id)
            assertEquals("맛집 탐방", room.name)
            assertEquals("설명", room.description)
            assertEquals("red_orange", room.color)
            assertEquals("u1", room.ownerId)
        }

    @Test
    fun `getRoom 응답의 설명 없음은 null로 읽고 DTO에 없는 필드는 무시한다`() =
        runTest {
            val engine =
                jsonEngine(
                    """
                    {"data":{"id":"$ROOM_ID","type":"shared","name":"맛집 탐방","description":null,
                             "color":"gray","ownerId":"u1","createdAt":"2026-08-28T00:00:00Z",
                             "pinCount":7,"memberCount":3}}
                    """.trimIndent(),
                )

            val room = service(engine).getRoom(ROOM_ID)

            assertEquals(null, room.description)
            assertEquals("gray", room.color)
        }

    @Test
    fun `createRoom은 api v1 rooms로 POST한다`() =
        runTest {
            var requested: HttpRequestData? = null
            val engine = jsonEngine(ROOM_BODY, HttpStatusCode.Created) { requested = it }

            service(engine).createRoom(ROOM_REQUEST)

            assertEquals(HttpMethod.Post, requested?.method)
            assertEquals("/api/v1/rooms", requested?.url?.encodedPath)
            assertEquals(ContentType.Application.Json, requested?.body?.contentType?.withoutParameters())
            assertEquals(
                """{"name":"맛집 탐방","description":"설명","color":"red_orange"}""",
                (requested?.body as TextContent).text,
            )
        }

    @Test
    fun `createRoom은 201 응답의 data 봉투를 벗기고 생성된 방을 반환한다`() =
        runTest {
            val engine = jsonEngine(ROOM_BODY, HttpStatusCode.Created)

            val room = service(engine).createRoom(ROOM_REQUEST)

            assertEquals(ROOM_ID, room.id)
            assertEquals("맛집 탐방", room.name)
            assertEquals("red_orange", room.color)
        }

    @Test
    fun `updateRoom은 roomId 경로로 PATCH한다`() =
        runTest {
            var requested: HttpRequestData? = null
            val engine = jsonEngine(ROOM_BODY) { requested = it }

            service(engine).updateRoom(ROOM_ID, ROOM_REQUEST)

            assertEquals(HttpMethod.Patch, requested?.method)
            assertEquals("/api/v1/rooms/$ROOM_ID", requested?.url?.encodedPath)
            assertEquals(ContentType.Application.Json, requested?.body?.contentType?.withoutParameters())
        }

    @Test
    fun `updateRoom은 data 봉투를 벗기고 수정된 방을 반환한다`() =
        runTest {
            val engine =
                jsonEngine(
                    """
                    {"data":{"id":"$ROOM_ID","type":"shared","name":"바뀐 이름","description":null,
                             "color":"light_blue","ownerId":"u1","createdAt":"2026-08-28T00:00:00Z"}}
                    """.trimIndent(),
                )

            val request = RoomRequest(name = "바뀐 이름", description = null, color = "light_blue")

            val room = service(engine).updateRoom(ROOM_ID, request)

            assertEquals("바뀐 이름", room.name)
            assertEquals(null, room.description)
            assertEquals("light_blue", room.color)
        }

    private companion object {
        const val ROOM_ID = "b0f1c2d3-4e5f-6a7b-8c9d-0e1f2a3b4c5d"
        val ROOM_REQUEST = RoomRequest(name = "맛집 탐방", description = "설명", color = "red_orange")
        val ROOM_BODY =
            """
            {"data":{"id":"$ROOM_ID","type":"shared","name":"맛집 탐방","description":"설명",
                     "color":"red_orange","ownerId":"u1","createdAt":"2026-08-28T00:00:00Z"}}
            """.trimIndent()
    }
}
