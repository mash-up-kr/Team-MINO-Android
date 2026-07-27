package team.mino.core.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import team.mino.core.data.datasource.DeviceIdLocalDataSource
import team.mino.core.data.device.DeviceInfoProvider
import team.mino.core.domain.repository.DeviceRepository
import javax.inject.Inject

internal class DeviceRepositoryImpl @Inject constructor(
    private val deviceIdLocalDataSource: DeviceIdLocalDataSource,
    private val deviceInfoProvider: DeviceInfoProvider,
) : DeviceRepository {
    // 동시 호출 시 ANDROID_ID 읽기·저장이 중복 수행되지 않도록 직렬화한다.
    private val mutex = Mutex()

    override suspend fun ensureDeviceId() {
        mutex.withLock {
            if (deviceIdLocalDataSource.getDeviceId() != null) return

            val androidId = checkNotNull(deviceInfoProvider.getDeviceId()) { "ANDROID_ID를 확보할 수 없습니다" }
            deviceIdLocalDataSource.saveDeviceId(androidId)
        }
    }
}
