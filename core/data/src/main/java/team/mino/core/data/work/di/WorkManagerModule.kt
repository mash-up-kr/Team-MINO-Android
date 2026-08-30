package team.mino.core.data.work.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object WorkManagerModule {
    // getInstance는 첫 호출 시점에 Application의 Configuration.Provider로 WorkManager를 초기화한다.
    // 기본 WorkManagerInitializer가 매니페스트에서 제거돼 있어, HiltWorkerFactory가 붙은 설정이 이 경로로만 들어간다.
    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context,
    ): WorkManager = WorkManager.getInstance(context)
}
