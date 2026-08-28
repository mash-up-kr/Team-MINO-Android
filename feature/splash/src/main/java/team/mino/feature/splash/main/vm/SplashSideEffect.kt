package team.mino.feature.splash.main.vm

import team.mino.core.common.android.architecture.SideEffect
import team.mino.core.domain.model.SplashEntry

internal sealed interface SplashSideEffect : SideEffect {
    data class NavigateTo(val entry: SplashEntry) : SplashSideEffect

    /**
     * 확보를 기다리는 시간이 임계를 넘겨 대기를 접었다.
     *
     * 도메인 예외 없이 시간만으로 나는 이 화면 고유의 사건이라 여기로 흘린다 — 도메인 예외의
     * 일회성 안내는 `DomainErrorEmitter`가 맡는다(에러 처리 규약 §5).
     */
    data object EntryTimedOut : SplashSideEffect
}
