package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.ProfileLocalDataSource
import team.mino.core.data.datasource.ProfileLocalDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ProfileDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindProfileLocalDataSource(impl: ProfileLocalDataSourceImpl): ProfileLocalDataSource
}
