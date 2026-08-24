package team.mino.feature.profile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import team.mino.core.navigation.activity.launcher.ProfileLauncher

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class ProfileNavigationModule {
    @Binds
    @ActivityRetainedScoped
    abstract fun bindProfileLauncher(impl: ProfileLauncherImpl): ProfileLauncher
}
