package team.mino.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.network.extension.convertDomainException
import team.mino.core.errorhandling.MinoDomainException
import java.io.IOException

class DomainExceptionMappingTest {
    private fun client(engine: MockEngine): HttpClient =
        HttpClient(engine) {
            expectSuccess = true
            convertDomainException()
        }

    @Test
    fun `2xx 응답은 예외 없이 통과한다`() =
        runTest {
            val engine = MockEngine { respondOk("body") }

            client(engine).get("https://test")
        }

    @Test
    fun `non-2xx 응답은 Http 리프로 매핑한다`() =
        runTest {
            val engine = MockEngine { respond("not found", HttpStatusCode.NotFound) }
            var thrown: Throwable? = null

            try {
                client(engine).get("https://test")
            } catch (e: MinoDomainException.Http) {
                thrown = e
            }

            assertEquals(404, (thrown as MinoDomainException.Http).code)
        }

    @Test
    fun `IOException은 Network 리프로 매핑한다`() =
        runTest {
            val engine = MockEngine { throw IOException("no network") }
            var thrown: Throwable? = null

            try {
                client(engine).get("https://test")
            } catch (e: MinoDomainException.Network) {
                thrown = e
            }

            assertNotNull(thrown)
            assertTrue(thrown?.cause is IOException)
        }

    @Test
    fun `CancellationException은 매핑하지 않고 그대로 전파한다`() =
        runTest {
            val engine = MockEngine { throw CancellationException("cancelled") }
            var thrown: Throwable? = null

            try {
                client(engine).get("https://test")
            } catch (e: CancellationException) {
                thrown = e
            }

            // 인스턴스 동일성은 코루틴 디버그 모드의 stack trace recovery가 사본을 만들어 단언 불가
            assertEquals("cancelled", thrown?.message)
        }

    @Test
    fun `화이트리스트 밖 예외는 매핑하지 않고 전파한다`() =
        runTest {
            val engine = MockEngine { throw SerializationException("contract violation") }
            var thrown: Throwable? = null

            try {
                client(engine).get("https://test")
            } catch (e: SerializationException) {
                thrown = e
            }

            assertNotNull(thrown)
        }
}
