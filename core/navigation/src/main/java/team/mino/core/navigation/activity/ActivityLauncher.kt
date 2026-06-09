package team.mino.core.navigation.activity

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

/**
 * feature 간 Activity 전환 진입점의 공통 계약.
 *
 * 각 feature는 `:api`에서 `interface XLauncher : ActivityLauncher`로만 노출하고,
 * `:impl`은 [BaseActivityLauncher]를 상속해 대상 Activity만 지정한다.
 * 진입 인자는 [intentBuilder]로 [Intent]에 직접 실어 보내고, 결과가 필요하면 [resultLauncher]를 넘긴다.
 */
interface ActivityLauncher {
    fun launch(
        context: Context,
        resultLauncher: ActivityResultLauncher<Intent>? = null,
        intentBuilder: (Intent.() -> Intent)? = null,
    )
}
