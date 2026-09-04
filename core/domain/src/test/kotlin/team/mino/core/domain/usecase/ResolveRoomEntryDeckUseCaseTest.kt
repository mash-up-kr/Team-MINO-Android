package team.mino.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.domain.model.DeckContext
import team.mino.core.domain.model.DeckKey
import team.mino.core.domain.model.DeckSort
import team.mino.core.domain.model.NextDeck
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.model.RoomType

/**
 * 수동 방 변경 규칙을 본다(`contracts/home-ui.md` §4.1 「수동 방 변경」 · FR-024, SC-008 ·
 * TS-028·028b·028c · EC-020·022).
 *
 * `ResolveNextDeckUseCase`(자동 전환)와 달리 탐색 범위를 **`roomId` 인자 하나로 한정한다.**
 * `roomId`의 세 정렬 중 `exhausted`에 없는 것이 있으면 그중 [DeckSort] 선언 순서상 최우선으로
 * `SameRoom`, 없으면 `AllExhausted`다. **`NextRoom`을 절대 내지 않는다** — 그것이 "다른 방으로
 * 넘기지 않는다"(FR-024·SC-008)의 코드 표현이다.
 *
 * 저장 장소가 0개인 방은 세 정렬이 모두 후보 0건이라 호출자가 [DeckContext.exhausted]에 그 방의
 * 세 [DeckKey]를 전부 넣어 넘긴다(FR-011). 그래서 이 UseCase는 `RoomSummary.placeCount`를 직접
 * 검사하는 별도 분기를 두지 않는다 — 1번 판정을 통과하지 못해 자연히 `AllExhausted`로 떨어진다
 * (EC-020·022).
 */
class ResolveRoomEntryDeckUseCaseTest {
    private val resolveRoomEntryDeck = ResolveRoomEntryDeckUseCase()

    /** TS-028 — 고른 방의 `꾹 Pick`이 소진되지 않았으면 그것을 고른다. */
    @Test
    fun `고른 방의 꾹 Pick이 남아 있으면 그것을 고른다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2")),
                currentRoomId = "room-1",
                currentSort = DeckSort.LATEST,
                exhausted = emptySet(),
            )

        assertEquals(NextDeck.SameRoom(DeckSort.GGUK_PICK), resolveRoomEntryDeck(context, "room-2"))
    }

    /** TS-028b — 고른 방의 `꾹 Pick`이 소진이면 그 방에 남은 덱 중 선언 순서상 최고 순위(`최신순`)를 고른다. */
    @Test
    fun `고른 방의 꾹 Pick이 소진이면 남은 덱 중 최고 순위를 고른다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2")),
                currentRoomId = "room-1",
                currentSort = DeckSort.LATEST,
                exhausted = setOf(DeckKey("room-2", DeckSort.GGUK_PICK)),
            )

        assertEquals(NextDeck.SameRoom(DeckSort.LATEST), resolveRoomEntryDeck(context, "room-2"))
    }

    /** TS-028c, EC-020 — 고른 방의 세 칸이 모두 소진이면 `AllExhausted`다. */
    @Test
    fun `고른 방의 세 칸이 모두 소진이면 AllExhausted다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2")),
                currentRoomId = "room-1",
                currentSort = DeckSort.LATEST,
                exhausted = allKeysOf("room-2"),
            )

        assertEquals(NextDeck.AllExhausted, resolveRoomEntryDeck(context, "room-2"))
    }

    /**
     * FR-024, SC-008 — 고른 방(room-2)이 소진이어도 **다른 방(room-1)이 미소진 덱을 갖고 있다는
     * 이유로 그리로 넘기지 않는다.** 반환은 `AllExhausted`여야 하며 `NextDeck.NextRoom`이 나오면
     * 안 된다 — 어떤 입력에서도 `NextRoom`을 내지 않는 것이 이 UseCase의 계약이다.
     */
    @Test
    fun `어떤 입력에서도 NextRoom을 내지 않는다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2")),
                currentRoomId = "room-1",
                currentSort = DeckSort.LATEST,
                exhausted = allKeysOf("room-2"),
            )

        val result = resolveRoomEntryDeck(context, "room-2")

        assertEquals(NextDeck.AllExhausted, result)
        assertTrue("NextRoom을 내면 안 된다: $result", result !is NextDeck.NextRoom)
    }

    /**
     * EC-020, EC-022 — `placeCount == 0`인 방(room-2)을 골라도 다른 방(room-1)으로 넘기지 않는다.
     * 0개 방의 세 정렬은 후보 0건이라 호출자가 이미 [DeckContext.exhausted]에 전부 채워 넘긴 상태다.
     */
    @Test
    fun `placeCount 0인 방을 골라도 다른 방으로 넘기지 않는다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2", placeCount = 0)),
                currentRoomId = "room-1",
                currentSort = DeckSort.LATEST,
                exhausted = allKeysOf("room-2"),
            )

        assertEquals(NextDeck.AllExhausted, resolveRoomEntryDeck(context, "room-2"))
    }

    private fun deckContext(
        rooms: List<RoomSummary>,
        currentRoomId: String,
        currentSort: DeckSort,
        exhausted: Set<DeckKey>,
    ): DeckContext =
        DeckContext(
            rooms = rooms,
            currentRoomId = currentRoomId,
            currentSort = currentSort,
            exhausted = exhausted,
        )

    private fun room(
        id: String,
        placeCount: Int = 3,
        type: RoomType = RoomType.GROUP,
    ): RoomSummary =
        RoomSummary(
            id = id,
            name = id,
            description = "",
            type = type,
            color = RoomColor.GRAY,
            placeCount = placeCount,
            thumbnailImageUrls = emptyList(),
        )

    private fun allKeysOf(roomId: String): Set<DeckKey> = DeckSort.entries.map { DeckKey(roomId, it) }.toSet()
}
