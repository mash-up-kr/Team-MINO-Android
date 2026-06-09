package team.mino.core.navigation.activity

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

/**
 * [ActivityLauncher] 구현 시 공통 동작(대상 Activity [Intent] 생성, 인자 주입, 실행)을 제공한다.
 *
 * feature `:impl`의 Launcher 구현체는 [createIntent]에서 대상 Activity만 지정한다.
 * 호출부가 넘긴 인자([intentBuilder])는 그 Intent에 덧씌워진다.
 */
abstract class BaseActivityLauncher : ActivityLauncher {
    protected abstract fun createIntent(context: Context): Intent

    override fun launch(
        context: Context,
        resultLauncher: ActivityResultLauncher<Intent>?,
        intentBuilder: (Intent.() -> Intent)?,
    ) {
        val intent = createIntent(context).let { intentBuilder?.invoke(it) ?: it }
        if (resultLauncher != null) {
            resultLauncher.launch(intent)
        } else {
            context.startActivity(intent)
        }
    }
}
