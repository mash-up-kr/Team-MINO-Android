package team.mino.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 프로필 캐시를 공유 `DataStore<Preferences>`에 보관한다. 키와 미저장 판정은 `docs/specs/profile/data-model.md` §3이 소유한다.
 *
 * 원천은 서버이고 여기 담긴 값은 캐시다. 도메인 타입을 알지 못하며 `Preferences` ↔ [ProfileEntry]까지만 조립한다.
 * 저장 실패(디스크 이상 등)는 잡지 않고 그대로 전파한다.
 */
internal class ProfileLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ProfileLocalDataSource {
    override fun observeProfile(): Flow<ProfileEntry?> =
        dataStore.data.map { preferences ->
            val nickname = preferences[NICKNAME_KEY]
            val avatarName = preferences[AVATAR_KEY]
            if (nickname == null || avatarName == null) {
                null
            } else {
                ProfileEntry(nickname = nickname, avatarName = avatarName)
            }
        }

    override suspend fun saveProfile(entry: ProfileEntry) {
        // 두 키를 같은 edit 블록에서 쓴다. 나눠 쓰면 닉네임만 바뀌고 아바타가 이전 값으로 남은 프로필이 한 번 더 흘러나간다.
        dataStore.edit { preferences ->
            preferences[NICKNAME_KEY] = entry.nickname
            preferences[AVATAR_KEY] = entry.avatarName
        }
    }

    override suspend fun clearProfile() {
        // 지우는 것도 한 트랜잭션이다. 나눠 지우면 한쪽 키만 남은 캐시가 잠깐 관측된다.
        dataStore.edit { preferences ->
            preferences.remove(NICKNAME_KEY)
            preferences.remove(AVATAR_KEY)
        }
    }

    private companion object {
        val NICKNAME_KEY = stringPreferencesKey("profile_nickname")
        val AVATAR_KEY = stringPreferencesKey("profile_avatar")
    }
}
