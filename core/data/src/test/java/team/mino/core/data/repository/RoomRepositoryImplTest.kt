package team.mino.core.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import team.mino.core.data.datasource.FakeRoomRemoteDataSource
import team.mino.core.data.network.dto.response.RoomResponse
import team.mino.core.data.network.dto.response.RoomSummaryResponse

class RoomRepositoryImplTest {
    private val dataSource = FakeRoomRemoteDataSource()
    private val repository = RoomRepositoryImpl(remoteDataSource = dataSource)

    @Test
    fun `observeMyRooms는 데이터소스가 내려준 방을 도메인 모델로 매핑해 방출한다`() =
        runTest {
            dataSource.rooms = listOf(response(id = "personal", type = "personal"), response(id = "group-1"))

            val rooms = repository.observeMyRooms().first()

            assertEquals(listOf("personal", "group-1"), rooms.map { it.id })
            assertEquals(true, rooms.first { it.id == "personal" }.isPersonal)
        }

    @Test
    fun `getRooms는 placeId를 showHasPlaceId 쿼리로 넘긴다`() =
        runTest {
            dataSource.rooms = listOf(response(id = "r1", hasPlace = true, matchedPinId = "pin-1"))

            val rooms = repository.getRooms(placeId = "place-1")

            assertEquals("place-1", dataSource.lastShowHasPlaceId)
            assertEquals(true, rooms.single().hasPlace)
            assertEquals("pin-1", rooms.single().matchedPinId)
        }

    @Test
    fun `placeId 없는 getRooms는 저장 여부를 묻지 않고 두 필드를 null로 남긴다`() =
        runTest {
            dataSource.rooms = listOf(response(id = "r1"))

            val rooms = repository.getRooms()

            assertEquals(null, dataSource.lastShowHasPlaceId)
            assertEquals(null, rooms.single().hasPlace)
            assertEquals(null, rooms.single().matchedPinId)
        }

    @Test
    fun `getRoom은 데이터소스의 단건 조회 결과를 그대로 반환한다`() =
        runTest {
            dataSource.room = roomResponse(id = "room-2")

            val room = repository.getRoom("room-2")

            assertEquals("room-2", room.id)
        }

    private fun response(
        id: String,
        type: String = "group",
        hasPlace: Boolean? = null,
        matchedPinId: String? = null,
    ): RoomSummaryResponse =
        RoomSummaryResponse(
            id = id,
            type = type,
            name = "테스트 방",
            description = null,
            color = "gray",
            ownerId = "owner-1",
            pinCount = 0,
            memberCount = 1,
            hasPlace = hasPlace,
            matchedPinId = matchedPinId,
        )

    private fun roomResponse(id: String): RoomResponse =
        RoomResponse(
            id = id,
            name = "테스트 방",
            description = null,
            color = "gray",
            ownerId = "owner-1",
        )
}
