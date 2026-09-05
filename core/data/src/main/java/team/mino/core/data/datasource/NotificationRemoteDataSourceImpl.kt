package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.NotificationPageResponse
import team.mino.core.data.network.service.NotificationApiService
import javax.inject.Inject

/**
 * [NotificationRemoteDataSource]의 유일한 구현. [NotificationApiService](Ktor) 호출을 그대로 위임한다 —
 * 변환·비즈니스 로직은 두지 않는다(`core/data/README.md` §5).
 */
internal class NotificationRemoteDataSourceImpl @Inject constructor(
    private val service: NotificationApiService,
) : NotificationRemoteDataSource {
    override suspend fun getNotifications(page: Int): NotificationPageResponse = service.getNotifications(page)
}
