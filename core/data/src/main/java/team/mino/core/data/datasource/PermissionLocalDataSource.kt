package team.mino.core.data.datasource

import team.mino.core.domain.model.PermissionType

internal interface PermissionLocalDataSource {
    suspend fun hasRequestedPermissionBefore(type: PermissionType): Boolean

    suspend fun markPermissionRequested(type: PermissionType)
}
