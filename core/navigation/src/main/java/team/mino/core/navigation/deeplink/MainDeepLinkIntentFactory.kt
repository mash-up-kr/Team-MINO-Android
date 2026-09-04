package team.mino.core.navigation.deeplink

import android.content.Context
import android.content.Intent

/**
 * `MainActivity`를 대상으로 하는 딥링크 [Intent] 팩토리.
 *
 * `ActivityLauncher`와 달리 `startActivity`하지 않고 [Intent]만 돌려준다 — `PendingIntent`로 감싸는
 * Service Context에서도 부를 수 있어야 하기 때문이다. 인터페이스는 여기, 구현은 `:feature:main`이 갖는다.
 * 계약: docs/specs/push-notification/contracts/push-deeplink-contract.md §2, research.md D13.
 */
interface MainDeepLinkIntentFactory {
    /** `MainActivity`를 대상으로 하는 Intent를 만든다. Activity 없이 [Context]만으로 호출 가능해야 한다. */
    fun create(context: Context): Intent
}
