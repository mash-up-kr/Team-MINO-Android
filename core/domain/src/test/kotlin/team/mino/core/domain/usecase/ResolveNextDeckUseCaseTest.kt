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
 * 자동 전환 규칙을 본다(`contracts/home-ui.md` §4.1 「자동 전환」 · FR-011·012·025 ·
 * TS-015·016·017·019·019a·021·024 · EC-009).
 *
 * plan 3.0.0에서 탐색 축이 뒤집혔다 — 「한 방의 세 덱을 다 보고 다음 방」이 아니라
 * 「한 정렬로 모든 방을 다 보고 다음 정렬, 그리고 다음 정렬은 첫 방부터 다시」다. 그래서 판정은
 * **`DeckSort` 선언 순서 → 그 안에서 `context.rooms` 순서**로 격자 전체를 훑어 `exhausted`에 없고
 * `placeCount > 0`인 **첫 칸**을 고른다. `currentRoomId`는 탐색의 시작점이 아니라 결과 판정
 * (`SameRoom` vs `NextRoom`)에만 쓰인다 — 고른 칸의 방이 `currentRoomId`와 같으면 `SameRoom(sort)`,
 * 다르면 `NextRoom(roomId, sort)`이다. `currentSort`는 이 판정에 관여하지 않는다(TS-021이 그 증거다 —
 * 칩으로 건너뛴 나중 정렬에 있어도 앞선 정렬이 안 소진됐으면 그리로 돌아간다).
 *
 * 「후보 0장인 덱」과 「위치 권한이 거부된 `가까운순`」은 호출자가 [DeckContext.exhausted]에 넣어
 * 넘긴다(FR-011, EC-009) — 이 계층은 두 사유를 구별하지 않는다.
 *
 * 방 순회 조건은 `RoomSummary.placeCount > 0`이다. quickstart.md §3.1이 `pinCount`라 적은 것은
 * 실제 타입과 다른 낡은 표기이며, 판정 근거는 타입([RoomSummary]) 쪽이다.
 */
class ResolveNextDeckUseCaseTest {
    private val resolveNextDeck = ResolveNextDeckUseCase()

