package team.mino.core.navigation.activity

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import kotlinx.serialization.KSerializer

/**
 * [ActivityLauncher] 구현 시 공통 동작(대상 Activity [Intent] 생성, 진입 인자 직렬화 주입, 실행)을 제공한다.
 *
 * feature `:impl`의 Launcher 구현체가 대상 Activity 클래스와 인자 타입을 지정해 상속한다.
 * 인자는 JSON 문자열로 [Intent] extra에 실려, 모듈 간 모델 공유 없이 type-safe 하게 전달된다.
 * 대상 Activity는 [activityArgsOrNull]로 같은 인자 타입을 복원한다.
 */
abstract class BaseActivityLauncher<ARGS>(
    private val argsSerializer: KSerializer<ARGS>,
) : ActivityLauncher<ARGS> {
    protected abstract fun createIntent(context: Context): Intent

    override fun launch(
        context: Context,
        args: ARGS,
        resultLauncher: ActivityResultLauncher<Intent>?,
    ) {
        val intent = createIntent(context)
            .putExtra(
                EXTRA_ACTIVITY_ARGS,
                activityArgsJson.encodeToString(argsSerializer, args),
            )

        if (resultLauncher != null) {
            resultLauncher.launch(intent)
        } else {
            context.startActivity(intent)
        }
    }
}
