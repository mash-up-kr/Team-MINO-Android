package team.mino.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import team.mino.core.data.network.service.PlaceApiService

/**
 * 삭제는 되돌릴 수 없는 동작인데 `deletePin`은 응답 본문을 읽지 않는다 — 경로·메서드가 맞게 나가는지도,
 * 실패가 성공으로 흡수되지 않는지도 컴파일이 보증하지 않는다([PinApiServiceTest]와 같은 이유).
 *
 * **성공 응답의 본문 스키마는 여기서 단언하지 않는다.** `deletePin`이 본문을 읽지 않아 목이 무엇을 주든
 * 판정에 쓰이지 않기 때문이다. 서버가 실제로 어떤 상태 코드·본문을 주는지는 이 테스트가 아니라 API 문서
 * 대조로 확인해야 한다.
 */
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
            val engine = jsonEngine("""{"data":{"ok":true}}""") { requested = it }

            service(engine).deletePin("pin-123")

            assertEquals(HttpMethod.Delete, requested?.method)
            assertEquals("/api/v1/pins/pin-123", requested?.url?.encodedPath)
        }

    /**
     * 삭제 실패가 성공으로 흡수되면 서버에 그대로 남은 장소가 화면에서만 사라진다 — 뷰모델이 목록에서
     * 지우는 것은 `onSuccess` 하나에 달려 있다.
     */
    @Test
    fun `삭제 실패 응답은 예외로 전파된다`() =
        runTest {
            val engine = MockEngine { respondError(HttpStatusCode.NotFound) }

            val error =
                try {
                    service(engine).deletePin("pin-missing")
                    null
                } catch (e: ClientRequestException) {
                    e
                }

            assertEquals(HttpStatusCode.NotFound, error?.response?.status)
        }
}
