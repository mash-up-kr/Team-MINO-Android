package team.mino.core.data.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.repository.GithubRepositoryImpl
import team.mino.core.domain.repository.GithubRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class GithubRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindGithubRepository(impl: GithubRepositoryImpl): GithubRepository
}
