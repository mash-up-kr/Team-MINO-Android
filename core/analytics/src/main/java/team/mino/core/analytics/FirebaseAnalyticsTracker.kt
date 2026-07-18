package team.mino.core.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

internal class FirebaseAnalyticsTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AnalyticsTracker {
    override fun logEvent(
        name: String,
        params: Map<String, Any>,
    ) {
        firebaseAnalytics.logEvent(name, params.toBundle())
    }

    override fun logScreenView(
        screenName: String,
        screenClass: String?,
    ) {
        firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass ?: screenName)
            },
        )
    }

    /**
     * Firebase Analytics 파라미터로 지원하는 타입만 [Bundle]에 담는다.
     * 지원하지 않는 타입은 `toString()`으로 떨어뜨려 이벤트 자체가 유실되지 않게 한다.
     */
    private fun Map<String, Any>.toBundle(): Bundle =
        Bundle().apply {
            forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Float -> putFloat(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
}
