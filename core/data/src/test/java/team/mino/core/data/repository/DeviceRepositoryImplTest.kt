package team.mino.core.data.repository

import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.datasource.FakeDeviceIdLocalDataSource
import team.mino.core.data.device.FakeDeviceInfoProvider

class DeviceRepositoryImplTest {
    private val dataSource = FakeDeviceIdLocalDataSource()
    private val provider = FakeDeviceInfoProvider(ANDROID_ID)
    private val repository =
        DeviceRepositoryImpl(
            deviceIdLocalDataSource = dataSource,
            deviceInfoProvider = provider,
        )

    @Test
    fun `로컬에 디바이스 ID가 있으면 ANDROID_ID를 읽지 않고 종료한다`() =
        runTest {
            dataSource.savedDeviceId = "cached-id"

            repository.ensureDeviceId()

            assertEquals(0, provider.readCount)
            assertEquals(0, dataSource.saveCount)
            assertEquals("cached-id", dataSource.savedDeviceId)
        }

    @Test
    fun `로컬에 디바이스 ID가 없으면 ANDROID_ID를 읽어 저장한다`() =
        runTest {
            repository.ensureDeviceId()

            assertEquals(ANDROID_ID, dataSource.savedDeviceId)
            assertEquals(1, dataSource.saveCount)
        }

    @Test
    fun `재호출해도 저장과 ANDROID_ID 읽기는 1회만 수행된다`() =
        runTest {
            repository.ensureDeviceId()
            repository.ensureDeviceId()

            assertEquals(1, provider.readCount)
            assertEquals(1, dataSource.saveCount)
        }

    @Test
    fun `ANDROID_ID를 확보할 수 없으면 예외를 던지고 저장하지 않는다`() =
        runTest {
            provider.androidId = null

            val result = runCatching { repository.ensureDeviceId() }

            assertTrue(result.exceptionOrNull() is IllegalStateException)
            assertNull(dataSource.savedDeviceId)
            assertEquals(0, dataSource.saveCount)
        }

    @Test
    fun `동시에 호출해도 저장은 1회만 수행된다`() =
        runTest {
            joinAll(
                launch { repository.ensureDeviceId() },
                launch { repository.ensureDeviceId() },
            )

            assertEquals(1, provider.readCount)
            assertEquals(1, dataSource.saveCount)
        }

    private companion object {
        const val ANDROID_ID = "test-android-id"
    }
}
