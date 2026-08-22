package team.mino.core.data.datasource

import kotlinx.coroutines.flow.Flow

internal interface AppSettingsLocalDataSource {
    fun observeNotificationDeliveryEnabled(): Flow<Boolean>

    suspend fun setNotificationDeliveryEnabled(enabled: Boolean)
}
