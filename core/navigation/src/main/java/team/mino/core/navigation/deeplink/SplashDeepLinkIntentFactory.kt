package team.mino.core.navigation.deeplink

import android.content.Context
import android.content.Intent

/**
 * `SplashActivity`를 대상으로 하는 딥링크 [Intent] 팩토리.
 *
 * `ActivityLauncher`와 달리 `startActivity`하지 않고 [Intent]만 돌려준다 — 호출자가 extra를 덧붙이고
 * 실행 시점·방식을 정한다. 인터페이스는 여기, 구현은 `:feature:splash`가 갖는다.
 * 계약: docs/specs/push-notification/contracts/push-deeplink-contract.md §2.
 */
interface SplashDeepLinkIntentFactory {
    /** `SplashActivity`를 대상으로 하는 Intent를 만든다. Activity 없이 [Context]만으로 호출 가능해야 한다. */
    fun create(context: Context): Intent
}
