package team.mino.feature.room.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import team.mino.core.domain.model.Profile
import team.mino.core.domain.repository.ProfileRepository

/** `:feature:room` 테스트용 [ProfileRepository] 테스트 더블. 기본값은 프로필 없음(`null`)이다. */
internal class FakeProfileRepository : ProfileRepository {
    private val profile = MutableStateFlow<Profile?>(null)

    fun givenProfile(value: Profile?) {
        profile.value = value
    }

    override fun observeProfile(): Flow<Profile?> = profile

    override suspend fun refreshProfile(): Unit = Unit

    override suspend fun saveProfile(profile: Profile): Unit = error("FakeProfileRepository는 saveProfile을 지원하지 않는다.")

    override suspend fun currentUserId(): String? = null
}
