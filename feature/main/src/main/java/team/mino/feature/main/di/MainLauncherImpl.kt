package team.mino.feature.main.di

import android.content.Context
import android.content.Intent
import team.mino.core.navigation.activity.BaseActivityLauncher
import team.mino.core.navigation.activity.intentOf
import team.mino.core.navigation.activity.launcher.MainLauncher
import team.mino.feature.main.MainActivity
import javax.inject.Inject

internal class MainLauncherImpl @Inject constructor() : BaseActivityLauncher(), MainLauncher {
    override fun createIntent(context: Context): Intent = context.intentOf<MainActivity>()
}
