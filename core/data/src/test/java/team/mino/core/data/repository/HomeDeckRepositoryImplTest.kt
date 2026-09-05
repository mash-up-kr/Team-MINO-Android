package team.mino.core.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.data.datasource.DeckRemoteDataSource
import team.mino.core.data.datasource.RoomRemoteDataSource
import team.mino.core.data.network.dto.request.RoomRequest
import team.mino.core.data.network.dto.response.CardPlaceResponse
import team.mino.core.data.network.dto.response.CardResponse
import team.mino.core.data.network.dto.response.RoomInvitationResponse
import team.mino.core.data.network.dto.response.RoomMemberDetailResponse
import team.mino.core.data.network.dto.response.RoomResponse
import team.mino.core.data.network.dto.response.RoomSummaryResponse
import team.mino.core.domain.model.DeckSort

/**
 * `HomeDeckRepositoryImpl`이 **더하는 규칙**만 판정한다. 위임뿐인 함수는 컴파일이 이미 보증하므로 셋만 본다.
 *
 * 1. 받은 카드를 다시 자르거나 정렬하지 않는다 — 10장 절단은 서버 몫이다
 *    (FR-004, `docs/specs/home-deck-exploration/data-model.md` §1.3).
 * 2. 좌표 없는 `가까운순`은 **요청 자체를 보내지 않는다**. 빈 덱만 확인하면 "불렀는데 빈 덱이 왔다"와 구별되지
 *    않으므로 호출 횟수 0을 함께 본다(EC-009, R-013).
 * 3. `getRoomSummaries`가 순회 순서를 확정한다 — 개인방 먼저, 그다음 공동방을 생성 오래된 순으로.
 *    응답 순서에 기대지 않는다(FR-012, R-014, TS-019a,
 *    `docs/specs/home-deck-exploration/contracts/home-ui.md` §4.2).
 *
 * `savePinToRoom`(구 FR-005 중복 저장 409 전파)은 더 이상 이 Repository의 함수가 아니다 —
 * `PlaceRepository.duplicatePin`으로 옮겨졌고(R-019, 계약 §4.2.1), 같은 판정은 이미
 * `PlaceRepositoryImplTest.이미 저장된 방의 409는 그대로 올라온다`가 지킨다. 여기서 되풀이하지 않는다.
 */
