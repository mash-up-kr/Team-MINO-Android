package team.mino.core.data.device

internal interface DeviceInfoProvider {
    suspend fun getDeviceId(): String?
}
