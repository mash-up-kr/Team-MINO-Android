package team.mino

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import team.mino.logging.CrashlyticsTree
import team.mino.logging.FileLineDebugTree
import timber.log.Timber

@HiltAndroidApp
class MinoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeLogger()
    }

    private fun initializeLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(FileLineDebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }
}
