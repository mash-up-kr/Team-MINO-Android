package team.mino.core.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// preferencesDataStore delegate는 파일당 단일 인스턴스를 보장하기 위해 top-level에 둔다.
// 같은 파일명으로 인스턴스를 중복 생성하면 IllegalStateException이 발생한다.
private val Context.minoDataStore: DataStore<Preferences> by preferencesDataStore(name = "mino_preferences")

@Module
@InstallIn(SingletonComponent::class)
internal object DataStoreModule {
    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.minoDataStore
}
