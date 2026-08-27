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
    fun `getRoom은 데이터소스의 단건 조회 결과를 그대로 반환한다`() =
        runTest {
            dataSource.room = roomResponse(id = "room-2")

            val room = repository.getRoom("room-2")

            assertEquals("room-2", room.id)
        }

    private fun response(
        id: String,
        type: String = "group",
    ): RoomSummaryResponse =
        RoomSummaryResponse(
            id = id,
            type = type,
            name = "테스트 방",
            description = null,
            color = null,
            ownerId = "owner-1",
            inviteCode = "invite-1",
            createdAt = "2026-08-25T00:00:00Z",
            pinCount = 0,
            memberCount = 1,
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
