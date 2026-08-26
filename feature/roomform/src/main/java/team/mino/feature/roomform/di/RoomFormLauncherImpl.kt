package team.mino.feature.roomform.di

import android.content.Context
import android.content.Intent
import team.mino.core.navigation.activity.BaseActivityLauncher
import team.mino.core.navigation.activity.intentOf
import team.mino.core.navigation.activity.launcher.RoomFormLauncher
import team.mino.feature.roomform.RoomFormActivity
import javax.inject.Inject

internal class RoomFormLauncherImpl @Inject constructor() : BaseActivityLauncher(), RoomFormLauncher {
    override fun createIntent(context: Context): Intent = context.intentOf<RoomFormActivity>()
}
