package team.mino.feature.profile.di

import android.content.Context
import android.content.Intent
import team.mino.core.navigation.activity.BaseActivityLauncher
import team.mino.core.navigation.activity.intentOf
import team.mino.core.navigation.activity.launcher.ProfileLauncher
import team.mino.feature.profile.ProfileActivity
import javax.inject.Inject

internal class ProfileLauncherImpl @Inject constructor() : BaseActivityLauncher(), ProfileLauncher {
    override fun createIntent(context: Context): Intent = context.intentOf<ProfileActivity>()
}
