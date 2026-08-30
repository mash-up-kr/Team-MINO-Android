package team.mino.core.errorhandling

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test

class UncaughtErrorHandlerTest {
    @Test
    fun `dispatch한 예외를 수신한다`() =
        runTest {
            val bug = IllegalStateException("bug")

            UncaughtErrorHandler.dispatch(bug)

            assertSame(bug, UncaughtErrorHandler.errors.first())
        }
}
