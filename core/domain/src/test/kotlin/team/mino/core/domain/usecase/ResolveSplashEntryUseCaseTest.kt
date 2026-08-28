package team.mino.core.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import team.mino.core.domain.model.SplashEntry
import team.mino.core.domain.repository.ProfileRegistrationRepository
import java.io.IOException

class ResolveSplashEntryUseCaseTest {
    private val profileRegistrationRepository = FakeProfileRegistrationRepository()
    private val resolveSplashEntry = ResolveSplashEntryUseCase(profileRegistrationRepository)

    @Test
    fun `등록된 프로필이 없으면 Onboarding이다`() =
        runTest {
            profileRegistrationRepository.registered = false

            assertEquals(SplashEntry.Onboarding, resolveSplashEntry())
        }

    @Test
    fun `등록된 프로필이 있으면 Main이다`() =
        runTest {
            profileRegistrationRepository.registered = true

            assertEquals(SplashEntry.Main, resolveSplashEntry())
        }

    @Test
    fun `조회 실패는 Onboarding으로 오판하지 않고 그대로 전파한다`() =
        runTest {
            // EC-004: 실패를 "프로필 없음"으로 뭉개면 기존 사용자가 온보딩으로 떨어진다.
            assertPropagates(IOException("네트워크 실패"))
        }

    @Test
    fun `인증 실패도 Onboarding으로 오판하지 않고 그대로 전파한다`() =
        runTest {
            assertPropagates(IllegalStateException("세션 없음"))
        }

    /** [failure]를 던지는 Repository에 대해 UseCase가 아무것도 잡지 않고 같은 인스턴스를 흘려보내는지. */
    private suspend fun assertPropagates(failure: Throwable) {
        profileRegistrationRepository.failure = failure
        var thrown: Throwable? = null
        var returned: SplashEntry? = null

        try {
            returned = resolveSplashEntry()
        } catch (e: Throwable) {
            thrown = e
        }

        assertNull(returned)
        assertSame(failure, thrown)
    }
}

/**
 * [failure]의 타입이 계약이 선언한 `MinoDomainException.Network`·`Auth`가 아닌 평범한 [Throwable]인 것은 의도다 —
 * `:core:domain`은 `:core:error-handling`에 의존하지 않고([SaveProfileUseCaseTest]와 같은 이유),
 * [ResolveSplashEntryUseCase]에는 `catch`가 없어 실패 타입이 결과를 가르지 않는다. 여기서 고정하는 것은
 * "무엇을 던지든 삼키지 않는다"는 전파 경로다. 실패 타입이 실제로 동작을 가르는 자리는
 * `:core:data`의 `ProfileRegistrationRepositoryImplTest`와 `:feature:splash`의 ViewModel 테스트다.
 */
private class FakeProfileRegistrationRepository : ProfileRegistrationRepository {
    var registered: Boolean = false
    var failure: Throwable? = null

    override suspend fun isRegistered(): Boolean {
        failure?.let { throw it }
        return registered
    }
}
