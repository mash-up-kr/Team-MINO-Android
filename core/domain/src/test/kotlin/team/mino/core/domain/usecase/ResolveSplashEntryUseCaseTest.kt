package team.mino.core.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.domain.model.OnboardingProgress
import team.mino.core.domain.model.OnboardingStep
import team.mino.core.domain.model.SplashEntry
import team.mino.core.domain.repository.OnboardingProgressRepository
import team.mino.core.domain.repository.ProfileRegistrationRepository
import java.io.IOException

/**
 * 판정 표는 `docs/specs/splash-screen/contracts/splash-entry-decision.md` §2가, 호출 순서 제약은 같은 문서 §3과
 * `docs/adr/2026-08-29-onboarding-entry-decision-owned-by-onboarding.md` §결정 3항이 소유한다.
 *
 * 두 근거의 조합은 넷뿐이므로 전수로 고정한다 — `등록됨 + 미완료`(FR-022)가 `Main`으로 새는 것이
 * 이 판정에서 가장 비싼 오답이고, 그것만 예외적으로 다루면 표가 아니라 특례가 된다.
 *
 * 결과 assertion만으로는 [ProfileRegistrationRepository.isRegistered]가 먼저 불렸는지 알 수 없다 —
 * 순서를 뒤집어도 네 조합의 반환값이 전부 같기 때문이다. 그래서 두 Fake가 하나의 호출 기록을 공유하고,
 * 그 기록의 **첫 항목**을 본다.
 */
class ResolveSplashEntryUseCaseTest {
    private val calls = mutableListOf<String>()
    private val profileRegistrationRepository = FakeProfileRegistrationRepository(calls)
    private val onboardingProgressRepository = FakeOnboardingProgressRepository(calls)
    private val resolveSplashEntry =
        ResolveSplashEntryUseCase(
            profileRegistrationRepository = profileRegistrationRepository,
            onboardingProgressRepository = onboardingProgressRepository,
        )

    @Test
    fun `등록된 프로필도 완료 표시도 없으면 Onboarding이다`() =
        runTest {
            arrange(registered = false, completed = false)

            assertEquals(SplashEntry.Onboarding, resolveSplashEntry())
        }

    @Test
    fun `완료 표시가 있어도 등록된 프로필이 없으면 Onboarding이다`() =
        runTest {
            // 이 설치의 로컬 표시만 남고 세션이 서버에서 사라진 조합. 로컬 표시가 서버 사실을 이기지 않는다.
            arrange(registered = false, completed = true)

            assertEquals(SplashEntry.Onboarding, resolveSplashEntry())
        }

    @Test
    fun `등록된 프로필이 있어도 완료 표시가 없으면 Onboarding이다`() =
        runTest {
            // FR-022 · TS-038: 프로필 저장은 온보딩 첫 스텝일 뿐이라, 여기서 Main으로 보내면
            // 중단한 사용자가 남은 세 스텝을 영영 보지 못한다(SC-002).
            arrange(registered = true, completed = false)

            assertEquals(SplashEntry.Onboarding, resolveSplashEntry())
        }

    @Test
    fun `등록된 프로필과 완료 표시가 모두 있으면 Main이다`() =
        runTest {
            arrange(registered = true, completed = true)

            assertEquals(SplashEntry.Main, resolveSplashEntry())
        }

    @Test
    fun `두 근거를 모두 읽을 때 등록 여부를 완료 표시보다 먼저 조회한다`() =
        runTest {
            arrange(registered = true, completed = true)

            resolveSplashEntry()

            assertEquals(listOf(IS_REGISTERED, GET_PROGRESS), calls)
        }

    @Test
    fun `네 조합 어디서도 등록 여부 조회가 첫 호출이다`() =
        runTest {
            // isRegistered()는 미등록 판정 시 프로필 로컬 캐시를 비우는 부수 효과를 갖고,
            // :feature:profile의 ProfileEntryPoint.needsRefresh가 그 보장에 기댄다. 완료 표시를 먼저 읽어도
            // 반환값은 넷 다 그대로라 결과 assertion으로는 잡히지 않는다.
            // (완료 표시 조회를 미등록에서 생략하는 것은 계약 §3이 구현 재량으로 열어 둔 부분이라 고정하지 않는다.)
            for (registered in listOf(false, true)) {
                for (completed in listOf(false, true)) {
                    calls.clear()
                    arrange(registered = registered, completed = completed)

                    resolveSplashEntry()

                    assertEquals("registered=$registered, completed=$completed", IS_REGISTERED, calls.firstOrNull())
                }
            }
        }

