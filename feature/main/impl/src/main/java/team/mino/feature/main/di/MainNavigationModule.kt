package team.mino.feature.main.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import team.mino.feature.main.api.MainLauncher

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class MainNavigationModule {
    @Binds
    @ActivityRetainedScoped
    abstract fun bindMainLauncher(impl: MainLauncherImpl): MainLauncher
}
