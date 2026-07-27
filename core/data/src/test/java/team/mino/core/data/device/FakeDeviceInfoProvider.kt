package team.mino.core.data.device

internal class FakeDeviceInfoProvider(
    var androidId: String?,
) : DeviceInfoProvider {
    var readCount = 0

    override suspend fun getDeviceId(): String? {
        readCount++
        return androidId
    }
}
