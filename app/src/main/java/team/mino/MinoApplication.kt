package team.mino

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import dagger.hilt.android.HiltAndroidApp
import team.mino.logging.CrashlyticsTree
import team.mino.logging.FileLineDebugTree
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MinoApplication :
    Application(),
    Configuration.Provider,
    SingletonImageLoader.Factory {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // MinoAvatar·MinoRoomThumbnail(core:design-system)은 coil3 AsyncImage로 http(s) URL을 그리는데,
    // coil-compose만으로는 실제 네트워크 요청을 보낼 fetcher가 없어 모든 이미지가 즉시 실패하고
    // fallback 글리프만 보인다(MinoAvatar KDoc의 사전 경고). 여기서 KtorNetworkFetcherFactory를 등록해야
    // 실제 서버 썸네일·아바타 이미지가 로드된다.
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader
            .Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()

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
