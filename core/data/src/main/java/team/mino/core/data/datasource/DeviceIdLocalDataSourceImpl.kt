package team.mino.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject

internal class DeviceIdLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : DeviceIdLocalDataSource {
    override suspend fun getDeviceId(): String? = dataStore.data.first()[KEY_DEVICE_ID]

    override suspend fun saveDeviceId(deviceId: String) {
        dataStore.edit { preferences -> preferences[KEY_DEVICE_ID] = deviceId }
    }

    private companion object {
        val KEY_DEVICE_ID = stringPreferencesKey("device_id")
    }
}
