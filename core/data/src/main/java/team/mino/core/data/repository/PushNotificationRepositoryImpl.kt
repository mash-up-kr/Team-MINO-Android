package team.mino.core.data.repository

import team.mino.core.data.datasource.UserRemoteDataSource
import team.mino.core.data.device.PushTokenProvider
import team.mino.core.domain.repository.PushNotificationRepository
import javax.inject.Inject

private const val PLATFORM_ANDROID = "android"

internal class PushNotificationRepositoryImpl @Inject constructor(
    private val pushTokenProvider: PushTokenProvider,
    private val remoteDataSource: UserRemoteDataSource,
) : PushNotificationRepository {
    override suspend fun syncPushToken() {
        val token = pushTokenProvider.getToken()
        remoteDataSource.putPushToken(token, PLATFORM_ANDROID)
    }
}
