package team.mino.core.notification

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import team.mino.core.domain.model.PushDestination
import team.mino.core.domain.model.PushMessage
import team.mino.core.domain.usecase.ParsePushMessageUseCase
import team.mino.core.domain.usecase.RegisterPushTokenUseCase
import team.mino.core.domain.usecase.ResolvePushDestinationUseCase
import team.mino.core.navigation.activity.launcher.EXTRA_PUSH_DESTINATION_ID
import team.mino.core.navigation.activity.launcher.EXTRA_PUSH_DESTINATION_TYPE
import team.mino.core.navigation.activity.launcher.PUSH_DESTINATION_TYPE_NOTIFICATION_TAB
import team.mino.core.navigation.activity.launcher.PUSH_DESTINATION_TYPE_PLACE
import team.mino.core.navigation.activity.launcher.PUSH_DESTINATION_TYPE_ROOM
import team.mino.core.navigation.deeplink.MainDeepLinkIntentFactory
import javax.inject.Inject

/**
 * FCM 진입점. 토큰 갱신은 서버 재등록으로, 수신 메시지는 시스템 알림으로 옮긴다.
 *
 * 두 콜백 모두 Firebase가 백그라운드 워커 스레드에서 부르고, 콜백이 반환되면 서비스는 스스로 멈춘다.
 * 그래서 코루틴을 띄워 두지 않고 `runBlocking`으로 콜백 안에서 끝낸다 — 띄워 둔 작업은 서비스가
 * 멈추는 순간 갈 곳을 잃는다. 콜백의 실행 예산은 [PushNotificationBuilder]의 타임아웃이 지킨다.
 *
 * 앱이 화면에 있는지는 보지 않는다(FR-011) — 알림은 상태와 무관하게 똑같이 뜨고, 탭 시점의 판정은
 * `MainActivity`가 한다(research.md D13).
 */
@AndroidEntryPoint
class MinoFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var registerPushToken: RegisterPushTokenUseCase

    @Inject
    lateinit var parsePushMessage: ParsePushMessageUseCase

    @Inject
    lateinit var resolvePushDestination: ResolvePushDestinationUseCase

    @Inject
    lateinit var mainDeepLinkIntentFactory: MainDeepLinkIntentFactory

    @Inject
    internal lateinit var notificationBuilder: PushNotificationBuilder

    /**
     * 인자로 온 [token]은 쓰지 않는다. 등록 경로를 앱 시작과 하나로 유지하기 위해 UseCase가 토큰을 다시
     * 조회한다(research.md D5). 실패는 Repository가 삼키므로 여기서 잡을 것이 없다.
     */
    override fun onNewToken(token: String) {
        runBlocking { registerPushToken() }
    }

    /** `notification` 페이로드는 읽지 않는다 — 서버는 data-only 메시지를 보낸다(research.md D1). */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val message = parsePushMessage(remoteMessage.data)
        // 모르는 유형은 조용히 버린다(EC-008, contracts/push-payload-contract.md §4).
        if (message.type == null) return

        val contentIntent = createContentIntent(message, resolvePushDestination(message))
        val notification = runBlocking { notificationBuilder.build(message, contentIntent) }

        PushNotificationChannel.ensureCreated(this)
        try {
            NotificationManagerCompat.from(this).notify(notificationIdOf(remoteMessage), notification)
        } catch (_: SecurityException) {
            // 권한이 없으면 알림을 띄우지 않을 뿐, 요청하거나 대체 표시를 두지 않는다(FR-014). 권한은 마이페이지가 소유한다.
        }
    }

    private fun createContentIntent(
        message: PushMessage,
        destination: PushDestination,
    ): PendingIntent {
        val intent = mainDeepLinkIntentFactory.create(this).putPushDestination(destination)
        // 도착지마다 다른 requestCode여야 PendingIntent가 서로 뭉개지지 않는다(contracts/push-deeplink-contract.md §2).
        val requestCode = (message.targetId ?: message.type?.name).hashCode()
        return PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    /** contracts/push-deeplink-contract.md §1 인코딩. `NotificationTab`은 ID를 싣지 않는다. */
    private fun Intent.putPushDestination(destination: PushDestination): Intent =
        when (destination) {
            is PushDestination.PlaceDetail -> {
                putExtra(EXTRA_PUSH_DESTINATION_TYPE, PUSH_DESTINATION_TYPE_PLACE)
                putExtra(EXTRA_PUSH_DESTINATION_ID, destination.pinId)
            }

            is PushDestination.RoomDetail -> {
                putExtra(EXTRA_PUSH_DESTINATION_TYPE, PUSH_DESTINATION_TYPE_ROOM)
                putExtra(EXTRA_PUSH_DESTINATION_ID, destination.roomId)
            }

            PushDestination.NotificationTab -> putExtra(
                EXTRA_PUSH_DESTINATION_TYPE,
                PUSH_DESTINATION_TYPE_NOTIFICATION_TAB,
            )
        }

    /**
     * 건마다 다른 ID여야 먼저 온 알림을 덮어쓰지 않는다(EC-011·UX-003). FCM 메시지 ID는 건마다 유일하므로
     * 그 해시를 쓰고, 없는 경우에만 수신 시각으로 대신한다.
     */
    private fun notificationIdOf(remoteMessage: RemoteMessage): Int =
        remoteMessage.messageId?.hashCode() ?: System.currentTimeMillis().toInt()
}
