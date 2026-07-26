package team.mino.core.common.android.extension

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import team.mino.core.errorhandling.UncaughtErrorHandler
import team.mino.core.errorhandling.UncaughtErrorReporter

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchSafelyTest {
    private val recorded = mutableListOf<Throwable>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        uncaughtErrorReporter = UncaughtErrorReporter { recorded += it }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `잡히지 않은 예외를 리포터와 전역 버스로 전달한다`() =
        runTest {
            val bug = IllegalStateException("bug")
            val viewModel = object : ViewModel() {}

            viewModel.launchSafely { throw bug }

            assertEquals(listOf<Throwable>(bug), recorded)
            assertSame(bug, UncaughtErrorHandler.errors.first())
        }

    @Test
    fun `예외가 없으면 리포터를 호출하지 않는다`() =
        runTest {
            val viewModel = object : ViewModel() {}
            var executed = false

            viewModel.launchSafely { executed = true }

            assertTrue(executed)
            assertTrue(recorded.isEmpty())
        }
}
