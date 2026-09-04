package team.mino.core.notification

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat

/**
 * 모든 유형의 시스템 알림이 공유하는 단일 채널(FR-008·UX-006).
 *
 * 앱 시작 시점이 아니라 알림을 만들기 직전 [ensureCreated]로 생성한다(research.md D3).
 * `createNotificationChannel`은 이미 있는 채널에 다시 호출해도 안전하므로 호출마다
 * 존재 여부를 따로 확인하지 않는다.
 */
internal object PushNotificationChannel {
    internal const val ID = "push"

    fun ensureCreated(context: Context) {
        val channel = NotificationChannelCompat
            .Builder(ID, NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName(context.getString(R.string.push_notification_channel_name))
            .build()
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }
}
