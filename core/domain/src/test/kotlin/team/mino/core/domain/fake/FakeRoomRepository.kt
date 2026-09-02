package team.mino.core.domain.fake

import kotlinx.coroutines.flow.Flow
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomDraft
import team.mino.core.domain.model.RoomMember
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.repository.RoomRepository

/**
 * 방 목록 조회를 흔들기 위한 [RoomRepository] 테스트 더블.
 *
 * [rooms]가 이 더블의 존재 이유다 — 서버가 준 순서를 그대로 세워 두고, 정렬이 UseCase에서 일어나는지를
 * 반환값으로 판정한다(`contracts/room-list-api.md` §4).
 *
 * 조회와 무관한 세 함수는 호출되면 실패한다. 목록 조회 경로가 다른 함수를 건드리지 않는다는 것도 계약이기 때문이다.
 *
 * `CreateRoomUseCaseTest`가 파일 안에 사사로이 둔 더블과 별개다. 그쪽은 `createRoom`에 도달한 초안을 보는
 * 물건이라 목록 조회와 공유할 지점이 없다.
 */
class FakeRoomRepository : RoomRepository {
    /** [getRooms]가 돌려줄 목록. 서버가 준 순서를 그대로 담는다. */
    var rooms: List<RoomSummary> = emptyList()

    /** [getRooms]가 호출된 횟수. */
    var getRoomsCallCount: Int = 0
        private set

    /** [getRooms]에 마지막으로 넘어온 `placeId`. */
    var lastPlaceId: String? = null
        private set

    override fun observeMyRooms(): Flow<List<Room>> = error("방 목록 조회는 observeMyRooms를 부르지 않는다.")

    override suspend fun getRooms(placeId: String?): List<RoomSummary> {
        getRoomsCallCount++
        lastPlaceId = placeId
        return rooms
    }

    override suspend fun getRoom(roomId: String): Room = error("방 목록 조회는 getRoom을 부르지 않는다.")

    override suspend fun createRoom(draft: RoomDraft): Room = error("방 목록 조회는 createRoom을 부르지 않는다.")

    override suspend fun updateRoom(
        roomId: String,
        draft: RoomDraft,
    ): Room = error("방 목록 조회는 updateRoom을 부르지 않는다.")

    override suspend fun getMembers(roomId: String): List<RoomMember> = error("방 목록 조회는 getMembers를 부르지 않는다.")

    override suspend fun createInvitation(roomId: String): String = error("방 목록 조회는 createInvitation을 부르지 않는다.")

    override suspend fun leaveRoom(roomId: String): Unit = error("방 목록 조회는 leaveRoom을 부르지 않는다.")

    override suspend fun transferOwner(
        roomId: String,
        nextOwnerId: String,
    ): Unit = error("방 목록 조회는 transferOwner를 부르지 않는다.")
}
