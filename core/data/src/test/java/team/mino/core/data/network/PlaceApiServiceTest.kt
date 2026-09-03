package team.mino.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import team.mino.core.data.network.service.PlaceApiService

class PlaceApiServiceTest {
    private fun service(engine: MockEngine): PlaceApiService =
        PlaceApiService(
            HttpClient(engine) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            },
        )

    @Test
    fun `장소 삭제 요청은 DELETE api v1 pins {pinId} 경로로 나간다`() =
        runTest {
            var requested: HttpRequestData? = null
            val engine =
                MockEngine { request ->
                    requested = request
                    respond(
                        content = """{"data":{"ok":true}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                }

            service(engine).deletePin("pin-123")

            assertEquals(HttpMethod.Delete, requested?.method)
            assertEquals("/api/v1/pins/pin-123", requested?.url?.encodedPath)
        }
}
