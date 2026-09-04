package team.mino.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import team.mino.core.domain.model.PermissionType
import javax.inject.Inject

internal class PermissionLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PermissionLocalDataSource {
    override suspend fun hasRequestedPermissionBefore(type: PermissionType): Boolean =
        dataStore.data.first()[type.toKey()] ?: false

    override suspend fun markPermissionRequested(type: PermissionType) {
        dataStore.edit { preferences -> preferences[type.toKey()] = true }
    }

    private fun PermissionType.toKey() =
        when (this) {
            PermissionType.NOTIFICATION -> KEY_NOTIFICATION_PERMISSION_REQUESTED
            PermissionType.LOCATION -> KEY_LOCATION_PERMISSION_REQUESTED
        }

    private companion object {
        val KEY_NOTIFICATION_PERMISSION_REQUESTED = booleanPreferencesKey("notification_permission_requested")
        val KEY_LOCATION_PERMISSION_REQUESTED = booleanPreferencesKey("location_permission_requested")
    }
}
