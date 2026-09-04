package team.mino.core.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import team.mino.core.domain.model.PushMessage
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/**
 * [PushMessage]와 탭 시 실행할 [PendingIntent]를 시스템 알림으로 조립한다.
 *
 * 문구는 서버가 완성해 보낸 것이라 가공 없이 그대로 싣고(FR-007), `imageUrl`이 있으면
 * `BigPictureStyle`로 확장 이미지를 붙인다(contracts/push-payload-contract.md §3).
 * 이미지 다운로드 실패는 알림 표시를 막지 않는다 — 이미지 없이 기본 스타일로 떨어진다(FR-006).
 */
internal class PushNotificationBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun build(
        message: PushMessage,
        contentIntent: PendingIntent,
    ): Notification {
        val builder = NotificationCompat
            .Builder(context, PushNotificationChannel.ID)
            // 상태 표시줄 전용 모노크롬 아이콘이 아직 없어 런처 아이콘을 임시 재사용한다(research.md D11).
            // `:app`의 R은 이 모듈에서 보이지 않으므로 PackageManager가 아는 아이콘 ID를 런타임에 얻는다.
            .setSmallIcon(context.applicationInfo.icon)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        message.imageUrl
            ?.let { downloadBitmap(it) }
            ?.let { builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(it)) }

        return builder.build()
    }

    /**
     * 다운로드 전체를 [IMAGE_TIMEOUT_MILLIS]로 감싼다 — `readTimeout`은 read 1회당 예산이라
     * 느리게 흘러오는 응답에는 상한이 되지 않고, 그러면 FCM 콜백 스레드가 무한정 잡힌다.
     */
    private suspend fun downloadBitmap(url: String): Bitmap? =
        withTimeoutOrNull(IMAGE_TIMEOUT_MILLIS.toLong()) {
            withContext(Dispatchers.IO) {
                try {
                    val connection =
                        (URL(url).openConnection() as? HttpURLConnection ?: return@withContext null).apply {
                            connectTimeout = IMAGE_TIMEOUT_MILLIS
                            readTimeout = IMAGE_TIMEOUT_MILLIS
                        }
                    try {
                        connection.inputStream.use(BitmapFactory::decodeStream)
                    } finally {
                        connection.disconnect()
                    }
                } catch (_: IOException) {
                    null
                }
            }
        }

    private companion object {
        // FCM 서비스 콜백의 실행 예산 안에서 끝나도록 짧게 잡는다. 초과하면 이미지만 포기한다.
        const val IMAGE_TIMEOUT_MILLIS = 5_000
    }
}
