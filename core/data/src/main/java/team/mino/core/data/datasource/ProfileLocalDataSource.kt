package team.mino.core.data.datasource

import kotlinx.coroutines.flow.Flow

internal interface ProfileLocalDataSource {
    /** 로컬에 캐시된 프로필을 흘린다. 캐시가 없으면 `null`이며, 캐시가 바뀔 때마다 새 값을 흘린다. */
    fun observeProfile(): Flow<ProfileEntry?>

    /** 닉네임과 아바타 이름을 함께 덮어쓴다. 둘 중 하나만 반영된 중간 상태는 관측되지 않는다. */
    suspend fun saveProfile(entry: ProfileEntry)

    /** 캐시를 비운다. 두 값을 함께 지우므로 한쪽만 남은 상태는 관측되지 않는다. */
    suspend fun clearProfile()
}
