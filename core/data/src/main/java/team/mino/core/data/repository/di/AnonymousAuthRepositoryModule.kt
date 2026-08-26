package team.mino.core.data.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.repository.AnonymousAuthRepositoryImpl
import team.mino.core.domain.repository.AnonymousAuthRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AnonymousAuthRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAnonymousAuthRepository(impl: AnonymousAuthRepositoryImpl): AnonymousAuthRepository
}
