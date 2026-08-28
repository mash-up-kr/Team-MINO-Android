package team.mino.core.domain.model

/**
 * 스플래시 다음에 진입할 지점. `ResolveSplashEntryUseCase`의 반환 타입이다.
 *
 * 실패는 이 타입으로 표현하지 않는다 — 실패는 `MinoDomainException`으로 던져진다.
 * 세션이 확보되지 않은 상태에서는 이 값이 아예 만들어지지 않는다.
 */
sealed interface SplashEntry {
    /** 프로필이 없다. 프로필 설정으로 시작하는 온보딩으로 간다. */
    data object Onboarding : SplashEntry

    /** 프로필이 있다. 메인으로 간다. */
    data object Main : SplashEntry
}
