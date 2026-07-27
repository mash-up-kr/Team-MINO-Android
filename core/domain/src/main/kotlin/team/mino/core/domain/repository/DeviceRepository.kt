package team.mino.core.domain.repository

interface DeviceRepository {
    /**
     * 디바이스 ID를 확보해 로컬에 저장한다. 이미 확보된 경우 아무것도 하지 않는다(멱등).
     * 확보에 실패하면 예외를 전파한다.
     */
    suspend fun ensureDeviceId()
}
