package team.mino.core.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.data.datasource.DeckRemoteDataSource
import team.mino.core.data.datasource.PinRemoteDataSource
import team.mino.core.data.datasource.RoomListRemoteDataSource
import team.mino.core.data.network.dto.request.PinCreateRequest
import team.mino.core.data.network.dto.request.PinDuplicateRequest
import team.mino.core.data.network.dto.response.CardPlaceResponse
import team.mino.core.data.network.dto.response.CardResponse
import team.mino.core.data.network.dto.response.RoomSummaryResponse
import team.mino.core.domain.model.DeckSort
import team.mino.core.errorhandling.MinoDomainException
import java.io.IOException

/**
 * `HomeDeckRepositoryImpl`이 **더하는 규칙**만 판정한다. 위임뿐인 함수는 컴파일이 이미 보증하므로 셋만 본다.
 *
 * 1. 받은 카드를 다시 자르거나 정렬하지 않는다 — 10장 절단은 서버 몫이다
 *    (FR-004, `docs/specs/home-deck-exploration/data-model.md` §1.3).
 * 2. 좌표 없는 `가까운순`은 **요청 자체를 보내지 않는다**. 빈 덱만 확인하면 "불렀는데 빈 덱이 왔다"와 구별되지
 *    않으므로 호출 횟수 0을 함께 본다(EC-009, R-013).
 * 3. 다른 방 저장의 실패를 흡수하지 않는다. 저장되지 않은 것이 저장된 것으로 보이면 안 된다(FR-005).
 */
class HomeDeckRepositoryImplTest {
    private val deckRemoteDataSource = RecordingDeckRemoteDataSource()
    private val pinRemoteDataSource = RecordingPinRemoteDataSource()
    private val repository =
        HomeDeckRepositoryImpl(
            deckRemoteDataSource = deckRemoteDataSource,
            roomListRemoteDataSource = UnusedRoomListRemoteDataSource,
            pinRemoteDataSource = pinRemoteDataSource,
        )

    @Test
    fun `서버가 준 열 장을 그대로 담는다`() =
        runTest {
            // 정원을 채운 10장을, 이름·id 어느 것으로도 정렬돼 있지 않은 순서로 준다 —
            // 그래야 "자르지 않는다"와 "정렬하지 않는다"를 한 단언이 함께 잡는다.
            val responses = listOf(9, 4, 7, 0, 2, 8, 1, 6, 3, 5).map { cardResponse("pin-$it") }
            deckRemoteDataSource.cards = responses

            val deck = repository.getDeck(roomId = "room-1", sort = DeckSort.GGUK_PICK)

            assertEquals(
                "여기서 다시 자르거나 정렬하면 서버가 정한 순위·정원과 어긋난다",
                responses.map { it.id },
                deck.cards.map { it.pinId },
            )
        }

    @Test
    fun `가까운순인데 좌표가 없으면 요청을 보내지 않는다`() =
        runTest {
            deckRemoteDataSource.cards = listOf(cardResponse("pin-0"))

            val deck = repository.getDeck(roomId = "room-1", sort = DeckSort.NEAREST, location = null)

            assertEquals(
                "좌표 없는 가까운순은 400이 확정이라 부르는 것 자체가 틀렸다",
                0,
                deckRemoteDataSource.callCount,
            )
            assertTrue(deck.cards.isEmpty())
        }

    @Test
    fun `가까운순이 아니면 좌표가 있어도 싣지 않는다`() =
        runTest {
            repository.getDeck(
                roomId = "room-1",
                sort = DeckSort.LATEST,
                location = GeoPoint(latitude = 37.5, longitude = 127.0),
            )

            assertEquals(1, deckRemoteDataSource.callCount)
            assertEquals(null, deckRemoteDataSource.lastLat)
            assertEquals(null, deckRemoteDataSource.lastLng)
        }

    @Test
    fun `다른 방 저장의 중복 실패는 그대로 올라온다`() =
        runTest {
            val conflict = MinoDomainException.Http(code = 409, cause = IOException())
            pinRemoteDataSource.duplicateFailure = conflict

            val thrown =
                try {
                    repository.savePinToRoom(pinId = "pin-0", roomId = "room-2")
                    null
                } catch (e: MinoDomainException) {
                    e
                }

            assertSame("409를 흡수하면 저장되지 않은 것이 저장된 것으로 보인다", conflict, thrown)
        }

    private fun cardResponse(id: String): CardResponse =
        CardResponse(
            id = id,
            place = CardPlaceResponse(name = "장소 $id", address = "서울시"),
            labelGroup = "worthVisiting",
        )

    private class RecordingDeckRemoteDataSource : DeckRemoteDataSource {
        var cards: List<CardResponse> = emptyList()

        var callCount: Int = 0
            private set

        var lastLat: Double? = null
            private set

        var lastLng: Double? = null
            private set

        override suspend fun getCards(
            roomId: String,
            sort: DeckSort,
            lat: Double?,
            lng: Double?,
        ): List<CardResponse> {
            callCount++
            lastLat = lat
            lastLng = lng
            return cards
        }
    }

    private class RecordingPinRemoteDataSource : PinRemoteDataSource {
        var duplicateFailure: Throwable? = null

        override suspend fun createPin(request: PinCreateRequest) = Unit

        override suspend fun recordAccess(pinId: String) = Unit

        override suspend fun duplicatePin(
            pinId: String,
            request: PinDuplicateRequest,
        ) {
            duplicateFailure?.let { throw it }
        }
    }

    /** 덱과 저장만 보는 테스트라 방 목록은 닿지 않는다. 닿으면 그것 자체가 실패다. */
    private object UnusedRoomListRemoteDataSource : RoomListRemoteDataSource {
        override suspend fun listRooms(): List<RoomSummaryResponse> = throw IllegalStateException("부르지 않는다")
    }
}
