package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.PermissionLocalDataSource
import team.mino.core.data.datasource.PermissionLocalDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PermissionDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindPermissionLocalDataSource(impl: PermissionLocalDataSourceImpl): PermissionLocalDataSource
}
