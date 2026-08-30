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
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchSafelyTest {
    private val recorded = mutableListOf<Throwable>()
    private val recordingTree =
        object : Timber.Tree() {
            override fun log(
                priority: Int,
                tag: String?,
                message: String,
                t: Throwable?,
            ) {
                t?.let { recorded += it }
            }
        }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        Timber.plant(recordingTree)
    }

    @After
    fun tearDown() {
        Timber.uproot(recordingTree)
        Dispatchers.resetMain()
    }

    @Test
    fun `잡히지 않은 예외를 로그와 전역 버스로 전달한다`() =
        runTest {
            val bug = IllegalStateException("bug")
            val viewModel = object : ViewModel() {}

            viewModel.launchSafely { throw bug }

            assertEquals(listOf<Throwable>(bug), recorded)
            assertSame(bug, UncaughtErrorHandler.errors.first())
        }

    @Test
    fun `예외가 없으면 로그를 남기지 않는다`() =
        runTest {
            val viewModel = object : ViewModel() {}
            var executed = false

            viewModel.launchSafely { executed = true }

            assertTrue(executed)
            assertTrue(recorded.isEmpty())
        }
}
