package team.mino.core.data.repository

import kotlinx.coroutines.flow.Flow
import team.mino.core.data.datasource.AppSettingsLocalDataSource
import team.mino.core.domain.repository.AppSettingsRepository
import javax.inject.Inject

internal class AppSettingsRepositoryImpl @Inject constructor(
    private val localDataSource: AppSettingsLocalDataSource,
) : AppSettingsRepository {
    override fun observeNotificationDeliveryEnabled(): Flow<Boolean> =
        localDataSource.observeNotificationDeliveryEnabled()

    override suspend fun setNotificationDeliveryEnabled(enabled: Boolean) {
        localDataSource.setNotificationDeliveryEnabled(enabled)
    }
}
