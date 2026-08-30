package team.mino.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import team.mino.core.domain.model.DeckContext
import team.mino.core.domain.model.DeckKey
import team.mino.core.domain.model.DeckSort
import team.mino.core.domain.model.NextDeck
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.model.RoomType

/**
 * 덱 전환 규칙을 본다(`contracts/home-ui.md` §4.1 · FR-011·012·013·014 · TS-015~019·021·024 · EC-009).
 *
 * 판정 대상은 **[DeckContext] → [NextDeck]** 하나뿐이다. 이 UseCase는 순수 함수라 저장소도 시간도 끼지 않는다.
 *
 * 「후보 0장인 덱」과 「위치 권한이 거부된 `가까운순`」은 호출자가 [DeckContext.exhausted]에 넣어 넘긴다
 * (FR-011 「후보가 0건인 덱은 소진된 것으로 본다」 · EC-009). 그래서 이 테스트는 0장인 덱을 카드 수로 세우지
 * 않고 소진 집합으로 세운다 — 두 사유를 이 계층이 구별하지 않는 것 자체가 계약이다.
 *
 * 방 순회 조건은 `RoomSummary.placeCount > 0`이다(FR-013). data-model.md §1.1이 `pinCount`라 적은 것은
 * 실제 타입과 다르며, 판정 근거는 타입 쪽이다.
 */
class ResolveNextDeckUseCaseTest {
    private val resolveNextDeck = ResolveNextDeckUseCase()

    // ── 1단계: 같은 방의 미소진 덱 (FR-011) ────────────────────────────────

