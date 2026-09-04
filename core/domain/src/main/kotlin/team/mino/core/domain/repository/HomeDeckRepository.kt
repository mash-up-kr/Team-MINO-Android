package team.mino.core.domain.repository

import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.Deck
import team.mino.core.domain.model.DeckSort
import team.mino.core.domain.model.RoomSummary

/**
 * 홈 카드 덱의 조회 계약.
 *
 * `Flow`를 흘리지 않고 실패를 `Result`로 감싸지 않는다 — 기존 [RoomRepository]와 같은 규약이다.
 * 실패는 `MinoDomainException`으로 던지고 취소는 그대로 전파한다.
 *
 * `docs/specs/home-deck-exploration/contracts/home-ui.md` §4.2.
 *
 * `savePinToRoom`은 이 인터페이스에 없다 — [PlaceRepository]가 이미 소유한 `duplicatePin`과 같은
 * 동작이라 홈이 별도 함수를 두지 않는다(R-019, §4.2.1).
 *
 * 「경과일 초기화 확인」을 알리는 함수도 없다. 그 기록은 [SCR-006] 장소 상세가 소유하며
 * (`docs/specs/place-detail/spec.md` FR-026), 홈까지 부르면 카드 한 번 탭에 두 건이 쌓인다 —
 * 홈은 `PlaceRepository.recordAccess`도 부르지 않는다(spec 4.0.0 FR-023).
 */
interface HomeDeckRepository {
    /**
     * 순회 대상 방 목록. [RoomSummary.placeCount]가 그 방에 저장된 장소의 수를 담으며, 다음 방을 고르는 판정이
     * 이 값을 쓴다(FR-012·013).
     *
     * **순서를 이 함수가 확정한다** — 개인방 먼저, 그다음 방을 만든 지 오래된 순(FR-012, R-014).
     * 여러 화면이 공유하는 방 목록 조회의 응답 순서에 기대지 않으므로, 구현이 받은 응답을 이 순서로
     * 재배치해 돌려준다. 호출부와 `ResolveNextDeckUseCase`는 받은 순서를 그대로 훑는다.
     */
    suspend fun getRoomSummaries(): List<RoomSummary>

    /**
     * [roomId]·[sort]의 덱. 서버가 최대 10장으로 잘라 주므로 받은 것을 그대로 담는다.
     *
     * [location]은 [sort]가 [DeckSort.NEAREST]일 때만 쓰인다. `NEAREST`인데 `null`이면 요청을 보내지 않고
     * **빈 덱**을 돌려준다 — 위치 권한 거부를 「소진」으로 흡수한다(EC-009,
     * `docs/specs/home-deck-exploration/research.md` R-013). 빈 덱과 소진된 덱은 같게 다뤄지므로
     * 이 경로가 전환 규칙에 예외 분기를 만들지 않는다.
     */
    suspend fun getDeck(
        roomId: String,
        sort: DeckSort,
        location: GeoPoint? = null,
    ): Deck
}
