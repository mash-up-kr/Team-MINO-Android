package team.mino.core.domain.repository

import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.Deck
import team.mino.core.domain.model.DeckSort
import team.mino.core.domain.model.RoomSummary

/**
 * 홈 카드 덱의 조회·기록 계약.
 *
 * `Flow`를 흘리지 않고 실패를 `Result`로 감싸지 않는다 — 기존 [RoomRepository]와 같은 규약이다.
 * 실패는 `MinoDomainException`으로 던지고 취소는 그대로 전파한다.
 *
 * `docs/specs/home-deck-exploration/contracts/home-ui.md` §4.2.
 */
interface HomeDeckRepository {
    /**
     * 순회 대상 방 목록. [RoomSummary.placeCount]가 그 방에 저장된 장소의 수를 담으며, 다음 방을 고르는 판정이
     * 이 값을 쓴다(FR-012·013).
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

    /**
     * 「경과일 초기화 확인」을 알린다(FR-007·023). 카드를 눌러 상세로 이동할 때 부른다.
     *
     * 호출자는 결과를 기다리지 않으며 실패해도 화면 전환을 막지 않는다 — 초기화는 다음 덱 요청부터 반영되면 충분하다
     * (`docs/specs/home-deck-exploration/research.md` R-012).
     *
     * 「카드 열람 확인」(넘김)은 이 함수가 다루지 않는다. 넘김은 서버와 무관한 클라이언트 전용 상태다.
     */
    suspend fun recordPlaceOpened(pinId: String)

    /** FR-005. 실패는 `MinoDomainException`으로 던진다. */
    suspend fun savePinToRoom(
        pinId: String,
        roomId: String,
    )
}
