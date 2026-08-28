package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.PlaceRemoteDataSource
import team.mino.core.data.datasource.PlaceRemoteDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PlaceDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindPlaceRemoteDataSource(impl: PlaceRemoteDataSourceImpl): PlaceRemoteDataSource
}
