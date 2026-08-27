package team.mino.core.data.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.repository.SharedPlaceRepositoryImpl
import team.mino.core.domain.repository.SharedPlaceRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SharedPlaceRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSharedPlaceRepository(impl: SharedPlaceRepositoryImpl): SharedPlaceRepository
}
