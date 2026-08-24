package team.mino.core.data.datasource

import kotlinx.coroutines.flow.Flow
import team.mino.core.domain.model.Profile

internal interface ProfileLocalDataSource {
    /** 로컬에 저장된 프로필을 흘린다. 저장된 적이 없으면 `null`이며, 저장이 일어날 때마다 새 값을 흘린다. */
    fun observeProfile(): Flow<Profile?>

    /** 닉네임과 아바타 식별자를 함께 덮어쓴다. 둘 중 하나만 반영된 중간 상태는 관측되지 않는다. */
    suspend fun saveProfile(profile: Profile)
}
