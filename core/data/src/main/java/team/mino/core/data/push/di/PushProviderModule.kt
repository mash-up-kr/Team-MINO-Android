package team.mino.core.data.push.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.push.PushTokenProvider
import team.mino.core.data.push.PushTokenProviderImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PushProviderModule {
    @Binds
    @Singleton
    abstract fun bindPushTokenProvider(impl: PushTokenProviderImpl): PushTokenProvider
}
