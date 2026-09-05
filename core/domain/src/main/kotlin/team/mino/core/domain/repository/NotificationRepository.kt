package team.mino.core.domain.repository

import team.mino.core.domain.model.NotificationPage

/**
 * 알림함 목록 조회 계약
 * (`docs/specs/notifications/contracts/notification-repository.md` §1).
 *
 * 1회성 요청이라 `Flow`를 흘리지 않으며, 실패를 `Result`로 감싸지 않고 `MinoDomainException`으로 던진다.
 * 취소는 그대로 전파한다 — [PlaceCommentRepository]와 같은 규약이다.
 *
 * **읽음 처리·삭제·수신 설정 함수를 두지 않는다.** spec §3.2가 셋 모두 범위 밖으로 뺐고, FR-016이 읽음이라는
 * 상태 자체를 없앴다.
 *
 * 페이지네이션 상태를 갖지 않는다 — 다음에 어느 묶음을 요청할지는 호출자가 정한다.
 */
interface NotificationRepository {
    /**
     * 알림 한 묶음을 가져온다(FR-018). [page] 0이 최신 묶음이다.
     *
     * **`pageSize`를 받지 않는다.** 한 묶음의 크기는 서버 기본값을 따르는 것이 spec §4의 전제이고, 인자로
     * 열어 두면 호출부가 20을 박게 된다(`docs/specs/notifications/contracts/notification-api.md` §1).
     *
     * **정렬 책임을 갖지 않는다.** 서버가 최신순으로 주고 받은 순서를 그대로 돌려준다(FR-001).
     *
     * **버려지는 항목이 두 갈래다.** 알 수 없는 유형뿐 아니라, 유형은 알아도 `payload`에 그 유형이 요구하는
     * 식별자가 없는 항목(장소 대상인데 `pinId`가 없거나 공동방 참가인데 `roomId`가 없는 경우)도 빠져 있다.
     * 어느 쪽도 행으로 그릴 수 없기 때문이며, 그 판정은 `:core:data`의 Mapper가 집행한다. 그런 항목이
     * 섞였다는 이유로 묶음 전체가 실패하지는 않는다.
     *
     * **그러므로 받은 건수로 끝을 판정하면 안 된다.** [NotificationPage.items]의 크기는 두 갈래 어느 쪽으로도
     * 서버가 실어 보낸 건수보다 작아질 수 있다. 다음 묶음이 있는지는 [NotificationPage.hasNext]만 말한다.
     *
     * 실패는 던진다. 빈 목록으로 수렴시키지 않는다 — spec UX-002가 "알림이 없다"와 "못 불러왔다"를 구분하라고
     * 요구하므로 둘을 같은 값으로 만들면 안 된다.
     */
    suspend fun getNotifications(page: Int): NotificationPage
}
