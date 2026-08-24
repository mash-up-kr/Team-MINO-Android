package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.RoomRemoteDataSource
import team.mino.core.data.datasource.RoomRemoteDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RoomDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindRoomRemoteDataSource(impl: RoomRemoteDataSourceImpl): RoomRemoteDataSource
}
