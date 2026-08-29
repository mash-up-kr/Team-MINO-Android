package team.mino.feature.placedetail.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import team.mino.core.navigation.activity.launcher.PlaceDetailLauncher

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class PlaceDetailNavigationModule {
    @Binds
    @ActivityRetainedScoped
    abstract fun bindPlaceDetailLauncher(impl: PlaceDetailLauncherImpl): PlaceDetailLauncher
}
