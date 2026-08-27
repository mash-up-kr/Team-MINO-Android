package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.PinRemoteDataSource
import team.mino.core.data.datasource.PinRemoteDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PinDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindPinRemoteDataSource(impl: PinRemoteDataSourceImpl): PinRemoteDataSource
}
