package team.mino.core.domain.model

/**
 * 다음에 무엇을 보여줄지 판정하기 위해 필요한 전부.
 *
 * 판정은 순수 함수여야 하므로(`contracts/home-ui.md` §4.1) 판정에 쓰이는 값이 전부 여기 모여 있어야 한다.
 * 저장소를 다시 읽지 않는다.
 *
 * [rooms]는 순회 순서 그대로다. 방 전환은 이 목록에서 [currentRoomId] **다음** 자리부터 찾는다.
 * [exhausted]에 없는 덱만 후보가 된다.
 *
 * 판정 규칙 자체는 이 타입이 아니라 `ResolveNextDeckUseCase`가 소유한다 — 여기 적지 않는다.
 */
data class DeckContext(
    val rooms: List<RoomSummary>,
    val currentRoomId: String,
    val currentSort: DeckSort,
    val exhausted: Set<DeckKey>,
)

/**
 * 판정 결과. 세 갈래뿐이고 서로 배타적이다.
 */
sealed interface NextDeck {
    /** 같은 방에 아직 볼 정렬이 남았다. [sort]는 그중 [DeckSort] 선언 순서상 최우선이다. */
    data class SameRoom(val sort: DeckSort) : NextDeck

    /**
     * 현재 방의 세 덱이 모두 소진돼 다음 방으로 넘어간다. 자동 전환은 정렬을 유지하므로(FR-012)
     * [sort]가 그 다음 방에서 보여줄 정렬이다 — 종전처럼 방 전환 시 최우선 정렬로 초기화된다고 가정하지 않는다.
     */
    data class NextRoom(val roomId: String, val sort: DeckSort) : NextDeck

    /** 순회할 방이 더 없다. 볼 것이 없는 상태(`Empty`)와는 다른 화면이다. */
    data object AllExhausted : NextDeck
}
