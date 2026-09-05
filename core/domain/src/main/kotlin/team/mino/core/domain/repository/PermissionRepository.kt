package team.mino.core.domain.repository

import team.mino.core.domain.model.PermissionType

interface PermissionRepository {
    /**
     * OS 알림 권한이 허용되어 있는지 즉시 조회한다. 캐시하지 않고 OS를 그대로 조회한다.
     */
    fun isNotificationPermissionGranted(): Boolean

    /**
     * OS 위치 권한이 허용되어 있는지 즉시 조회한다. 캐시하지 않고 OS를 그대로 조회한다.
     */
    fun isLocationPermissionGranted(): Boolean

    /**
     * 해당 권한을 과거에 한 번이라도 요청한 적 있는지 로컬 저장소에서 조회한다.
     */
    suspend fun hasRequestedPermissionBefore(type: PermissionType): Boolean

    /**
     * 해당 권한을 요청했음을 로컬 저장소에 기록한다.
     */
    suspend fun markPermissionRequested(type: PermissionType)
}
