package team.mino.core.data.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.repository.ProfileRepositoryImpl
import team.mino.core.domain.repository.ProfileRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ProfileRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
}
