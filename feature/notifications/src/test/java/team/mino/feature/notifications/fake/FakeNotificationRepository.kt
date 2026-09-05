package team.mino.feature.notifications.fake

import kotlinx.coroutines.CompletableDeferred
import team.mino.core.domain.model.Notification
import team.mino.core.domain.model.NotificationPage
import team.mino.core.domain.repository.NotificationRepository
import team.mino.core.errorhandling.MinoDomainException

/**
 * `:feature:notifications` 테스트용 [NotificationRepository] 테스트 더블.
 *
 * **묶음을 페이지 번호별로 세운다** — 다음에 어느 묶음을 요청할지는 호출자가 정한다는 계약이라
 * (`docs/specs/notifications/contracts/notification-repository.md` §1) 어느 번호로 나갔는지가 판정 대상이다.
 * 세우지 않은 번호는 **빈 묶음이고 `hasNext`가 `false`다** — 서버에 더 줄 것이 없는 상태와 같다.
 *
 * **요청 번호를 순서대로 남긴다**([requestedPages]). 알림함의 계약 중 셋은 상태만 봐서는 판정할 수 없다.
 * - 끝에 도달해도 더 받을 것이 없으면 요청이 나가지 않는다(EC-018) — 상태가 그대로인 것만으로는
 *   "다시 받아 같은 값이 된 것"과 구별되지 않는다.
 * - 재시도는 **같은 번호**를 다시 부른다 — 실패한 묶음을 건너뛰면 알림이 통째로 사라진다.
 * - 두 번째 `Load`는 요청을 만들지 않는다(FR-015·TS-011·TS-043).
 *
 * **실패는 번호별로 세운다**([failPage]). 첫 페이지 실패(UX-002·EC-001)와 추가 페이지 실패(UX-012·EC-016)가
 * 서로 다른 통로로 나가야 하므로, 둘을 한 스위치로 묶으면 그 갈림을 만들 수 없다. 계약대로 **던진다** —
 * 빈 목록으로 수렴시키지 않는다.
 */
internal class FakeNotificationRepository : NotificationRepository {
    private val pages = mutableMapOf<Int, NotificationPage>()
    private val failures = mutableMapOf<Int, MinoDomainException>()
    private val requests = mutableListOf<Int>()

    /** [getNotifications]로 들어온 페이지 번호들. 실패한 요청도 남는다 — 나갔다가 실패한 것이다. */
    val requestedPages: List<Int> get() = requests

    /**
     * 값이 있으면 응답을 내주기 전에 여기서 멈춘다.
     *
     * 조회가 도는 **동안**의 상태를 관측하는 유일한 수단이다 — 응답이 즉시 돌아오면 `Loading`이 존재한
     * 구간 자체가 없어 UX-001을 판정할 수 없다.
     */
    var gate: CompletableDeferred<Unit>? = null

    /** [page] 묶음을 세운다. [items]는 서버가 준 순서 그대로 돌려준다(FR-001). */
    fun setPage(
        page: Int,
        items: List<Notification>,
        hasNext: Boolean,
    ) {
        pages[page] = NotificationPage(items = items, hasNext = hasNext)
    }

    /** [page] 요청이 [error]를 던지게 한다. */
    fun failPage(
        page: Int,
        error: MinoDomainException,
    ) {
        failures[page] = error
    }

    /** [page]의 실패를 걷는다. 재시도가 성공하는 경로를 만들 때 쓴다. */
    fun succeedPage(page: Int) {
        failures -= page
    }

    override suspend fun getNotifications(page: Int): NotificationPage {
        requests += page
        gate?.await()
        failures[page]?.let { throw it }
        return pages[page] ?: NotificationPage(items = emptyList(), hasNext = false)
    }
}
