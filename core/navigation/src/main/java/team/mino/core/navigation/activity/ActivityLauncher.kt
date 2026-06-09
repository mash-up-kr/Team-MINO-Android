package team.mino.core.navigation.activity

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

/**
 * feature 간 Activity 전환 진입점의 공통 계약.
 *
 * 각 feature는 `:api`에서 `interface XLauncher : ActivityLauncher<XArgs>` 형태로만 노출하고,
 * `:impl`은 [BaseActivityLauncher]를 상속해 대상 Activity만 지정한다.
 * 다른 feature는 그 `:api`에만 의존해 전환한다(`:impl` 직접 의존 금지).
 *
 * [resultLauncher]를 넘기면 결과를 돌려받는 전환, 생략하면 fire-and-forget 전환이다.
 */
interface ActivityLauncher<ARGS> {
    fun launch(
        context: Context,
        args: ARGS,
        resultLauncher: ActivityResultLauncher<Intent>? = null,
    )
}
