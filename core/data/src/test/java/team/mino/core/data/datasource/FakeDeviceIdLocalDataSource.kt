package team.mino.core.data.datasource

import kotlinx.coroutines.yield

internal class FakeDeviceIdLocalDataSource : DeviceIdLocalDataSource {
    var savedDeviceId: String? = null
    var saveCount = 0

    override suspend fun getDeviceId(): String? {
        // 조회와 저장 사이에 다른 코루틴이 끼어들 수 있는 실제 suspend 지점을 재현한다 (동시성 테스트 전제)
        yield()
        return savedDeviceId
    }

    override suspend fun saveDeviceId(deviceId: String) {
        savedDeviceId = deviceId
        saveCount++
    }
}
