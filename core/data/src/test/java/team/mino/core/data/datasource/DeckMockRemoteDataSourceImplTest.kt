package team.mino.core.data.datasource

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.data.datasource.mock.DeckMockStore
import team.mino.core.domain.model.DeckSort
import team.mino.core.errorhandling.MinoDomainException

/**
 * mock이 `docs/specs/home-deck-exploration/contracts/deck-api.md` §4의 「반드시 재현해야 하는 경우」 5종을
 * 실제로 내려주는지 본다. 하나라도 빠지면 TS-005·TS-014·TS-017·TS-023·EC-013을 검증할 수 없다.
 *
 * 어떤 `roomId`가 어떤 프로필을 받는지는 mock의 내부 사정이라 특정 방을 지목하지 않는다. 방을 여럿 훑어
 * **5종이 모두 나오는지**만 본다 — 배정 방식이 바뀌어도 계약을 지키는 한 이 판정은 그대로다.
 */
class DeckMockRemoteDataSourceImplTest {
    private val dataSource = DeckMockRemoteDataSourceImpl(DeckMockStore())

    @Test
    fun `10장 상한을 넘기지 않고 라벨 4종이 모두 섞인 덱이 있다`() =
        runTest {
            val labels =
                roomIdsForProbe
                    .map { roomId -> dataSource.getCards(roomId, DeckSort.GGUK_PICK) }
                    .also { decks -> assertTrue(decks.all { it.size <= MAX_DECK_SIZE }) }
                    .map { deck -> deck.map { it.labelGroup }.toSet() }

            assertTrue(
                "labelGroup 4종이 한 덱에 섞여 있어야 한다 (TS-014)",
                labels.any { it == setOf("worthVisiting", "manySaves", "manyComments", "manyViews") },
            )
        }

    @Test
    fun `10장 미만인 덱과 후보 0건인 정렬이 모두 있다`() =
        runTest {
            val sizes =
                roomIdsForProbe.flatMap { roomId ->
                    DeckSort.entries.map { sort -> dataSource.getCards(roomId, sort, LAT, LNG).size }
                }

            assertTrue("10장 미만인 덱이 있어야 한다 (TS-005)", sizes.any { it in 1 until MAX_DECK_SIZE })
            assertTrue("후보 0건인 정렬이 있어야 한다 (TS-017·TS-023·EC-013)", sizes.any { it == 0 })
        }

    @Test
    fun `정렬끼리 후보가 겹치는 방이 있다`() =
        runTest {
            val overlaps =
                roomIdsForProbe.map { roomId ->
                    val ggukPick = dataSource.getCards(roomId, DeckSort.GGUK_PICK).map { it.id }.toSet()
                    val latest = dataSource.getCards(roomId, DeckSort.LATEST).map { it.id }.toSet()
                    (ggukPick intersect latest).size
                }

            assertTrue("덱 간 중복을 제거하지 않는다 (spec §4 가정)", overlaps.any { it > 0 })
        }

    @Test
    fun `지표가 전부 0인 방은 모든 카드가 worthVisiting이다`() =
        runTest {
            val allWorthVisitingRooms =
                roomIdsForProbe
                    .map { roomId -> dataSource.getCards(roomId, DeckSort.GGUK_PICK) }
                    .filter { deck -> deck.isNotEmpty() && deck.all { it.labelGroup == "worthVisiting" } }

            assertTrue("전부 worthVisiting인 방이 있어야 한다 (FR-008)", allWorthVisitingRooms.isNotEmpty())
        }

    @Test
    fun `가까운순인데 좌표가 없으면 400이다`() =
        runTest {
            val failure =
                runCatching {
                    dataSource.getCards("room-any", DeckSort.NEAREST, lat = null, lng = null)
                }.exceptionOrNull()

            assertTrue(failure is MinoDomainException.Http)
            assertEquals(400, (failure as MinoDomainException.Http).code)
        }

    @Test
    fun `같은 방은 언제 물어도 같은 덱을 준다`() =
        runTest {
            val first = dataSource.getCards("room-stable", DeckSort.GGUK_PICK)
            val second = dataSource.getCards("room-stable", DeckSort.GGUK_PICK)

            assertEquals(first, second)
        }

    /** 프로필 배정이 `roomId` 해시로 갈리므로, 네 프로필을 모두 만나기에 충분한 만큼 훑는다. */
    private val roomIdsForProbe: List<String> = (1..40).map { "mock-room-$it" }

    private companion object {
        const val MAX_DECK_SIZE = 10

        const val LAT = 37.5563
        const val LNG = 126.9236
    }
}
