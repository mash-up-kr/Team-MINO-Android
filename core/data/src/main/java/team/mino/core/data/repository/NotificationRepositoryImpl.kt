package team.mino.core.data.repository

import team.mino.core.data.datasource.NotificationRemoteDataSource
import team.mino.core.data.repository.mapper.toDomain
import team.mino.core.domain.model.NotificationPage
import team.mino.core.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * [NotificationRepository]의 구현 — 계약은
 * `docs/specs/notifications/contracts/notification-repository.md` §1이 소유한다.
 *
 * 페이지네이션 상태를 갖지 않는다. 다음에 어느 묶음을 부를지 정하는 것은 호출자이며, 이 클래스는 요청받은
 * 묶음을 그대로 옮긴다 — [PlaceCommentRepositoryImpl]과 같은 규약이다. 순서도 건드리지 않는다. 서버가 준
 * 최신순 그대로다(FR-001).
 *
 * **목록을 기기에 저장하지 않는다.** 오프라인 캐시를 두지 않기로 한 결정이라 로컬 DataSource가 없고,
 * 원격 출처 하나만 주입받는다(`docs/specs/notifications/plan.md` §범위 경계).
 *
 * 예외를 잡지 않는다 — 매핑은 `HttpClient`의 `convertDomainException`이 전역 수행하고 실패는
 * `MinoDomainException`으로 전파된다(`core/data/README.md` §6). 빈 묶음으로 수렴시키지 않는 것은 spec
 * UX-002가 "알림이 없다"와 "못 불러왔다"를 구분하라고 요구하기 때문이다.
 */
internal class NotificationRepositoryImpl @Inject constructor(
    private val notificationRemoteDataSource: NotificationRemoteDataSource,
) : NotificationRepository {
    /**
     * `pageSize`를 싣지 않아 서버 기본값을 쓴다. 한 묶음의 크기를 도메인이 정하면 서버 기본값이 바뀔 때 두
     * 곳이 어긋난다(`docs/specs/notifications/contracts/notification-api.md` §1).
     *
     * **그릴 수 없는 항목을 여기서 다시 판정하지 않는다.** 알 수 없는 유형과 대상 식별자가 빈 항목을
     * 걸러내는 것은 Mapper의 몫이고(`NotificationMapper`), `hasNext`는 그 취사선택과 무관하게 서버가 준
     * 값을 그대로 싣는다.
     */
    override suspend fun getNotifications(page: Int): NotificationPage =
        notificationRemoteDataSource.getNotifications(page).toDomain()
}
