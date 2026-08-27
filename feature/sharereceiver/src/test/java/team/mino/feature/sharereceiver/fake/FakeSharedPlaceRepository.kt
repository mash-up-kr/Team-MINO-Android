package team.mino.feature.sharereceiver.fake

import team.mino.core.domain.model.SharedPlaceSaveRequest
import team.mino.core.domain.repository.SharedPlaceRepository

/**
 * `:feature:sharereceiver` 테스트용 [SharedPlaceRepository] 테스트 더블.
 *
 * 예약을 받아 [scheduled]에 쌓아 두는 것이 전부다. 실패 스위치를 두지 않는 것은 계약이 그렇기 때문이다 —
 * `scheduleSave`는 반환값도 예외도 없고, 저장의 성패는 호출자가 떠난 뒤에 갈린다(`data-model.md` §2.2).
 *
 * **`suspend`가 아니다.** 예약이 코루틴에 묶이면 토스트 뒤 즉시 물러나는 경로(FR-011·UX-006)가 성립하지 않으므로,
 * 이 더블도 관문을 두지 않고 즉시 반환한다.
 *
 * 요청을 몇 개의 작업으로 내보낼지는 이 계약이 아니라 구현의 몫이므로(`research.md` R-021) 여기서는
 * **요청 한 건에 방 id가 몇 개 실려 왔는지**만 남긴다.
 */
internal class FakeSharedPlaceRepository : SharedPlaceRepository {
    private val requests = mutableListOf<SharedPlaceSaveRequest>()

    /** 예약된 요청들. 순서대로 쌓인다. */
    val scheduled: List<SharedPlaceSaveRequest> get() = requests

    override fun scheduleSave(request: SharedPlaceSaveRequest) {
        requests += request
    }
}
