package team.mino.core.data.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.repository.PushNotificationRepositoryImpl
import team.mino.core.domain.repository.PushNotificationRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PushNotificationRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPushNotificationRepository(impl: PushNotificationRepositoryImpl): PushNotificationRepository
}
