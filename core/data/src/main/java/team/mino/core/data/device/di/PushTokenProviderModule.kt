package team.mino.core.data.device.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.device.PushTokenProvider
import team.mino.core.data.device.PushTokenProviderImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PushTokenProviderModule {
    @Binds
    @Singleton
    abstract fun bindPushTokenProvider(impl: PushTokenProviderImpl): PushTokenProvider
}
