package team.mino.core.domain.model

/**
 * 알림함이 한 번에 받아 오는 알림 한 묶음 (`docs/specs/notifications/data-model.md` §1.4).
 *
 * [items]는 서버가 준 최신순 그대로다 — 정렬을 도메인이 다시 하지 않는다
 * (`docs/specs/notifications/contracts/notification-repository.md` §1).
 * Mapper가 두 갈래로 항목을 버리므로 [items]의 크기는 서버가 실어 보낸 건수보다 작을 수 있다
 * ([team.mino.core.domain.repository.NotificationRepository.getNotifications]에 버림 조건이 있다).
 * **그러므로 끝은 [items] 크기가 아니라 [hasNext]로 판정한다.**
 *
 * **`page`·`pageSize`를 담지 않는다.** 다음에 무엇을 요청할지는 호출자가 이미 알고 있고, 응답이 알려 줄
 * 필요가 있는 것은 [hasNext] 하나다(spec FR-018·EC-018).
 */
data class NotificationPage(
    val items: List<Notification>,
    val hasNext: Boolean,
)
