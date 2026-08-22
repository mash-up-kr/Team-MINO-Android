package team.mino.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    /**
     * 알림 표시 억제 여부(로컬 전용 플래그, 기본값 false)를 관찰한다. 서버 발송 여부는 막지 않는다.
     */
    fun observeNotificationDeliveryEnabled(): Flow<Boolean>

    /**
     * 알림 표시 억제 여부를 로컬에 저장한다.
     */
    suspend fun setNotificationDeliveryEnabled(enabled: Boolean)
}
