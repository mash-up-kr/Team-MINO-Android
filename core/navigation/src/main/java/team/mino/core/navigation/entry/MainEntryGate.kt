package team.mino.core.navigation.entry

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 「이 프로세스에서 스플래시가 Main 진입을 확정했다」는 사실 하나를 기억하는 자리.
 *
 * 알림의 `PendingIntent`는 앱 실행 상태와 무관하게 항상 `MainActivity`를 겨냥한다. 프로세스가
 * 죽은 채로 알림을 누르면 `MainActivity`가 스플래시(세션 확보·진입 판정·최소 노출)를 건너뛰고 뜨게
 * 되므로, `MainActivity.onCreate`는 `setContent` 전에 이 게이트를 읽어 아직 통과 표시가 아니면
 * extra를 그대로 실어 `SplashActivity`로 우회한다
 * (`docs/specs/push-notification/contracts/push-deeplink-contract.md` §3·§4).
 *
 * 켜는 쪽은 `SplashActivity`뿐이다 — `SplashEntry.Main`으로 전환하며 `MainLauncher`를 부르기
 * 직전에 [markPassed]한다. 온보딩 갈래에서는 켜지 않는다. 온보딩을 마친 뒤 Main으로 갈 때도
 * 스플래시를 다시 지나므로 그 시점에 켜진다.
 *
 * 온보딩·프로필 판정을 담지 않는다. 그 판정은 온보딩이 소유하고 스플래시가 소비한다
 * (`docs/adr/2026-08-29-onboarding-entry-decision-owned-by-onboarding.md`). 이 게이트는 스플래시가
 * 이미 내린 결론을 기억할 뿐이다.
 *
 * 스코프가 `Singleton`인 이유는 이것이 Activity 하나가 아니라 프로세스의 사실이기 때문이다.
 * 프로세스가 죽으면 함께 사라지며, 그것이 「다음 콜드 진입은 다시 스플래시를 지난다」는 의도다.
 */
@Singleton
class MainEntryGate @Inject constructor() {
    /** 이 프로세스에서 스플래시가 Main 진입을 확정했는가. */
    @Volatile
    var isPassed: Boolean = false
        private set

    /** 스플래시가 Main 진입을 확정했다. 되돌리는 API는 없다 — 프로세스 종료가 유일한 초기화다. */
    fun markPassed() {
        isPassed = true
    }
}
