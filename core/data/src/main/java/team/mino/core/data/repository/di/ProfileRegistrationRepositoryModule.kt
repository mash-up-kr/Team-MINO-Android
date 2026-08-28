package team.mino.core.data.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.repository.ProfileRegistrationRepositoryImpl
import team.mino.core.domain.repository.ProfileRegistrationRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ProfileRegistrationRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindProfileRegistrationRepository(
        impl: ProfileRegistrationRepositoryImpl,
    ): ProfileRegistrationRepository
}