    @Test
    fun `등록 여부 조회 실패는 Onboarding으로 오판하지 않고 그대로 전파한다`() =
        runTest {
            // EC-004: 실패를 "프로필 없음"으로 뭉개면 기존 사용자가 온보딩으로 떨어진다.
            val failure = IOException("네트워크 실패")
            profileRegistrationRepository.failure = failure

            assertPropagates(failure)
        }

    @Test
    fun `인증 실패도 Onboarding으로 오판하지 않고 그대로 전파한다`() =
        runTest {
            val failure = IllegalStateException("세션 없음")
            profileRegistrationRepository.failure = failure

            assertPropagates(failure)
        }

    @Test
    fun `완료 표시 조회 실패는 Main으로 오판하지 않고 그대로 전파한다`() =
        runTest {
            // 등록은 확인됐고 완료 표시만 읽지 못한 상태. 로컬 조회 실패를 "완료"로도 "미완료"로도 뭉개지 않는다 —
            // 로컬 저장 읽기 실패는 도메인 예외가 아니라 버그이며 CEH로 간다(계약 §4).
            val failure = IOException("로컬 저장소 읽기 실패")
            arrange(registered = true, completed = true)
            onboardingProgressRepository.failure = failure

            assertPropagates(failure)
        }

    @Test
    fun `진행 상태를 읽기만 하고 쓰지 않는다`() =
        runTest {
            arrange(registered = true, completed = false)

            resolveSplashEntry()

            assertTrue(calls.none { it in WRITE_CALLS })
        }

    private fun arrange(
        registered: Boolean,
        completed: Boolean,
    ) {
        profileRegistrationRepository.registered = registered
        onboardingProgressRepository.progress = OnboardingProgress(isCompleted = completed)
    }

    /** [failure]에 대해 UseCase가 아무것도 잡지 않고 같은 인스턴스를 흘려보내는지 — 반환값은 만들어지지 않는다. */
    private suspend fun assertPropagates(failure: Throwable) {
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

    private companion object {
        const val IS_REGISTERED = "isRegistered"
        const val GET_PROGRESS = "getProgress"
        val WRITE_CALLS = setOf("setCurrentStep", "setCreatedRoomId", "markCompleted")
    }
}

/**
 * [failure]의 타입이 계약이 선언한 `MinoDomainException.Network`·`Auth`가 아닌 평범한 [Throwable]인 것은 의도다 —
 * `:core:domain`은 `:core:error-handling`에 의존하지 않고([SaveProfileUseCaseTest]와 같은 이유),
 * [ResolveSplashEntryUseCase]에는 `catch`가 없어 실패 타입이 결과를 가르지 않는다. 여기서 고정하는 것은
 * "무엇을 던지든 삼키지 않는다"는 전파 경로다. 실패 타입이 실제로 동작을 가르는 자리는
 * `:core:data`의 `ProfileRegistrationRepositoryImplTest`와 `:feature:splash`의 ViewModel 테스트다.
 */
private class FakeProfileRegistrationRepository(
    private val calls: MutableList<String>,
) : ProfileRegistrationRepository {
    var registered: Boolean = false
    var failure: Throwable? = null

    override suspend fun isRegistered(): Boolean {
        calls += "isRegistered"
        failure?.let { throw it }
        return registered
    }
}

/** 쓰기 함수도 기록한다 — 판정이 진행 상태를 건드리지 않는다는 것까지 이 기록 하나로 본다. */
private class FakeOnboardingProgressRepository(
    private val calls: MutableList<String>,
) : OnboardingProgressRepository {
    var progress: OnboardingProgress = OnboardingProgress()
    var failure: Throwable? = null

    override suspend fun getProgress(): OnboardingProgress {
        calls += "getProgress"
        failure?.let { throw it }
        return progress
    }

    override suspend fun setCurrentStep(step: OnboardingStep) {
        calls += "setCurrentStep"
    }

    override suspend fun setCreatedRoomId(roomId: String) {
        calls += "setCreatedRoomId"
    }

    override suspend fun markCompleted() {
        calls += "markCompleted"
    }
}
