package team.mino.feature.home

import android.content.Context
import android.content.Intent
import team.mino.core.navigation.activity.BaseActivityLauncher
import team.mino.core.navigation.activity.intentOf
import team.mino.feature.home.api.HomeLauncher
import javax.inject.Inject

internal class HomeLauncherImpl
    @Inject
    constructor() :
    BaseActivityLauncher(),
        HomeLauncher {
        override fun createIntent(context: Context): Intent = context.intentOf<HomeActivity>()
    }
