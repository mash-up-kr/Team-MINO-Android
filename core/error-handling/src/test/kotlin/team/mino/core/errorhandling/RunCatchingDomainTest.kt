package team.mino.core.errorhandling

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class RunCatchingDomainTest {
    @Test
    fun `성공하면 Result success를 반환한다`() =
        runTest {
            val result = runCatchingDomain { "value" }

            assertEquals(Result.success("value"), result)
        }

    @Test
    fun `도메인 예외는 Result failure로 잡는다`() =
        runTest {
            val exception = MinoDomainException.Network(IOException())

            val result = runCatchingDomain<String> { throw exception }

            assertSame(exception, result.exceptionOrNull())
        }

    @Test
    fun `도메인 예외가 아닌 예외는 잡지 않고 전파한다`() =
        runTest {
            var thrown: Throwable? = null

            try {
                runCatchingDomain<String> { error("bug") }
            } catch (e: IllegalStateException) {
                thrown = e
            }

            assertNotNull(thrown)
        }

    @Test
    fun `CancellationException은 잡지 않고 전파한다`() =
        runTest {
            var thrown: Throwable? = null

            try {
                runCatchingDomain<String> { throw CancellationException("cancelled") }
            } catch (e: CancellationException) {
                thrown = e
            }

            assertNotNull(thrown)
        }

    @Test
    fun `onDomainFailure는 도메인 예외 타입으로 소비한다`() =
        runTest {
            val exception = MinoDomainException.Http(code = 404, cause = IOException())
            var received: MinoDomainException? = null

            runCatchingDomain<String> { throw exception }
                .onDomainFailure { received = it }

            assertTrue(received is MinoDomainException.Http)
            assertEquals(404, (received as MinoDomainException.Http).code)
        }

    @Test
    fun `성공한 Result에서는 onDomainFailure가 호출되지 않는다`() =
        runTest {
            var received: MinoDomainException? = null

            runCatchingDomain { "value" }
                .onDomainFailure { received = it }

            assertNull(received)
        }
}
