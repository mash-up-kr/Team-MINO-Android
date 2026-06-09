package team.mino.feature.home

import android.content.Context
import android.content.Intent
import team.mino.core.navigation.activity.BaseActivityLauncher
import team.mino.feature.home.api.HomeArgs
import team.mino.feature.home.api.HomeLauncher
import javax.inject.Inject

internal class HomeLauncherImpl
    @Inject
    constructor() :
    BaseActivityLauncher<HomeArgs>(HomeArgs.serializer()),
        HomeLauncher {
        override fun createIntent(context: Context): Intent = Intent(context, HomeActivity::class.java)
    }
