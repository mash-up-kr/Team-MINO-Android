package team.mino.core.data.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.repository.RoomPlacesRepositoryImpl
import team.mino.core.domain.repository.RoomPlacesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RoomPlacesRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRoomPlacesRepository(impl: RoomPlacesRepositoryImpl): RoomPlacesRepository
}