    /** TS-015 — 현재 방의 정렬이 소진되면, 그 정렬을 유지한 채 다음 방으로 넘어간다. */
    @Test
    fun `덱을 소진하면 정렬을 유지한 채 다음 방으로 간다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2")),
                currentRoomId = "room-1",
                currentSort = DeckSort.GGUK_PICK,
                exhausted = setOf(DeckKey("room-1", DeckSort.GGUK_PICK)),
            )

        assertEquals(NextDeck.NextRoom("room-2", DeckSort.GGUK_PICK), resolveNextDeck(context))
    }

    /**
     * TS-016 — `꾹 Pick`을 모든 방에서 소진해야 `최신순`으로 넘어가고, 그 `최신순`은
     * 현재 방(room-2)이 아니라 **첫 방(room-1)**부터 다시 훑는다. room-2의 `최신순`도 미소진이지만
     * room-1이 목록에서 먼저이므로 그쪽이 답이어야 한다.
     */
    @Test
    fun `지금 정렬로 모든 방을 확인해야 다음 정렬로 넘어가고 첫 방부터 다시 훑는다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2")),
                currentRoomId = "room-2",
                currentSort = DeckSort.GGUK_PICK,
                exhausted =
                    setOf(
                        DeckKey("room-1", DeckSort.GGUK_PICK),
                        DeckKey("room-2", DeckSort.GGUK_PICK),
                    ),
            )

        assertEquals(NextDeck.NextRoom("room-1", DeckSort.LATEST), resolveNextDeck(context))
    }

    /** TS-017 — 후보 0건이라 소진으로 넘어온 칸(`꾹 Pick`·`최신순`)은 건너뛰고 `가까운순`을 고른다. */
    @Test
    fun `후보 0건 칸은 건너뛴다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1")),
                currentRoomId = "room-1",
                currentSort = DeckSort.GGUK_PICK,
                exhausted =
                    setOf(
                        DeckKey("room-1", DeckSort.GGUK_PICK),
                        DeckKey("room-1", DeckSort.LATEST),
                    ),
            )

        assertEquals(NextDeck.SameRoom(DeckSort.NEAREST), resolveNextDeck(context))
    }

    /** TS-019 — `placeCount == 0`인 방은 어느 정렬도 후보가 될 수 없어 통째로 건너뛴다. */
    @Test
    fun `placeCount 0인 방을 건너뛴다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-empty", placeCount = 0), room("room-2")),
                currentRoomId = "room-empty",
                currentSort = DeckSort.GGUK_PICK,
                exhausted = emptySet(),
            )

        assertEquals(NextDeck.NextRoom("room-2", DeckSort.GGUK_PICK), resolveNextDeck(context))
    }

    /**
     * TS-019a, FR-012 — `context.rooms`는 이미 「개인방 먼저, 생성 오래된 순」으로 정렬된 목록이라고
     * 보고 받은 순서를 그대로 훑는다. 타입으로 재정렬했다면 개인방인 room-a가 먼저 고려됐겠지만,
     * 목록에서 먼저 오는 room-z가 답이어야 한다 — 재배치하지 않는다는 증거다.
     */
    @Test
    fun `방 순서를 받은 목록 그대로 훑는다`() {
        val context =
            deckContext(
                rooms =
                    listOf(
                        room("room-z", type = RoomType.GROUP),
                        room("room-a", type = RoomType.PERSONAL),
                    ),
                currentRoomId = "room-a",
                currentSort = DeckSort.GGUK_PICK,
                exhausted = emptySet(),
            )

        assertEquals(NextDeck.NextRoom("room-z", DeckSort.GGUK_PICK), resolveNextDeck(context))
    }

    /**
     * TS-021 — 사용자가 칩으로 `가까운순`까지 건너뛰어 `currentSort = NEAREST`이지만 `꾹 Pick`은
     * 소진된 적이 없다. 격자 순회는 이 칸을 다시 데려와 **앞선 정렬로 되돌아간다.**
     */
    @Test
    fun `칩으로 건너뛴 칸을 순회가 다시 데려온다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2")),
                currentRoomId = "room-1",
                currentSort = DeckSort.NEAREST,
                exhausted = setOf(DeckKey("room-1", DeckSort.NEAREST)),
            )

        assertEquals(NextDeck.SameRoom(DeckSort.GGUK_PICK), resolveNextDeck(context))
    }

    /** TS-024 — 격자 전체(모든 정렬 × 모든 방)가 소진되면 완료다. */
    @Test
    fun `남은 칸이 없으면 AllExhausted다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2")),
                currentRoomId = "room-2",
                currentSort = DeckSort.NEAREST,
                exhausted = allKeys(listOf("room-1", "room-2")),
            )

        assertEquals(NextDeck.AllExhausted, resolveNextDeck(context))
    }

    /**
     * EC-009 — 위치 권한 거부로 `가까운순`이 **모든 방에 대해** 소진 처리돼도(방마다 다시 묻지 않는다),
     * 다른 정렬에 남은 칸(room-2의 `최신순`)이 있으면 그리로 간다.
     */
    @Test
    fun `가까운순을 모든 방에 대해 소진 처리해도 남은 칸을 고른다`() {
        val context =
            deckContext(
                rooms = listOf(room("room-1"), room("room-2")),
                currentRoomId = "room-1",
                currentSort = DeckSort.NEAREST,
                exhausted =
                    setOf(
                        DeckKey("room-1", DeckSort.GGUK_PICK),
                        DeckKey("room-2", DeckSort.GGUK_PICK),
                        DeckKey("room-1", DeckSort.LATEST),
                        DeckKey("room-1", DeckSort.NEAREST),
                        DeckKey("room-2", DeckSort.NEAREST),
                    ),
            )

        assertEquals(NextDeck.NextRoom("room-2", DeckSort.LATEST), resolveNextDeck(context))
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

    private fun allKeys(roomIds: List<String>): Set<DeckKey> =
        roomIds.flatMap { roomId -> DeckSort.entries.map { DeckKey(roomId, it) } }.toSet()
}
