package team.mino.feature.splash.main.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import team.mino.core.common.android.architecture.MviContainer
import team.mino.core.common.android.architecture.mviContainer
import team.mino.core.common.android.extension.launchSafely
import team.mino.core.domain.model.SplashEntry
import team.mino.core.domain.usecase.EnsureAnonymousSessionUseCase
import team.mino.core.domain.usecase.ResolveSplashEntryUseCase
import team.mino.core.errorhandling.DomainErrorEmitter
import team.mino.core.errorhandling.domainErrorEmitter
import team.mino.core.errorhandling.onDomainFailure
import team.mino.core.errorhandling.runCatchingDomain
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
internal class SplashViewModel @Inject constructor(
    private val ensureAnonymousSession: EnsureAnonymousSessionUseCase,
    private val resolveSplashEntry: ResolveSplashEntryUseCase,
) : ViewModel(),
    MviContainer<SplashUiState, SplashSideEffect> by mviContainer(SplashUiState()),
    DomainErrorEmitter by domainErrorEmitter() {
    /**
     * 진입 작업은 화면당 한 번만 돈다. 구성 변경으로 컴포지션이 다시 만들어지면 [SplashIntent.Start]가
     * 다시 오는데, 그때 두 번째 작업을 띄우면 전환도 두 번 발행된다.
     */
    private var startJob: Job? = null

    /** 직전 안내로부터 [NOTICE_INTERVAL]이 지날 때까지 살아 있는 작업. 살아 있는 동안은 발행하지 않는다. */
    private var noticeCooldown: Job? = null

    fun processIntent(intent: SplashIntent) {
        when (intent) {
            SplashIntent.Start -> start()
        }
    }

    /**
     * 최소 노출과 진입 지점 확보를 **독립 작업으로 띄우고 둘 다 끝났을 때** 전환한다
     * (research.md R-004). 순서대로 이으면 둘 중 느린 쪽이 아니라 합이 대기 시간이 된다.
     *
     * 지연 안내([indicateProgress])는 확보되는 순간 취소되므로, 정상 속도에서는 스피너도 안내도
     * 한 번 뜨지 않는다. 전환은 [awaitEntry]가 값을 돌려준 뒤에만 일어난다(호출자 계약 C-2).
     */
    private fun start() {
        if (startJob != null) return

        startJob = launchSafely {
            val minimumExposure = async { delay(MINIMUM_EXPOSURE) }
            val progress = launch { indicateProgress() }
            val entry = async { awaitEntry() }

            val destination = entry.await()
            progress.cancel()
            minimumExposure.await()

            updateState { copy(isSpinnerVisible = false) }
            postSideEffect(SplashSideEffect.NavigateTo(destination))
        }
    }

    /**
     * 최소 노출이 끝나는 시점에도 확보되지 않았으면 스피너를 올리고, 거기서 [SPINNER_TIMEOUT]을 더
     * 기다려도 끝나지 않으면 스피너를 내리고 대기를 접었음을 알린다.
     *
     * 이 안내는 도메인 예외 없이 시간만으로 나므로 SideEffect로 흘린다(에러 처리 규약 §5).
     *
     * `withTimeout`으로 임계를 걸지 않는다 — `TimeoutCancellationException`은 `CancellationException`이라
     * 도메인 예외 경로를 타지 않고 CEH로 새며, 확보 작업까지 취소해 재시도를 끊는다(research.md R-004·R-013).
     */
    private suspend fun indicateProgress() {
        delay(MINIMUM_EXPOSURE)
        updateState { copy(isSpinnerVisible = true) }

        delay(SPINNER_TIMEOUT)
        updateState { copy(isSpinnerVisible = false) }
        if (claimNoticeSlot()) postSideEffect(SplashSideEffect.EntryTimedOut)
    }

    /**
     * 진입 지점이 확보될 때까지 자동으로 재시도한다. 횟수 상한은 두지 않는다(호출자 계약 C-4).
     *
     * 시도를 [launchSafely]로 띄우고 **그 완료만** 기다리는 이유는 루프를 도메인 예외 수신에
     * 종속시키지 않기 위해서다(호출자 계약 C-5·R-013). 매핑되지 않은 실패는 CEH가 받고 시도 작업은
     * 그대로 완료되므로, 그런 실패에도 루프가 조용히 끝나지 않는다.
     *
     * 실패한 리프는 문구가 아니라 그대로 방출하고, 사용자 문구는 Route가 고른다(에러 처리 규약 §5).
     */
    private suspend fun awaitEntry(): SplashEntry {
        while (true) {
            var resolved: SplashEntry? = null
            launchSafely {
                val attempt = runCatchingDomain {
                    ensureAnonymousSession()
                    resolveSplashEntry()
                }
                attempt.onDomainFailure { if (claimNoticeSlot()) emitDomainError(it) }
                resolved = attempt.getOrNull()
            }.join()

            resolved?.let { return it }
            delay(RETRY_INTERVAL)
        }
    }

    /**
     * 안내를 지금 발행해도 되는지 묻고, 되면 다음 [NOTICE_INTERVAL]을 예약한다(UX-006).
     *
     * 통로가 둘(SideEffect·[DomainErrorEmitter])이어도 사용자가 보는 자리는 하나이므로 간격은
     * 통로를 가리지 않고 함께 센다. 억제는 각 통로로 나가기 **전에** 걸린다.
     */
    private fun claimNoticeSlot(): Boolean {
        if (noticeCooldown?.isActive == true) return false

        noticeCooldown = launchSafely { delay(NOTICE_INTERVAL) }
        return true
    }

    private companion object {
        val MINIMUM_EXPOSURE = 3.seconds
        val SPINNER_TIMEOUT = 10.seconds
        val NOTICE_INTERVAL = 10.seconds

        /** 상한 없는 루프라 고정 간격으로 둔다. 백오프는 서버 부하가 실제로 문제가 될 때 넣는다. */
        val RETRY_INTERVAL = 3.seconds
    }
}
