package team.mino.core.data.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.repository.PushRegistrationRepositoryImpl
import team.mino.core.domain.repository.PushRegistrationRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PushRegistrationRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPushRegistrationRepository(impl: PushRegistrationRepositoryImpl): PushRegistrationRepository
}
