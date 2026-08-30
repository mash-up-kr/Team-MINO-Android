package team.mino.core.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import team.mino.core.domain.fake.FakeRoomRepository
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.model.RoomType

/**
 * 개인방을 최상단으로 올리는 책임이 이 UseCase에 있는지 본다(FR-005 · `contracts/room-list-api.md` §4).
 *
 * 판정 대상은 **반환된 목록의 순서**다. 서버에는 정렬을 요구하지 않으므로 [FakeRoomRepository]는 개인방을 뒤섞인
 * 자리에 세워 두고, 그 자리에서 앞으로 올라오는지 확인한다.
 *
 * 공동방 사이의 순서는 앱이 정하지 않는다 — 서버가 준 순서를 유지하는지만 본다. 어떤 기준으로 정렬됐는지는
 * spec §4 가정이 "정렬 기준 없음"으로 닫았으므로 여기서 판정하지 않는다.
 */
class GetRoomPickerRoomsUseCaseTest {
    private val roomRepository = FakeRoomRepository()
    private val getRoomPickerRooms = GetRoomPickerRoomsUseCase(roomRepository = roomRepository)

    @Test
    fun `개인방이 목록 중간에 있어도 최상단으로 올린다`() =
        runTest {
            roomRepository.rooms =
                listOf(
                    groupRoom("room-1"),
                    personalRoom("room-personal"),
                    groupRoom("room-2"),
                )

            val result = getRoomPickerRooms()

            assertEquals("room-personal", result.first().id)
        }

    @Test
    fun `개인방을 올린 뒤 공동방은 서버가 준 순서를 유지한다`() =
        runTest {
            roomRepository.rooms =
                listOf(
                    groupRoom("room-1"),
                    groupRoom("room-2"),
                    personalRoom("room-personal"),
                    groupRoom("room-3"),
                )

            val result = getRoomPickerRooms()

            assertEquals(
                listOf("room-personal", "room-1", "room-2", "room-3"),
                result.map { it.id },
            )
        }

    @Test
    fun `개인방이 이미 최상단이면 순서를 바꾸지 않는다`() =
        runTest {
            roomRepository.rooms =
                listOf(
                    personalRoom("room-personal"),
                    groupRoom("room-1"),
                    groupRoom("room-2"),
                )

            val result = getRoomPickerRooms()

            assertEquals(
                listOf("room-personal", "room-1", "room-2"),
                result.map { it.id },
            )
        }

    @Test
    fun `개인방이 없으면 서버가 준 순서 그대로다`() =
        runTest {
            roomRepository.rooms =
                listOf(
                    groupRoom("room-1"),
                    groupRoom("room-2"),
                    groupRoom("room-3"),
                )

            val result = getRoomPickerRooms()

            assertEquals(
                listOf("room-1", "room-2", "room-3"),
                result.map { it.id },
            )
        }

    @Test
    fun `개인방 하나뿐이어도 그대로 돌려준다`() =
        runTest {
            roomRepository.rooms = listOf(personalRoom("room-personal"))

            val result = getRoomPickerRooms()

            assertEquals(listOf("room-personal"), result.map { it.id })
        }

    @Test
    fun `방이 하나도 없으면 빈 목록이다`() =
        runTest {
            roomRepository.rooms = emptyList()

            val result = getRoomPickerRooms()

            assertEquals(emptyList<RoomSummary>(), result)
        }

    @Test
    fun `정렬은 앱이 하므로 Repository는 한 번만 부른다`() =
        runTest {
            roomRepository.rooms = listOf(personalRoom("room-personal"), groupRoom("room-1"))

            getRoomPickerRooms()

            assertEquals(1, roomRepository.getRoomsCallCount)
        }

    private fun personalRoom(id: String): RoomSummary = roomSummary(id = id, type = RoomType.PERSONAL, name = "내 장소")

    private fun groupRoom(id: String): RoomSummary = roomSummary(id = id, type = RoomType.GROUP, name = "민호야 잘하자")

    private fun roomSummary(
        id: String,
        type: RoomType,
        name: String,
    ): RoomSummary =
        RoomSummary(
            id = id,
            name = name,
            description = "",
            type = type,
            color = RoomColor.GRAY,
            placeCount = 0,
            thumbnailImageUrls = emptyList(),
        )
}