    /** TS-016 — 세 덱 모두 카드가 있는 방에서 `꾹 Pick`을 소진하면 `가까운순`이 아니라 `최신순`이다. */
    @Test
    fun `같은 방에 미소진 덱이 남으면 선언 순서상 최우선 덱을 고른다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2")),
                currentRoomId = "room-1",
                currentSort = DeckSort.GGUK_PICK,
                exhaustedSorts = setOf(DeckSort.GGUK_PICK),
            )

        assertEquals(NextDeck.SameRoom(DeckSort.LATEST), resolveNextDeck(context))
    }

    /** TS-015 — `가까운순`을 보다 소진했고 `최신순`은 0장이면, 다음 방이 아니라 `꾹 Pick`으로 되돌아간다. */
    @Test
    fun `현재 정렬보다 앞선 덱이 남아 있으면 그 덱으로 되돌아간다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2")),
                currentRoomId = "room-1",
                currentSort = DeckSort.NEAREST,
                exhaustedSorts = setOf(DeckSort.LATEST, DeckSort.NEAREST),
            )

        assertEquals(NextDeck.SameRoom(DeckSort.GGUK_PICK), resolveNextDeck(context))
    }

    /** TS-017 — `최신순`이 0장이라 소진으로 들어온 경우, 그 덱을 건너뛰고 `가까운순`을 고른다. */
    @Test
    fun `후보 0장이라 소진으로 넘어온 덱은 건너뛴다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2")),
                currentRoomId = "room-1",
                currentSort = DeckSort.GGUK_PICK,
                exhaustedSorts = setOf(DeckSort.GGUK_PICK, DeckSort.LATEST),
            )

        assertEquals(NextDeck.SameRoom(DeckSort.NEAREST), resolveNextDeck(context))
    }

    /** TS-021 — 칩으로 건너뛴 `가까운순`을 소진해도 `꾹 Pick`이 남아 있으므로 방을 넘기지 않는다. */
    @Test
    fun `칩으로 건너뛴 덱을 소진해도 남은 덱이 있으면 방을 넘기지 않는다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2")),
                currentRoomId = "room-1",
                currentSort = DeckSort.NEAREST,
                exhaustedSorts = setOf(DeckSort.NEAREST),
            )

        assertEquals(NextDeck.SameRoom(DeckSort.GGUK_PICK), resolveNextDeck(context))
    }

    /** EC-009 — 위치 권한 거부로 `가까운순`이 소진 처리돼도, 같은 방에 남은 덱이 있으면 그리로 간다. */
    @Test
    fun `위치 권한 거부로 가까운순이 소진되어도 같은 방의 남은 덱으로 간다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2")),
                currentRoomId = "room-1",
                currentSort = DeckSort.NEAREST,
                exhaustedSorts = setOf(DeckSort.GGUK_PICK, DeckSort.NEAREST),
            )

        assertEquals(NextDeck.SameRoom(DeckSort.LATEST), resolveNextDeck(context))
    }

    // ── 2단계: 다음 방 (FR-012·013) ───────────────────────────────────────

    /** TS-018 · EC-009 — 세 덱이 모두 소진돼야만 방을 넘긴다. */
    @Test
    fun `세 덱을 모두 소진하면 다음 방으로 넘어간다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2"), room("room-3")),
                currentRoomId = "room-1",
                currentSort = DeckSort.NEAREST,
                exhaustedSorts = ALL_SORTS,
            )

        assertEquals(NextDeck.NextRoom("room-2"), resolveNextDeck(context))
    }

    /** TS-019 — 저장 장소가 0개인 방은 연달아 있어도 전부 건너뛴다. */
    @Test
    fun `저장 장소가 0개인 방은 건너뛰고 그 뒤의 방을 고른다`() {
        val context =
            deckContext(
                rooms =
                    listOf(
                        room("room-1"),
                        room("room-empty-1", placeCount = 0),
                        room("room-empty-2", placeCount = 0),
                        room("room-4"),
                    ),
                currentRoomId = "room-1",
                currentSort = DeckSort.NEAREST,
                exhaustedSorts = ALL_SORTS,
            )

        assertEquals(NextDeck.NextRoom("room-4"), resolveNextDeck(context))
    }

    // ── 3단계: 완료 (FR-014) ──────────────────────────────────────────────

    /** TS-024 — 현재 방이 목록의 마지막이고 세 덱이 모두 소진되면 완료다. */
    @Test
    fun `마지막 방의 세 덱을 모두 소진하면 완료다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2")),
                currentRoomId = "room-2",
                currentSort = DeckSort.NEAREST,
                exhaustedSorts = ALL_SORTS,
            )

        assertEquals(NextDeck.AllExhausted, resolveNextDeck(context))
    }

    /**
     * FR-012 · EC-010 — 순회는 현재 방 **다음** 자리부터다. 뒤에 남은 방이 전부 0개면, 앞의 방에 장소가
     * 있더라도 되돌아가지 않고 완료로 간다.
     */
    @Test
    fun `앞자리 방에 장소가 남아 있어도 되돌아가지 않는다`() {
        val context =
            deckContext(
                rooms =
                    listOf(
                        room("room-1"),
                        room("room-2"),
                        room("room-empty", placeCount = 0),
                    ),
                currentRoomId = "room-2",
                currentSort = DeckSort.NEAREST,
                exhaustedSorts = ALL_SORTS,
            )

        assertEquals(NextDeck.AllExhausted, resolveNextDeck(context))
    }

    private fun deckContext(
        rooms: List<RoomSummary>,
        currentRoomId: String,
        currentSort: DeckSort,
        exhaustedSorts: Set<DeckSort>,
    ): DeckContext =
        DeckContext(
            rooms = rooms,
            currentRoomId = currentRoomId,
            currentSort = currentSort,
            exhausted = exhaustedSorts.map { DeckKey(roomId = currentRoomId, sort = it) }.toSet(),
        )

    private fun room(
        id: String,
        placeCount: Int = 3,
    ): RoomSummary =
        RoomSummary(
            id = id,
            name = id,
            description = "",
            type = RoomType.GROUP,
            color = RoomColor.GRAY,
            placeCount = placeCount,
            thumbnailImageUrls = emptyList(),
        )

    private companion object {
        val ALL_SORTS = DeckSort.entries.toSet()
    }
}
