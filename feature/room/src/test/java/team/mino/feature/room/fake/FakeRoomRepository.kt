package team.mino.feature.room.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import team.mino.core.domain.model.Room
import team.mino.core.domain.repository.RoomRepository

/**
 * `:feature:room` 테스트용 [RoomRepository] 테스트 더블.
 *
 * [givenRooms]로 방 목록을 미리 채워 두고 [observeMyRooms]로 흘려보낸다 — 개인방/공동방 구분은
 * [Room.isPersonal] 필드로만 판정하므로, 이 더블은 그 값을 그대로 통과시킬 뿐 별도 규칙을 갖지 않는다.
 */
internal class FakeRoomRepository : RoomRepository {
    private val rooms = MutableStateFlow<List<Room>>(emptyList())

    fun givenRooms(vararg values: Room) {
        rooms.value = values.toList()
    }

    override fun observeMyRooms(): Flow<List<Room>> = rooms

    override suspend fun getRoom(roomId: String): Room = rooms.value.first { it.id == roomId }
}
