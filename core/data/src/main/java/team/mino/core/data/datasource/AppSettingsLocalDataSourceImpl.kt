package team.mino.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class AppSettingsLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : AppSettingsLocalDataSource {
    override fun observeNotificationDeliveryEnabled(): Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[KEY_NOTIFICATION_DELIVERY_ENABLED] ?: false }

    override suspend fun setNotificationDeliveryEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_NOTIFICATION_DELIVERY_ENABLED] = enabled }
    }

    private companion object {
        val KEY_NOTIFICATION_DELIVERY_ENABLED = booleanPreferencesKey("notification_delivery_enabled")
    }
}
