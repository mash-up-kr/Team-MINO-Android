package team.mino.core.navigation.activity

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import kotlinx.serialization.KSerializer

internal const val EXTRA_ACTIVITY_RESULT = "team.mino.core.navigation.ACTIVITY_RESULT"

/**
 * 대상 Activity가 호출부에 돌려줄 결과를 직렬화해 설정한다. 호출부는 [resultOrNull]로 복원한다.
 */
fun <RESULT> Activity.setActivityResult(
    serializer: KSerializer<RESULT>,
    result: RESULT,
) {
    setResult(
        Activity.RESULT_OK,
        Intent().putExtra(EXTRA_ACTIVITY_RESULT, activityArgsJson.encodeToString(serializer, result)),
    )
}

/**
 * `registerForActivityResult(StartActivityForResult())` 콜백에서 받은 결과를 역직렬화한다.
 *
 * `RESULT_OK`가 아니거나 결과가 없으면 `null`을 반환한다.
 */
fun <RESULT> ActivityResult.resultOrNull(serializer: KSerializer<RESULT>): RESULT? {
    if (resultCode != Activity.RESULT_OK) return null
    return data
        ?.getStringExtra(EXTRA_ACTIVITY_RESULT)
        ?.let { activityArgsJson.decodeFromString(serializer, it) }
}
