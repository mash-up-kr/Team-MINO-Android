package team.mino.logging

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import timber.log.Timber

/**
 * WARN 이상 로그만 Crashlytics로 보내는 릴리즈용 트리.
 * 메시지는 크래시·non-fatal 리포트에 붙는 브레드크럼으로 남고,
 * throwable은 non-fatal 이벤트로 기록된다.
 */
class CrashlyticsTree : Timber.Tree() {
    override fun isLoggable(
        tag: String?,
        priority: Int,
    ): Boolean = priority >= Log.WARN

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        val crashlytics = Firebase.crashlytics
        crashlytics.log(message)
        t?.let(crashlytics::recordException)
    }
}
