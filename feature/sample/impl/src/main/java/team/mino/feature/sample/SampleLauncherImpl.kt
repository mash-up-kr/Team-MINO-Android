package team.mino.feature.sample

import android.content.Context
import android.content.Intent
import team.mino.core.navigation.activity.BaseActivityLauncher
import team.mino.feature.sample.api.SampleArgs
import team.mino.feature.sample.api.SampleLauncher
import javax.inject.Inject

internal class SampleLauncherImpl
    @Inject
    constructor() :
    BaseActivityLauncher<SampleArgs>(SampleArgs.serializer()),
        SampleLauncher {
        override fun createIntent(context: Context): Intent = Intent(context, SampleActivity::class.java)
    }
