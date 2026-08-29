package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.OnboardingProgressLocalDataSource
import team.mino.core.data.datasource.OnboardingProgressLocalDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class OnboardingDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindOnboardingProgressLocalDataSource(
        impl: OnboardingProgressLocalDataSourceImpl,
    ): OnboardingProgressLocalDataSource
}
