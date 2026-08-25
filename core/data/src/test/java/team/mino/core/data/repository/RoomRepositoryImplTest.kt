package team.mino.core.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import team.mino.core.data.datasource.FakeRoomRemoteDataSource
import team.mino.core.data.network.dto.response.RoomSummaryResponse

class RoomRepositoryImplTest {
    private val dataSource = FakeRoomRemoteDataSource()
    private val repository = RoomRepositoryImpl(dataSource = dataSource)

    @Test
    fun `observeMyRooms는 데이터소스가 내려준 방을 도메인 모델로 매핑해 방출한다`() =
        runTest {
            dataSource.rooms = listOf(response(id = "personal", type = "personal"), response(id = "group-1"))

            val rooms = repository.observeMyRooms().first()

            assertEquals(listOf("personal", "group-1"), rooms.map { it.id })
            assertEquals(true, rooms.first { it.id == "personal" }.isPersonal)
        }

    @Test
    fun `getRoom은 id가 일치하는 방을 찾아 반환한다`() =
        runTest {
            dataSource.rooms = listOf(response(id = "room-1"), response(id = "room-2"))

            val room = repository.getRoom("room-2")

            assertEquals("room-2", room.id)
        }

    @Test
    fun `getRoom은 일치하는 방이 없으면 예외를 던진다`() =
        runTest {
            dataSource.rooms = listOf(response(id = "room-1"))

            val result = runCatching { repository.getRoom("missing") }

            assertEquals(true, result.isFailure)
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
}
