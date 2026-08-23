package team.mino.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.BuildConfig
import team.mino.core.data.network.plugin.minoIdentityProofPlugin
import team.mino.core.errorhandling.MinoDomainException

/**
 * 신원 증명 자동 첨부 플러그인의 계약 A-1~A-4를 판정한다.
 * 계약은 contracts/identity-proof-attachment.md §3이 소유하고, host 판정 근거는 research.md R-009다.
 */
class IdentityProofAttachmentTest {
    private val engine = MockEngine { respondOk() }
    private val tokenProvider = FakeIdTokenProvider(TOKEN)

    private fun client(): HttpClient =
        HttpClient(engine) {
            expectSuccess = true
            install(minoIdentityProofPlugin(tokenProvider))
        }

    private fun clientWithoutPlugin(): HttpClient =
        HttpClient(engine) {
            expectSuccess = true
        }

    private fun sentRequest(index: Int = 0): HttpRequestData = engine.requestHistory[index]

    @Test
    fun `Mino host 요청에는 신원 증명이 Bearer 헤더로 실린다`() =
        runTest {
            client().get(minoUrl())

            assertEquals("Bearer $TOKEN", sentRequest().headers[HttpHeaders.Authorization])
        }

    @Test
    fun `외부 host 요청에는 신원 증명이 실리지 않는다`() =
        runTest {
            client().get(externalUrl())

            assertNull(sentRequest().headers[HttpHeaders.Authorization])
        }

    @Test
    fun `플러그인은 Authorization 외에 헤더를 더하지 않는다`() =
        runTest {
            client().get(minoUrl())
            clientWithoutPlugin().get(minoUrl())

            val baseline = sentRequest(index = 1).headers.names()
            val added = sentRequest(index = 0).headers.names().filterNot { it in baseline }
            assertEquals(listOf(HttpHeaders.Authorization), added)
        }

    @Test
    fun `플러그인은 쿼리와 본문을 그대로 내보낸다`() =
        runTest {
            client().post("${minoUrl()}?$QUERY_NAME=$QUERY_VALUE") {
                contentType(ContentType.Application.Json)
                setBody(BODY)
            }

            val sent = sentRequest()
            assertEquals(setOf(QUERY_NAME), sent.url.parameters.names())
            assertEquals(QUERY_VALUE, sent.url.parameters[QUERY_NAME])
            assertEquals(BODY, (sent.body as TextContent).text)
        }

    @Test
    fun `Mino host 요청인데 신원 증명이 없으면 요청을 내보내지 않고 예외를 전파한다`() =
        runTest {
            tokenProvider.token = null

            val result = runCatching { client().get(minoUrl()) }

            assertEquals(0, engine.requestHistory.size)
            // 호출자 계약 C-1 위반이므로 프로그래머 오류로 전파한다 — 도메인 예외로 감싸지 않는다 (A-3)
            assertTrue(result.exceptionOrNull() !is MinoDomainException)
            assertTrue(result.exceptionOrNull() is IllegalStateException)
        }

    @Test
    fun `Mino host가 외부 host로 리다이렉트해도 신원 증명이 따라가지 않는다`() =
        runTest {
            val redirectingEngine =
                MockEngine { request ->
                    if (request.url.host == minoHost) {
                        respond(
                            content = "",
                            status = HttpStatusCode.Found,
                            headers = headersOf(HttpHeaders.Location, externalUrl()),
                        )
                    } else {
                        respondOk()
                    }
                }
            val redirectingClient =
                HttpClient(redirectingEngine) {
                    expectSuccess = true
                    install(minoIdentityProofPlugin(tokenProvider))
                }

            redirectingClient.get(minoUrl())

            val (origin, followed) = redirectingEngine.requestHistory
            assertEquals("Bearer $TOKEN", origin.headers[HttpHeaders.Authorization])
            assertEquals(externalHost, followed.url.host)
            assertNull(followed.headers[HttpHeaders.Authorization])
        }

    private companion object {
        const val TOKEN = "identity-proof-token"
        const val QUERY_NAME = "cursor"
        const val QUERY_VALUE = "20"
        const val BODY = """{"name":"room"}"""

        /** 실서버 도메인이 들어와도 테스트가 그대로 서도록 기준 host는 빌드 설정에서 얻는다. */
        val minoHost: String = Url(BuildConfig.API_BASE_URL).host

        fun minoUrl() = "https://$minoHost/v1/rooms"

        /** 기준 host에 라벨을 덧붙여 어떤 값이 들어와도 반드시 다른 host가 되게 만든다. */
        fun externalUrl() = "https://external-$minoHost/v1/rooms"

        val externalHost: String = Url(externalUrl()).host
    }
}
