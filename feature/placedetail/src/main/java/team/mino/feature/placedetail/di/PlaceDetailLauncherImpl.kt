package team.mino.feature.placedetail.di

import android.content.Context
import android.content.Intent
import team.mino.core.navigation.activity.BaseActivityLauncher
import team.mino.core.navigation.activity.intentOf
import team.mino.core.navigation.activity.launcher.PlaceDetailLauncher
import team.mino.feature.placedetail.PlaceDetailActivity
import javax.inject.Inject

internal class PlaceDetailLauncherImpl @Inject constructor() : BaseActivityLauncher(), PlaceDetailLauncher {
    override fun createIntent(context: Context): Intent = context.intentOf<PlaceDetailActivity>()
}
