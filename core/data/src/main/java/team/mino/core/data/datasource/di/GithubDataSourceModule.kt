package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.GithubRemoteDataSource
import team.mino.core.data.datasource.GithubRemoteDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class GithubDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindGithubRemoteDataSource(impl: GithubRemoteDataSourceImpl): GithubRemoteDataSource
}
