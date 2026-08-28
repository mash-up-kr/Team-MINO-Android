package team.mino.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import team.mino.core.domain.model.Profile
import team.mino.core.domain.model.ProfileAvatar
import team.mino.core.domain.repository.ProfileRepository
import java.io.IOException

class SaveProfileUseCaseTest {
    private val profileRepository = FakeProfileRepository()
    private val saveProfile =
        SaveProfileUseCase(
            profileRepository = profileRepository,
            validateNickname = ValidateNicknameUseCase(),
        )

    @Test
    fun `앞뒤 공백을 제거한 닉네임으로 저장한다`() =
        runTest {
            saveProfile(rawNickname = "  민호  ", avatar = ProfileAvatar.Person3)

            assertEquals(
                Profile(nickname = "민호", avatar = ProfileAvatar.Person3),
                profileRepository.savedProfile,
            )
        }

    @Test
    fun `무효한 닉네임은 저장까지 도달하지 않는다`() =
        runTest {
            var thrown: Throwable? = null

            try {
                saveProfile(rawNickname = "민", avatar = ProfileAvatar.entries.first())
            } catch (e: Throwable) {
                thrown = e
            }

            assertEquals(0, profileRepository.saveCallCount)
            assertNull(profileRepository.savedProfile)
            // 판정 실패는 화면이 막아야 할 프로그래머 오류다 — 예외 타입은 고정하지 않는다(계약 §UseCase).
            assertNotNull(thrown)
        }

    @Test
    fun `Repository가 던진 예외를 잡지 않고 그대로 전파한다`() =
        runTest {
            val failure = IOException("저장 실패")
            profileRepository.saveFailure = failure
            var thrown: Throwable? = null

            try {
                saveProfile(rawNickname = "민호", avatar = ProfileAvatar.entries.first())
            } catch (e: Throwable) {
                thrown = e
            }

            assertSame(failure, thrown)
        }
}

/**
 * 저장된 값을 들고 있는 [ProfileRepository] 테스트 더블.
 *
 * [saveFailure]의 타입은 의도적으로 평범한 [Throwable]이다 — 계약이 선언한 실패 타입(`MinoDomainException`)이
 * 아니라 "UseCase가 아무것도 잡지 않고 그대로 흘려보내는가"라는 전파 경로를 흔들기 위해서다. 실패 타입이 실제로
 * 동작을 가르는 자리는 `:feature:profile`의 `ProfileViewModel` 테스트이며, 거기서는 `runCatchingDomain`이
 * `MinoDomainException`만 잡아 타입이 결과를 바꾼다.
 *
 * [refreshProfile]은 인터페이스를 채우기 위해서만 있다 — `SaveProfileUseCase`가 밟는 것은 저장 경로뿐이라
 * 이 fake가 갱신을 흉내 낼 것이 없다.
 */
private class FakeProfileRepository : ProfileRepository {
    var savedProfile: Profile? = null
    var saveCallCount: Int = 0
    var saveFailure: Throwable? = null

    override fun observeProfile(): Flow<Profile?> = flowOf(savedProfile)

    override suspend fun refreshProfile() = Unit

    override suspend fun saveProfile(profile: Profile) {
        saveCallCount++
        saveFailure?.let { throw it }
        savedProfile = profile
    }

    override suspend fun currentUserId(): String? = error("이 테스트가 다루는 저장 경로는 currentUserId를 부르지 않는다.")
}
