package team.mino.feature.onboarding.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import team.mino.core.navigation.activity.launcher.OnboardingLauncher

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class OnboardingNavigationModule {
    @Binds
    @ActivityRetainedScoped
    abstract fun bindOnboardingLauncher(impl: OnboardingLauncherImpl): OnboardingLauncher
}
