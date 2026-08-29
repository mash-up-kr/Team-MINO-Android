package team.mino.feature.splash.main.vm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import team.mino.core.domain.model.AnonymousSession
import team.mino.core.domain.model.OnboardingProgress
import team.mino.core.domain.model.OnboardingStep
import team.mino.core.domain.model.SplashEntry
import team.mino.core.domain.repository.AnonymousAuthRepository
import team.mino.core.domain.repository.OnboardingProgressRepository
import team.mino.core.domain.repository.ProfileRegistrationRepository
import team.mino.core.domain.usecase.EnsureAnonymousSessionUseCase
import team.mino.core.domain.usecase.ResolveSplashEntryUseCase
import team.mino.core.errorhandling.MinoDomainException
import java.io.IOException
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * 스플래시의 지연 안내·실패 안내·재시도를 판정한다.
 *
 * 임계(3초·13초)와 안내 간격(10초)은 `data-model.md` §2.1 상태 전이표와
 * `contracts/splash-ui.md` §6·§7이 소유한다. 여기서는 그 값을 가상 시간으로 되짚기만 한다.
 *
 * 안내는 통로가 둘이다 — 도메인 예외는 `DomainErrorEmitter`로 리프 그대로, 시간 임계로 대기를
 * 접은 것은 [SplashSideEffect.EntryTimedOut]으로 나간다(에러 처리 규약 §5). 리프 → 문구 매핑은
 * Route가 가지므로 여기서는 어느 리프가 어느 통로로 몇 번 나가는지만 본다.
 *
 * 확보가 실패하는 시나리오에서는 `advanceUntilIdle`을 쓰지 않는다 — 재시도 루프에 상한이 없어(C-4)
 * 가상 시간이 영원히 전진한다. 같은 이유로 검증이 끝나면 [splash]가 ViewModel의 스코프를 접는다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `3초 안에 진입 지점이 확보되면 스피너가 한 번도 뜨지 않는다`() =
        runTest(testDispatcher) {
            splash(isRegistered = { true }) { recording ->
                advanceTimeBy(MINIMUM_EXPOSURE)

                // 확보는 즉시 끝났지만 최소 노출이 남아 있어 아직 전환하지 않는다(FR-005).
                assertTrue(recording.effects.isEmpty())

                advanceTimeBy(TICK)

                assertTrue(recording.states.none { it.isSpinnerVisible })
                assertEquals(listOf(SplashSideEffect.NavigateTo(SplashEntry.Main)), recording.effects)
            }
        }

    @Test
    fun `3초가 지나도 확보되지 않으면 스피너를 노출한다`() =
        runTest(testDispatcher) {
            splash(session = { awaitCancellation() }) { recording ->
                advanceTimeBy(MINIMUM_EXPOSURE)
                assertFalse(recording.isSpinnerVisible)

                advanceTimeBy(TICK)

                assertTrue(recording.isSpinnerVisible)
                assertTrue(recording.effects.isEmpty())
            }
        }

    @Test
    fun `스피너 노출 후 10초가 더 지나도 확보되지 않으면 스피너를 내리고 대기를 접는다`() =
        runTest(testDispatcher) {
            splash(session = { awaitCancellation() }) { recording ->
                advanceTimeBy(MINIMUM_EXPOSURE + SPINNER_TIMEOUT)
                assertTrue(recording.isSpinnerVisible)

                advanceTimeBy(TICK)

                assertFalse(recording.isSpinnerVisible)
                // 도메인 예외가 난 것이 아니므로 안내는 SideEffect로만 나간다.
                assertEquals(1, recording.timeoutNotices)
                assertTrue(recording.domainErrors.isEmpty())
                assertTrue(recording.effects.none { it is SplashSideEffect.NavigateTo })
            }
        }

    @Test
    fun `연결 실패는 네트워크 리프 그대로 도메인 에러 통로로 나간다`() =
        runTest(testDispatcher) {
            splash(session = { throw MinoDomainException.Network(IOException()) }) { recording ->
                advanceTimeBy(TICK)

                assertEquals(listOf(MinoDomainException.Network::class), recording.domainErrorLeaves)
                assertEquals(0, recording.timeoutNotices)
            }
        }

    @Test
    fun `그 밖의 도메인 예외도 리프 그대로 도메인 에러 통로로 나간다`() =
        runTest(testDispatcher) {
            splash(session = { throw MinoDomainException.Auth(IOException()) }) { auth ->
                splash(session = { throw MinoDomainException.Http(code = 500, cause = IOException()) }) { http ->
                    advanceTimeBy(TICK)

                    assertEquals(listOf(MinoDomainException.Auth::class), auth.domainErrorLeaves)
                    assertEquals(listOf(MinoDomainException.Http::class), http.domainErrorLeaves)
                }
            }
        }

    @Test
    fun `실패가 이어져도 안내는 10초 간격으로만 나간다`() =
        runTest(testDispatcher) {
            splash(session = { throw MinoDomainException.Network(IOException()) }) { recording ->
                // t=0에 한 번 나간 뒤 t=3·6·9초의 재시도 실패는 억제된다.
                advanceTimeBy(9500.milliseconds)
                assertEquals(1, recording.domainErrors.size)

                // 간격이 비는 것은 t=10초이므로 그 뒤 첫 재시도(t=12초)에서 다시 나간다.
                advanceTimeBy(3.seconds)

                assertEquals(2, recording.domainErrors.size)
            }
        }

    @Test
    fun `실패한 뒤에도 재시도가 이어지고 확보되면 전환한다`() =
        runTest(testDispatcher) {
            var attempts = 0
            val session: suspend () -> AnonymousSession = {
                attempts++
                if (attempts < 3) throw MinoDomainException.Network(IOException())
                AnonymousSession(USER_ID)
            }

            splash(session = session, isRegistered = { false }) { recording ->
                // t=0·3초 실패 → t=6초 성공. 최소 노출은 이미 지났으므로 그 자리에서 전환한다.
                advanceTimeBy(RETRY_INTERVAL * 2 + TICK)

                assertEquals(3, recording.sessionCallCount)
                assertEquals(SplashEntry.Onboarding, recording.navigatedEntry)
                assertFalse(recording.isSpinnerVisible)
            }
        }

    /**
     * 호출자 계약 C-5 회귀(research.md R-013 · quickstart.md §5).
     *
     * 도메인 예외로 매핑되지 않는 실패는 `runCatchingDomain`이 잡지 않고 CEH로 샌다. 재시도 루프가
     * 실패 콜백에만 걸려 있으면 여기서 조용히 끝나 화면이 안내도 재시도도 없이 멈춘다.
     */
    @Test
    fun `도메인 예외로 매핑되지 않는 실패에도 재시도 루프가 끊기지 않는다`() =
        runTest(testDispatcher) {
            splash(session = { error("매핑되지 않은 실패") }) { recording ->
                advanceTimeBy(MINIMUM_EXPOSURE + SPINNER_TIMEOUT + TICK)

                // t=0·3·6·9·12초의 다섯 번. 루프가 첫 실패에서 끊겼다면 한 번에 그친다.
                assertEquals(5, recording.sessionCallCount)
                // 매핑되지 않은 실패는 도메인 에러 통로를 타지 않으므로 13초 임계가 사용자에게 닿는 유일한 안내다.
                assertTrue(recording.domainErrors.isEmpty())
                assertEquals(1, recording.timeoutNotices)
                assertFalse(recording.isSpinnerVisible)
            }
        }

    /**
     * ViewModel을 세우고 [SplashIntent.Start]까지 보낸 뒤 [assertions]를 부른다.
     *
     * 수집을 인텐트보다 먼저 걸어 둔다 — 채널로 나가는 일회성 신호와 중간 상태는 놓치면 되돌릴 수 없다.
     * 검증이 끝나면 ViewModel의 스코프를 접는다. 상한 없는 재시도 루프를 남겨 두면 `runTest`가
     * 본문 뒤에 스케줄러를 비우는 단계에서 끝나지 않는다.
     */
    private fun TestScope.splash(
        session: suspend () -> AnonymousSession = { AnonymousSession(USER_ID) },
        isRegistered: suspend () -> Boolean = { true },
        isOnboardingCompleted: suspend () -> Boolean = { true },
        assertions: (Recording) -> Unit,
    ) {
        val authRepository = FakeAnonymousAuthRepository(session)
        val viewModel =
            SplashViewModel(
                ensureAnonymousSession = EnsureAnonymousSessionUseCase(authRepository),
                resolveSplashEntry =
                    ResolveSplashEntryUseCase(
                        profileRegistrationRepository = FakeProfileRegistrationRepository(isRegistered),
                        onboardingProgressRepository = FakeOnboardingProgressRepository(isOnboardingCompleted),
                    ),
            )

        val states = mutableListOf<SplashUiState>()
        val effects = mutableListOf<SplashSideEffect>()
        val domainErrors = mutableListOf<MinoDomainException>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.toList(states) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.sideEffect.toList(effects) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.domainErrors.toList(domainErrors) }

        viewModel.processIntent(SplashIntent.Start)
        try {
            assertions(Recording(states, effects, domainErrors, authRepository))
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    private class Recording(
        val states: List<SplashUiState>,
        val effects: List<SplashSideEffect>,
        val domainErrors: List<MinoDomainException>,
        private val authRepository: FakeAnonymousAuthRepository,
    ) {
        val isSpinnerVisible: Boolean get() = states.last().isSpinnerVisible

        /** 리프는 값 비교가 되지 않으므로 타입으로 본다. 문구 매핑은 Route가 갖는다. */
        val domainErrorLeaves: List<KClass<out MinoDomainException>>
            get() = domainErrors.map { it::class }

        val timeoutNotices: Int get() = effects.count { it == SplashSideEffect.EntryTimedOut }

        val navigatedEntry: SplashEntry?
            get() = effects.filterIsInstance<SplashSideEffect.NavigateTo>().singleOrNull()?.entry

        val sessionCallCount: Int get() = authRepository.callCount
    }

    private class FakeAnonymousAuthRepository(
        private val session: suspend () -> AnonymousSession,
    ) : AnonymousAuthRepository {
        var callCount: Int = 0
            private set

        override suspend fun ensureSession(): AnonymousSession {
            callCount++
            return session()
        }

        override suspend fun currentSession(): AnonymousSession? =
            error("스플래시는 세션을 확보한다 — 조회만 하는 currentSession()은 쓰지 않는다(research.md R-012).")
    }

    private class FakeProfileRegistrationRepository(
        private val isRegistered: suspend () -> Boolean,
    ) : ProfileRegistrationRepository {
        override suspend fun isRegistered(): Boolean = isRegistered.invoke()
    }

    /**
     * 진입 판정의 두 번째 근거. 이 화면의 관심사는 확보·지연·실패이지 판정 표가 아니므로
     * 기본값은 완료(`true`)로 둔다 — 등록 여부만으로 [SplashEntry]가 갈리던 때의 기대를 그대로 유지한다.
     * 판정 표 자체는 `:core:domain`의 `ResolveSplashEntryUseCaseTest`가 전수로 고정한다.
     */
    private class FakeOnboardingProgressRepository(
        private val isCompleted: suspend () -> Boolean,
    ) : OnboardingProgressRepository {
        override suspend fun getProgress(): OnboardingProgress = OnboardingProgress(isCompleted = isCompleted.invoke())

        override suspend fun setCurrentStep(step: OnboardingStep) = error("스플래시는 진행 상태를 쓰지 않는다")

        override suspend fun setCreatedRoomId(roomId: String) = error("스플래시는 진행 상태를 쓰지 않는다")

        override suspend fun markCompleted() = error("스플래시는 진행 상태를 쓰지 않는다")
    }

    private companion object {
        val MINIMUM_EXPOSURE = 3.seconds
        val SPINNER_TIMEOUT = 10.seconds
        val RETRY_INTERVAL = 3.seconds

        /** 임계에 걸린 작업이 실행되도록 경계를 막 넘기는 최소 전진. */
        val TICK = 1.milliseconds

        const val USER_ID = "anonymous-user"
    }
}
