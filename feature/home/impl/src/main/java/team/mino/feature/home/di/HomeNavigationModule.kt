package team.mino.feature.home.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import team.mino.feature.home.api.HomeLauncher

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class HomeNavigationModule {
    @Binds
    @ActivityRetainedScoped
    abstract fun bindHomeLauncher(impl: HomeLauncherImpl): HomeLauncher
}
