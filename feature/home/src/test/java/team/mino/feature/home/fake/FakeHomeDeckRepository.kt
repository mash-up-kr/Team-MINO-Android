package team.mino.feature.home.fake

import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.Deck
import team.mino.core.domain.model.DeckKey
import team.mino.core.domain.model.DeckSort
import team.mino.core.domain.model.PlaceCard
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.repository.HomeDeckRepository
import team.mino.core.errorhandling.MinoDomainException

/**
 * `:feature:home` 테스트용 [HomeDeckRepository] 테스트 더블. 홈의 ViewModel 테스트 전부가 이 하나를 공유한다.
 *
 * **덱은 방·정렬 조합([DeckKey])별로 따로 세운다** — 전환 규칙이 그 조합을 키로 판정하기 때문이다
 * (`contracts/home-ui.md` §4.1). [setDeck]으로 세우지 않은 조합은 **빈 덱**이며, 빈 덱은 소진된 덱과 같게 다뤄진다
 * (`data-model.md` §1.3). 그래서 "볼 것이 없는 방"을 따로 표현하는 스위치를 두지 않는다.
 *
 * **호출을 세는 것이 이 더블의 핵심이다.** 홈의 계약 중 셋은 상태만 봐서는 판정할 수 없다.
 * - [openedPinIds] — 넘김은 서버를 부르지 않고 상세 진입만 부른다(FR-023, TS-034·035). 넘김 뒤 이 목록이
 *   **비어 있는 것**이 TS-035의 판정 근거이므로, 호출 여부와 인자를 둘 다 남긴다.
 * - [deckRequests] — 현재 방을 다시 골라도 덱을 다시 구성하지 않는다(EC-014). 상태가 그대로인 것만으로는
 *   "다시 받아 같은 값이 된 것"과 구별되지 않아 **요청 횟수**가 있어야 한다. 정렬·좌표도 함께 남는다.
 * - [savedPins] — `다른 방 저장`이 실제로 나갔는지(FR-005).
 *
 * **실패 스위치는 셋이고, 하나는 없다.** 넷 모두 계약이 실패를 [MinoDomainException]으로 던지기로 돼 있지만,
 * 그 실패가 화면에 닿는 방식이 서로 다르다.
 * - [savePinToRoomFailure] — 사용자 액션의 일회성 실패라 상태도 SideEffect도 아닌 `DomainErrorEmitter`로
 *   나간다(`docs/conventions/error_handling.md` §5 2행, FR-005). 방출이 이 실패에서만 생긴다.
 * - [getDeckFailure]·[getRoomSummariesFailure] — 둘은 **화면의 주 데이터** 로드라 실패가 스낵바가 아니라
 *   상태에 담겨야 한다(`docs/conventions/error_handling.md` §5 1행). 담기지 않으면 첫 로드가 실패한 화면이
 *   로딩에서 걷히지 않는다 — 그 고착을 잡는 그물이 이 둘이다.
 * - `recordPlaceOpened`만 스위치가 없다. 결과를 기다리지 않는 호출이라(R-012) 실패가 화면에 닿지 않는다.
 *
 * 셋 다 기본값은 **「실패하지 않음」**(`null`)이다 — 세우지 않은 테스트는 실패를 만나지 않는다.
 */
internal class FakeHomeDeckRepository : HomeDeckRepository {
    /** [getRoomSummaries]가 돌려줄 순회 대상 방 목록. 순서 그대로 쓰인다. */
    var rooms: List<RoomSummary> = emptyList()

    /** [getRoomSummaries]가 호출된 횟수. */
    var getRoomSummariesCallCount: Int = 0
        private set

    /** 값이 있으면 [savePinToRoom]이 이 예외를 던진다. */
    var savePinToRoomFailure: MinoDomainException? = null

    /** 값이 있으면 [getDeck]이 이 예외를 던진다. 요청은 [deckRequests]에 남는다 — 나갔다가 실패한 것이다. */
    var getDeckFailure: MinoDomainException? = null

    /** 값이 있으면 [getRoomSummaries]가 이 예외를 던진다. 호출 횟수는 [getRoomSummariesCallCount]에 남는다. */
    var getRoomSummariesFailure: MinoDomainException? = null

    private val decks = mutableMapOf<DeckKey, List<PlaceCard>>()
    private val requestedDecks = mutableListOf<DeckRequest>()
    private val openedPins = mutableListOf<String>()
    private val savedPinRequests = mutableListOf<SaveRequest>()

    /** [getDeck]에 들어온 요청들. 호출 순서대로 쌓인다. */
    val deckRequests: List<DeckRequest> get() = requestedDecks

    /** [recordPlaceOpened]로 알린 pinId들. 호출 순서대로 쌓인다. */
    val openedPinIds: List<String> get() = openedPins

    /** [savePinToRoom]으로 나간 요청들. 실패한 호출은 남지 않는다. */
    val savedPins: List<SaveRequest> get() = savedPinRequests

    /** [roomId]·[sort] 덱을 [cards]로 세운다. 세우지 않은 조합은 빈 덱이다. */
    fun setDeck(
        roomId: String,
        sort: DeckSort,
        cards: List<PlaceCard>,
    ) {
        decks[DeckKey(roomId = roomId, sort = sort)] = cards
    }

    override suspend fun getRoomSummaries(): List<RoomSummary> {
        getRoomSummariesCallCount++
        getRoomSummariesFailure?.let { throw it }
        return rooms
    }

    /**
     * `NEAREST`인데 좌표가 없으면 세워 둔 덱과 무관하게 **빈 덱**이다 — 계약이 그렇다(EC-009, R-013).
     * 더블이 이 규칙을 빼먹으면 권한 거부 경로에서 실서버에는 없는 카드가 나와 테스트가 거짓으로 통과한다.
     */
    override suspend fun getDeck(
        roomId: String,
        sort: DeckSort,
        location: GeoPoint?,
    ): Deck {
        requestedDecks += DeckRequest(roomId = roomId, sort = sort, location = location)
        getDeckFailure?.let { throw it }
        val cards =
            if (sort == DeckSort.NEAREST && location == null) {
                emptyList()
            } else {
                decks[DeckKey(roomId = roomId, sort = sort)].orEmpty()
            }
        return Deck(roomId = roomId, sort = sort, cards = cards)
    }

    override suspend fun recordPlaceOpened(pinId: String) {
        openedPins += pinId
    }

    override suspend fun savePinToRoom(
        pinId: String,
        roomId: String,
    ) {
        savePinToRoomFailure?.let { throw it }
        savedPinRequests += SaveRequest(pinId = pinId, roomId = roomId)
    }

    /** [getDeck] 호출 하나. [location]은 `NEAREST`가 아닌 정렬에서 `null`인 것이 정상이다. */
    data class DeckRequest(
        val roomId: String,
        val sort: DeckSort,
        val location: GeoPoint?,
    )

    /** [savePinToRoom] 호출 하나. */
    data class SaveRequest(
        val pinId: String,
        val roomId: String,
    )
}
