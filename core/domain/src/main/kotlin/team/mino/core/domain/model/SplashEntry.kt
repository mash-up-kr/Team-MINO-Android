package team.mino.core.domain.model

/**
 * 스플래시 다음에 진입할 지점. `ResolveSplashEntryUseCase`의 반환 타입이다.
 *
 * 실패는 이 타입으로 표현하지 않는다 — 실패는 `MinoDomainException`으로 던져진다.
 * 세션이 확보되지 않은 상태에서는 이 값이 아예 만들어지지 않는다.
 */
sealed interface SplashEntry {
    /**
     * 프로필 등록과 온보딩 완료 표시 중 **하나라도 없다.** 온보딩으로 간다.
     *
     * 프로필 저장은 온보딩 네 스텝 중 첫 스텝일 뿐이라, 프로필이 있어도 완료 표시가 없으면 여기로 온다.
     * 어느 스텝부터 여는지는 이 타입이 가르지 않는다 — 재개 지점 판정은 온보딩이 소유한다
     * (`ResolveOnboardingStepUseCase`).
     */
    data object Onboarding : SplashEntry

    /** 프로필이 등록됐고 온보딩 완료 표시도 있다. 메인으로 간다. */
    data object Main : SplashEntry
}
