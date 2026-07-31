package team.mino.core.navigation.activity

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

/**
 * feature 간 Activity 전환 진입점의 공통 계약.
 *
 * 각 feature는 `launcher` 패키지에 `interface XLauncher : ActivityLauncher`로 계약만 노출하고,
 * 구현은 대상 feature 모듈이 [BaseActivityLauncher]를 상속해 대상 Activity만 지정한다.
 */
interface ActivityLauncher {
    /**
     * 대상 화면으로 전환한다.
     *
     * 진입 인자는 [intentBuilder]로 [Intent]에 실어 보내고, 결과가 필요하면 [resultLauncher]를 넘긴다.
     * [withFinish]는 전환 이후 호출한 화면이 백스택에 남아 있으면 안 될 때 쓴다 — 되돌아왔을 때
     * 보여줄 것이 없거나, 되돌아오는 것 자체가 플로우상 잘못된 진입이다. 종료된 Activity는 결과를
     * 받을 수 없으므로 [resultLauncher]와 함께 쓰지 않는다.
     *
     * 첫 인자를 [Activity]로 좁힌 것은 [withFinish]가 호출자 종료를 요구하고,
     * 비-Activity Context로 `startActivity`를 호출하면 `FLAG_ACTIVITY_NEW_TASK` 없이 실패하기 때문이다.
     */
    fun launch(
        activity: Activity,
        resultLauncher: ActivityResultLauncher<Intent>? = null,
        withFinish: Boolean = false,
        intentBuilder: (Intent.() -> Intent)? = null,
    )
}
