package team.mino.core.domain.model

/**
 * 한 방·한 정렬이 만들어 내는 카드 묶음.
 *
 * [cards]는 서버가 최대 10장으로 잘라 준 것을 그대로 담는다. 클라이언트가 다시 자르지도, 모자란 만큼 채우지도
 * 않는다 — `docs/specs/home-deck-exploration/data-model.md` §1.3.
 *
 * [cards]가 비어 있는 덱은 **소진된 덱과 같게 다룬다**. 그래서 "빈 덱"을 따로 표현하는 타입을 두지 않는다.
 */
data class Deck(
    val roomId: String,
    val sort: DeckSort,
    val cards: List<PlaceCard>,
)

/**
 * 덱 하나를 가리키는 식별자.
 *
 * 덱은 방과 정렬의 조합으로만 정해지므로 이 둘이 곧 키다. 소진 집합([DeckContext.exhausted])과 예고 이력이
 * 이 값을 키로 쓴다 — 그 용도 때문에 [Deck]과 달리 내용(카드)을 갖지 않는다.
 */
data class DeckKey(
    val roomId: String,
    val sort: DeckSort,
)
