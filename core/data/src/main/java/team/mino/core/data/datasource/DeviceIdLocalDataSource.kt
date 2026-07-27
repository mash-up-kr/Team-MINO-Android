package team.mino.core.data.datasource

internal interface DeviceIdLocalDataSource {
    suspend fun getDeviceId(): String?

    suspend fun saveDeviceId(deviceId: String)
}
