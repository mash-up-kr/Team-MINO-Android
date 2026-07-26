package team.mino.core.errorhandling

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException

class DomainErrorEmitterTest {
    @Test
    fun `수집 시작 전에 방출한 에러도 버퍼에 보존한다`() =
        runTest {
            val emitter = domainErrorEmitter()
            val error = MinoDomainException.Network(IOException())

            emitter.emitDomainError(error)

            assertSame(error, emitter.domainErrors.first())
        }

    @Test
    fun `방출한 순서대로 수신한다`() =
        runTest {
            val emitter = domainErrorEmitter()
            val first = MinoDomainException.Network(IOException())
            val second = MinoDomainException.Http(code = 500, cause = IOException())

            emitter.emitDomainError(first)
            emitter.emitDomainError(second)

            assertEquals(listOf(first, second), emitter.domainErrors.take(2).toList())
        }
}
