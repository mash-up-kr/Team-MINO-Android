package team.mino.feature.profile.fake

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import team.mino.core.domain.model.Profile
import team.mino.core.domain.repository.ProfileRepository
import team.mino.core.errorhandling.MinoDomainException

/**
 * `:feature:profile` 테스트용 [ProfileRepository] 테스트 더블.
 *
 * 저장된 값을 미리 채워 두고, 저장을 실패시키거나 저장이 끝나지 않는 상태를 만들 수 있다.
 *
 * [saveFailure]의 타입이 [MinoDomainException]인 것은 편의가 아니라 계약이다 — ViewModel의
 * `runCatchingDomain`은 이 타입만 잡으므로, 다른 예외를 주입하면 실패 통로가 아니라 CEH로 빠져
 * 판정하려던 경로를 지나가지 않는다. `:core:domain`의 같은 이름 더블이 평범한 `Throwable`을 쓰는 것은
 * 그쪽 UseCase가 "잡지 않고 흘려보내는가"만 보기 때문이다.
 */
internal class FakeProfileRepository : ProfileRepository {
    private val profile = MutableStateFlow<Profile?>(null)

    /** [saveProfile]에 마지막으로 들어온 값. 저장이 일어나지 않았으면 `null`이다. */
    var savedProfile: Profile? = null
        private set

    /** [saveProfile]이 호출된 횟수. 저장 중 두 번째 요청이 막히는지(EC-004) 보는 데 쓴다. */
    var saveCallCount: Int = 0
        private set

    /** 값이 있으면 [saveProfile]이 저장 대신 이 예외를 던진다. */
    var saveFailure: MinoDomainException? = null

    /** 값이 있으면 [saveProfile]이 이것이 완료될 때까지 멈춘다 — 저장 중(`isSaving`) 상태를 붙잡아 둔다. */
    var saveGate: CompletableDeferred<Unit>? = null

    /** 이미 저장된 프로필이 있는 상태를 만든다. */
    fun givenProfile(profile: Profile?) {
        this.profile.value = profile
        savedProfile = profile
    }

    override fun observeProfile(): Flow<Profile?> = profile

    override suspend fun saveProfile(profile: Profile) {
        saveCallCount++
        saveGate?.await()
        saveFailure?.let { throw it }
        savedProfile = profile
        this.profile.value = profile
    }
}
