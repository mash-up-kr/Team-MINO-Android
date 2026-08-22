package team.mino.core.domain.repository

interface PushNotificationRepository {
    /**
     * 로컬에서 FCM 토큰을 얻어 서버에 등록/갱신한다. 알림 권한이 처음 허용되는 시점에 호출한다.
     */
    suspend fun syncPushToken()
}
