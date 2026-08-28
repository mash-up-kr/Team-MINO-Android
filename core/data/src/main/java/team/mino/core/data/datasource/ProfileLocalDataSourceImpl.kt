package team.mino.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import team.mino.core.domain.model.Profile
import javax.inject.Inject

/**
 * 프로필을 공유 `DataStore<Preferences>`에 보관한다. 키와 미저장 판정은 `docs/specs/profile/data-model.md` §3이 소유한다.
 *
 * 저장 실패(디스크 이상 등)는 잡지 않고 그대로 전파한다.
 */
internal class ProfileLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ProfileLocalDataSource {
    override fun observeProfile(): Flow<Profile?> =
        dataStore.data.map { preferences ->
            val nickname = preferences[NICKNAME_KEY]
            val avatarId = preferences[AVATAR_ID_KEY]
            if (nickname == null || avatarId == null) {
                null
            } else {
                Profile(nickname = nickname, avatarId = avatarId)
            }
        }

    override suspend fun saveProfile(profile: Profile) {
        // 두 키를 같은 edit 블록에서 쓴다. 나눠 쓰면 닉네임만 바뀌고 아바타가 이전 값으로 남은 프로필이 한 번 더 흘러나간다.
        dataStore.edit { preferences ->
            preferences[NICKNAME_KEY] = profile.nickname
            preferences[AVATAR_ID_KEY] = profile.avatarId
        }
    }

    private companion object {
        val NICKNAME_KEY = stringPreferencesKey("profile_nickname")
        val AVATAR_ID_KEY = intPreferencesKey("profile_avatar_id")
    }
}