class HomeDeckRepositoryImplTest {
    private val deckRemoteDataSource = RecordingDeckRemoteDataSource()
    private val repository =
        HomeDeckRepositoryImpl(
            deckRemoteDataSource = deckRemoteDataSource,
            roomRemoteDataSource = UnusedRoomRemoteDataSource,
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
    fun `순회 순서 - 개인방이 먼저, 공동방은 생성 오래된 순으로 잇는다`() =
        runTest {
            // 응답 순서에 기대지 않는다는 것까지 함께 잡기 위해, 원래 순서(개인방-A-B)와 다르게 뒤섞어 넣는다
            // (FR-012, R-014). 개인방이 어디 끼어 있어도 최상단으로 올라와야 한다.
            val personal =
                roomSummaryResponse(
                    id = "room-personal",
                    type = "personal",
                    createdAt = "2026-01-01T00:00:00Z",
                )
            val groupA = roomSummaryResponse(id = "room-group-a", type = "shared", createdAt = "2026-01-02T00:00:00Z")
            val groupB = roomSummaryResponse(id = "room-group-b", type = "shared", createdAt = "2026-01-03T00:00:00Z")
            val roomRemoteDataSource = StubRoomRemoteDataSource(rooms = listOf(groupB, groupA, personal))
            val repositoryWithRooms =
                HomeDeckRepositoryImpl(
                    deckRemoteDataSource = deckRemoteDataSource,
                    roomRemoteDataSource = roomRemoteDataSource,
                )

            val summaries = repositoryWithRooms.getRoomSummaries()

            assertEquals(
                "개인방이 먼저, 그다음 공동방은 만든 지 오래된 순이어야 한다(TS-019a)",
                listOf("room-personal", "room-group-a", "room-group-b"),
                summaries.map { it.id },
            )
        }

    @Test
    fun `생성 시각을 읽을 수 없는 방이 있어도 목록이 떨어지지 않고 맨 뒤로 밀린다`() =
        runTest {
            // 서버가 createdAt을 빼면 DTO 기본값 ""가 남는다. 그 값이 정렬 키로 들어가도 목록 전체가 실패하지
            // 않아야 한다 — 「방 하나의 값이 어긋났다는 이유로 목록 전체가 실패하면 안 된다」는 RoomSummaryMapper의
            // 규칙을 정렬까지 이은 것이다. 예외가 나면 MinoDomainException이 아니라 홈 첫 화면이 통째로 죽는다.
            val personal =
                roomSummaryResponse(
                    id = "room-personal",
                    type = "personal",
                    createdAt = "2026-01-01T00:00:00Z",
                )
            val groupA = roomSummaryResponse(id = "room-group-a", type = "shared", createdAt = "2026-01-02T00:00:00Z")
            val broken = roomSummaryResponse(id = "room-broken", type = "shared", createdAt = "")
            val roomRemoteDataSource = StubRoomRemoteDataSource(rooms = listOf(broken, groupA, personal))
            val repositoryWithRooms =
                HomeDeckRepositoryImpl(
                    deckRemoteDataSource = deckRemoteDataSource,
                    roomRemoteDataSource = roomRemoteDataSource,
                )

            val summaries = repositoryWithRooms.getRoomSummaries()

            assertEquals(
                "읽히지 않은 생성 시각은 순서를 잃을 뿐 목록에서 사라지지 않는다",
                listOf("room-personal", "room-group-a", "room-broken"),
                summaries.map { it.id },
            )
        }

    private fun roomSummaryResponse(
        id: String,
        type: String,
        createdAt: String,
    ): RoomSummaryResponse =
        RoomSummaryResponse(
            id = id,
            name = "방 $id",
            type = type,
            color = "gray",
            ownerId = "user-0",
            pinCount = 0,
            memberCount = 1,
            createdAt = createdAt,
        )

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

    /** 덱과 저장만 보는 테스트라 방 출처는 닿지 않는다. 닿으면 그것 자체가 실패다. */
    private object UnusedRoomRemoteDataSource : RoomRemoteDataSource {
        override suspend fun listRooms(showHasPlaceId: String?): List<RoomSummaryResponse> =
            throw IllegalStateException("부르지 않는다")

        override suspend fun getRoom(roomId: String): RoomResponse = throw IllegalStateException("부르지 않는다")

        override suspend fun createRoom(request: RoomRequest): RoomResponse = throw IllegalStateException("부르지 않는다")

        override suspend fun updateRoom(
            roomId: String,
            request: RoomRequest,
        ): RoomResponse = throw IllegalStateException("부르지 않는다")

        override suspend fun getMembers(roomId: String): List<RoomMemberDetailResponse> =
            throw IllegalStateException("부르지 않는다")

        override suspend fun createInvitation(roomId: String): RoomInvitationResponse =
            throw IllegalStateException("부르지 않는다")

        override suspend fun leaveRoom(roomId: String): Unit = throw IllegalStateException("부르지 않는다")

        override suspend fun transferOwner(
            roomId: String,
            nextOwnerId: String,
        ): Unit = throw IllegalStateException("부르지 않는다")

        override suspend fun joinRoom(
            roomId: String,
            inviteCode: String,
        ): Unit = throw IllegalStateException("부르지 않는다")
    }

    /** 순회 순서 테스트 전용. `listRooms`만 재정의하고 나머지는 [UnusedRoomRemoteDataSource]에 위임한다. */
    private class StubRoomRemoteDataSource(
        private val rooms: List<RoomSummaryResponse>,
    ) : RoomRemoteDataSource by UnusedRoomRemoteDataSource {
        override suspend fun listRooms(showHasPlaceId: String?): List<RoomSummaryResponse> = rooms
    }
}
