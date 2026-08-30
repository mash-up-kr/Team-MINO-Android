package team.mino

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import team.mino.logging.CrashlyticsTree
import team.mino.logging.FileLineDebugTree
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MinoApplication :
    Application(),
    Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // AndroidManifest에서 WorkManagerInitializer를 제거했으므로 WorkManager는 첫 getInstance() 시점에
    // 이 설정으로 초기화된다. 그 시점은 Hilt 주입이 끝난 onCreate() 이후다.
    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(workerFactory)
                .build()

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
