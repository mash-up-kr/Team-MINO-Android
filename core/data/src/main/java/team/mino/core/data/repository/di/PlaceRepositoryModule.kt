package team.mino.core.data.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.repository.PlaceRepositoryImpl
import team.mino.core.domain.repository.PlaceRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PlaceRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPlaceRepository(impl: PlaceRepositoryImpl): PlaceRepository
}
