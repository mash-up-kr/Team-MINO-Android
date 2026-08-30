package team.mino.core.data.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.repository.OnboardingProgressRepositoryImpl
import team.mino.core.domain.repository.OnboardingProgressRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class OnboardingProgressRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindOnboardingProgressRepository(impl: OnboardingProgressRepositoryImpl): OnboardingProgressRepository
}
