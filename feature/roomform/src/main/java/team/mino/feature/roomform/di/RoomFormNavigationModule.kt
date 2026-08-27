package team.mino.feature.roomform.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import team.mino.core.navigation.activity.launcher.RoomFormLauncher

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class RoomFormNavigationModule {
    @Binds
    @ActivityRetainedScoped
    abstract fun bindRoomFormLauncher(impl: RoomFormLauncherImpl): RoomFormLauncher
}
