package team.mino.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import team.mino.core.data.network.dto.request.PinCreateRequest
import team.mino.core.data.network.service.PinApiService

/**
 * `202`는 본문 스키마가 없어 반환 타입이 `Unit`인데(contracts/shared-place-save-api.md §1.2),
 * 본문을 읽지 않는 호출에도 `expectSuccess = true`의 판정이 걸리는지는 컴파일이 보증하지 않는다.
 * 접수 응답이 예외 없이 통과하는지와, 실패 응답은 그대로 예외가 되는지를 확인한다.
 */
class PinApiServiceTest {
    private fun service(engine: MockEngine): PinApiService =
        PinApiService(
            HttpClient(engine) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            },
        )

    private fun acceptedEngine(onRequest: (HttpRequestData) -> Unit = {}): MockEngine =
        MockEngine { request ->
            onRequest(request)
            respond(content = "", status = HttpStatusCode.Accepted)
        }

    @Test
    fun `본문 없는 202 응답은 예외 없이 통과한다`() =
        runTest {
            service(acceptedEngine()).createPin(PinCreateRequest(url = INSTAGRAM_URL, roomIds = listOf("r1")))
        }

    @Test
    fun `대상 방은 경로가 아니라 본문의 roomIds에 담긴다`() =
        runTest {
            var requested: HttpRequestData? = null
            val engine = acceptedEngine { requested = it }

            service(engine).createPin(PinCreateRequest(url = INSTAGRAM_URL, roomIds = listOf("r1", "r2")))

            assertEquals(HttpMethod.Post, requested?.method)
            assertEquals("/api/v1/rooms/pins", requested?.url?.encodedPath)
            assertEquals(
                """{"url":"$INSTAGRAM_URL","roomIds":["r1","r2"]}""",
                (requested?.body as TextContent).text,
            )
            assertEquals(ContentType.Application.Json, requested?.body?.contentType?.withoutParameters())
        }

    @Test
    fun `실패 응답은 예외로 전파된다`() =
        runTest {
            val engine = MockEngine { respondError(HttpStatusCode.Forbidden) }

            val error =
                try {
                    service(engine).createPin(PinCreateRequest(url = INSTAGRAM_URL, roomIds = listOf("r1")))
                    null
                } catch (e: ClientRequestException) {
                    e
                }

            assertEquals(HttpStatusCode.Forbidden, error?.response?.status)
        }

    private companion object {
        const val INSTAGRAM_URL = "https://www.instagram.com/p/XXXX/"
    }
}
