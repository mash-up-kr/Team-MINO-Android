package team.mino.core.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import team.mino.core.data.datasource.PermissionLocalDataSource
import team.mino.core.domain.model.PermissionType
import team.mino.core.domain.repository.PermissionRepository
import javax.inject.Inject

internal class PermissionRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val permissionLocalDataSource: PermissionLocalDataSource,
) : PermissionRepository {
    override fun isNotificationPermissionGranted(): Boolean =
        context.isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)

    override fun isLocationPermissionGranted(): Boolean =
        context.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
            context.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)

    override suspend fun hasRequestedPermissionBefore(type: PermissionType): Boolean =
        permissionLocalDataSource.hasRequestedPermissionBefore(type)

    override suspend fun markPermissionRequested(type: PermissionType) {
        permissionLocalDataSource.markPermissionRequested(type)
    }

    private fun Context.isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
