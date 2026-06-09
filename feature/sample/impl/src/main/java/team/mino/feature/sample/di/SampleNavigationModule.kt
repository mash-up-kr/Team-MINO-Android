package team.mino.feature.sample.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import team.mino.feature.sample.SampleLauncherImpl
import team.mino.feature.sample.api.SampleLauncher

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class SampleNavigationModule {
    @Binds
    @ActivityRetainedScoped
    abstract fun bindSampleLauncher(impl: SampleLauncherImpl): SampleLauncher
}
