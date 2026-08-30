package team.mino.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 온보딩 진행 상태를 공유 `DataStore<Preferences>`에 보관한다. 키 이름과 기본값은
 * `docs/specs/onboarding-flow/data-model.md` §4.1이 소유한다.
 *
 * 도메인 타입을 알지 못하며 `Preferences` ↔ [OnboardingProgressEntry]까지만 조립한다.
 * 저장 실패(디스크 이상 등)는 잡지 않고 그대로 전파한다 — 예상 가능한 실패가 아니라 버그다.
 */
internal class OnboardingProgressLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : OnboardingProgressLocalDataSource {
    override suspend fun getProgress(): OnboardingProgressEntry {
        // 같은 스냅샷에서 세 값을 읽는다. 키마다 따로 읽으면 쓰기 사이에 끼어 서로 다른 시점의 조합이 나온다.
        val preferences = dataStore.data.first()
        return OnboardingProgressEntry(
            lastStepName = preferences[LAST_STEP_KEY],
            createdRoomId = preferences[CREATED_ROOM_ID_KEY],
            isCompleted = preferences[COMPLETED_KEY] ?: false,
        )
    }

    override suspend fun setLastStepName(stepName: String) {
        dataStore.edit { preferences ->
            preferences[LAST_STEP_KEY] = stepName
        }
    }

    override suspend fun setCreatedRoomId(roomId: String) {
        dataStore.edit { preferences ->
            preferences[CREATED_ROOM_ID_KEY] = roomId
        }
    }

    override suspend fun markCompleted() {
        dataStore.edit { preferences ->
            preferences[COMPLETED_KEY] = true
        }
    }

    private companion object {
        val LAST_STEP_KEY = stringPreferencesKey("onboarding_last_step")
        val CREATED_ROOM_ID_KEY = stringPreferencesKey("onboarding_created_room_id")
        val COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    }
}
