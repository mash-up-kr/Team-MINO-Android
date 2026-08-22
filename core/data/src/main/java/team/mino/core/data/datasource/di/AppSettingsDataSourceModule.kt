package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.AppSettingsLocalDataSource
import team.mino.core.data.datasource.AppSettingsLocalDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AppSettingsDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindAppSettingsLocalDataSource(impl: AppSettingsLocalDataSourceImpl): AppSettingsLocalDataSource
}
