package team.mino.feature.home.fake

import team.mino.core.domain.model.PlaceDetail
import team.mino.core.domain.repository.PlaceRepository
import team.mino.core.errorhandling.MinoDomainException

/**
 * `:feature:home` 테스트용 [PlaceRepository] 테스트 더블.
 *
 * 홈이 쓰는 것은 [recordAccess]·[duplicatePin] 둘뿐이다(`contracts/home-ui.md` §4.2.1, R-019) — 상세 조회는
 * [SCR-006]의 몫이라 [getPlaceDetail]은 부르면 실패한다.
 *
 * **호출을 세는 것이 이 더블의 핵심이다.** 둘 다 상태만 봐서는 판정할 수 없다.
 * - [recordedAccessPinIds] — 카드 탭이 「경과일 초기화 확인」을 알렸는지(FR-007·023, TS-034), 넘김은 알리지
 *   않는지(TS-035). 실패 스위치를 두지 않는다 — [recordAccess]는 계약상 결과를 기다리지 않고 실패해도 화면에
 *   닿지 않는다(R-012).
 * - [duplicatePinCalls] — `다른 방 저장`이 고른 방 전부를 그대로 실어 나갔는지(FR-005, TS-011a·011b).
 *
 * [duplicatePinFailure]만 실패 스위치를 갖는다. 그 실패가 `DomainErrorEmitter`로 나가는지가 홈의 판정
 * 대상이다(`docs/conventions/error_handling.md` §5 2행). 기본값은 **「실패하지 않음」**(`null`)이다.
 */
internal class FakePlaceRepository : PlaceRepository {
    /** 값이 있으면 [duplicatePin]이 이 예외를 던진다. */
    var duplicatePinFailure: MinoDomainException? = null

    private val recordedAccess = mutableListOf<String>()
    private val duplicatePinRequests = mutableListOf<DuplicatePinRequest>()

    /** [recordAccess]로 알린 pinId들. 호출 순서대로 쌓인다. */
    val recordedAccessPinIds: List<String> get() = recordedAccess

    /** [duplicatePin]으로 나간 요청들. 실패한 호출은 남지 않는다. */
    val duplicatePinCalls: List<DuplicatePinRequest> get() = duplicatePinRequests

    override suspend fun getPlaceDetail(pinId: String): PlaceDetail =
        error("FakePlaceRepository는 getPlaceDetail을 지원하지 않는다.")

    override suspend fun recordAccess(pinId: String) {
        recordedAccess += pinId
    }

    override suspend fun duplicatePin(
        pinId: String,
        roomIds: List<String>,
    ) {
        duplicatePinFailure?.let { throw it }
        duplicatePinRequests += DuplicatePinRequest(pinId = pinId, roomIds = roomIds)
    }

    /** [duplicatePin] 호출 하나. [roomIds]는 넘어온 순서·전체를 그대로 담는다(TS-011a). */
    data class DuplicatePinRequest(
        val pinId: String,
        val roomIds: List<String>,
    )
}
