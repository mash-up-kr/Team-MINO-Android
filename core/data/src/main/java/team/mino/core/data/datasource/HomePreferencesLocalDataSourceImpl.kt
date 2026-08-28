package team.mino.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 홈의 영속 값을 공유 `DataStore<Preferences>`에 보관한다.
 *
 * 읽기는 흐름을 구독하지 않고 현재 값을 한 번만 본다 — 두 값 모두 홈 진입 시점의 판정에 쓰이고, 바뀌는 순간
 * 화면이 따라가야 할 값이 아니다.
 *
 * 저장 실패(디스크 이상 등)는 잡지 않고 그대로 전파한다.
 */
internal class HomePreferencesLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : HomePreferencesLocalDataSource {
    override suspend fun getLastRoomId(): String? = dataStore.data.first()[LAST_ROOM_ID_KEY]

    override suspend fun setLastRoomId(roomId: String) {
        dataStore.edit { preferences -> preferences[LAST_ROOM_ID_KEY] = roomId }
    }

    override suspend fun isGuideDismissed(): Boolean = dataStore.data.first()[GUIDE_DISMISSED_KEY] ?: false

    override suspend fun dismissGuide() {
        dataStore.edit { preferences -> preferences[GUIDE_DISMISSED_KEY] = true }
    }

    private companion object {
        val LAST_ROOM_ID_KEY = stringPreferencesKey("home_last_room_id")
        val GUIDE_DISMISSED_KEY = booleanPreferencesKey("home_guide_dismissed")
    }
}
