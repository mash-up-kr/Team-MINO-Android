package team.mino.feature.room.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomDraft
import team.mino.core.domain.model.RoomMember
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.repository.RoomRepository

/**
 * `:feature:room` 테스트용 [RoomRepository] 테스트 더블.
 *
 * [givenRooms]로 방 목록을 미리 채워 두고 [observeMyRooms]로 흘려보낸다 — 개인방/공동방 구분은
 * [Room.isPersonal] 필드로만 판정하므로, 이 더블은 그 값을 그대로 통과시킬 뿐 별도 규칙을 갖지 않는다.
 *
 * [createRoom]·[updateRoom]은 `:feature:room`(목록 관찰)이 부르지 않는다 — group-room-form과 병합된
 * 인터페이스를 만족시키기 위한 자리표시자다.
 */
internal class FakeRoomRepository : RoomRepository {
    private val rooms = MutableStateFlow<List<Room>>(emptyList())

    fun givenRooms(vararg values: Room) {
        rooms.value = values.toList()
    }

    override fun observeMyRooms(): Flow<List<Room>> = rooms

    override suspend fun getRooms(): List<RoomSummary> = error("FakeRoomRepository는 getRooms를 지원하지 않는다.")

    override suspend fun getRoom(roomId: String): Room = rooms.value.first { it.id == roomId }

    override suspend fun createRoom(draft: RoomDraft): Room = error("FakeRoomRepository는 createRoom을 지원하지 않는다.")

    override suspend fun updateRoom(
        roomId: String,
        draft: RoomDraft,
    ): Room = error("FakeRoomRepository는 updateRoom을 지원하지 않는다.")

    override suspend fun getMembers(roomId: String): List<RoomMember> =
        error("FakeRoomRepository는 getMembers를 지원하지 않는다.")

    override suspend fun createInvitation(roomId: String): String =
        error("FakeRoomRepository는 createInvitation을 지원하지 않는다.")

    override suspend fun leaveRoom(roomId: String): Unit = error("FakeRoomRepository는 leaveRoom을 지원하지 않는다.")

    override suspend fun transferOwner(
        roomId: String,
        nextOwnerId: String,
    ): Unit = error("FakeRoomRepository는 transferOwner를 지원하지 않는다.")
}
