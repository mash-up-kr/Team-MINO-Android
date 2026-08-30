package team.mino.core.designsystem.util.modifier.clickable

import android.os.SystemClock

internal class MultipleEventsCutter(
    private val intervalMillis: Long = DEFAULT_INTERVAL_MILLIS,
    private val currentTimeMillis: () -> Long = SystemClock::uptimeMillis,
) {
    private var lastEventTimeMillis: Long? = null

    fun processEvent(): Boolean {
        val now = currentTimeMillis()
        val last = lastEventTimeMillis
        return if (last == null || now - last >= intervalMillis) {
            lastEventTimeMillis = now
            true
        } else {
            false
        }
    }

    companion object {
        const val DEFAULT_INTERVAL_MILLIS: Long = 300L
    }
}
