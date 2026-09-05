package team.mino.feature.sharereceiver.fake

import kotlinx.coroutines.flow.Flow
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomDraft
import team.mino.core.domain.model.RoomMember
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.repository.RoomRepository
import team.mino.core.errorhandling.MinoDomainException

/**
 * `:feature:sharereceiver` 테스트용 [RoomRepository] 테스트 더블.
 *
 * 목록 조회 하나만 실제로 응답하고 나머지 셋은 호출되면 터진다. 방 선택 시트는 방을 만들지도 고치지도 않으므로,
 * 그 셋에 닿는 것 자체가 계약 위반이다.
 *
 * [getRoomsFailure]가 이 더블의 존재 이유다 — 조회 실패가 별도 오류 상태를 만들지 않고 빈 목록으로 수렴하는지를
 * 판정하려면(`research.md` R-006) 실패를 주입할 수 있어야 한다. 실서버·mock 어느 쪽에도 실패 스위치가 없다.
 *
 * 실패 타입이 [MinoDomainException]인 것은 편의가 아니라 계약이다. `RoomRepository.getRooms()`는 실패를
 * 이 타입으로 던지기로 돼 있고, 다른 예외를 주입하면 검증하려던 통로가 아니라 CEH로 빠진다.
 *
 * [getRoomsCallCount]는 두 방향으로 쓰인다 — 세션이 없을 때 **조회가 아예 일어나지 않는 것**과,
 * 실패 수렴 케이스에서 **조회가 실제로 일어난 뒤 실패한 것**을 각각 확인한다.
 */
internal class FakeRoomRepository : RoomRepository {
    /** [getRooms]가 돌려줄 목록. 서버가 준 순서를 그대로 담는다. */
    var rooms: List<RoomSummary> = emptyList()

    /** 값이 있으면 [getRooms]가 목록 대신 이 예외를 던진다 — 오프라인·5xx(R-006). */
    var getRoomsFailure: MinoDomainException? = null

    /** [getRooms]가 호출된 횟수. */
    var getRoomsCallCount: Int = 0
        private set

    override fun observeMyRooms(): Flow<List<Room>> = error("방 선택 시트는 observeMyRooms를 부르지 않는다.")

    override suspend fun getRooms(placeId: String?): List<RoomSummary> {
        getRoomsCallCount++
        getRoomsFailure?.let { throw it }
        return rooms
    }

    override suspend fun getRoom(roomId: String): Room = error("방 선택 시트는 방 하나를 조회하지 않는다.")

    override suspend fun createRoom(draft: RoomDraft): Room = error("방 선택 시트는 방을 만들지 않는다.")

    override suspend fun updateRoom(
        roomId: String,
        draft: RoomDraft,
    ): Room = error("방 선택 시트는 방을 고치지 않는다.")

    override suspend fun getMembers(roomId: String): List<RoomMember> = error("방 선택 시트는 멤버 목록을 조회하지 않는다.")

    override suspend fun createInvitation(roomId: String): String = error("방 선택 시트는 초대 링크를 발급하지 않는다.")

    override suspend fun leaveRoom(roomId: String): Unit = error("방 선택 시트는 방에서 나가지 않는다.")

    override suspend fun transferOwner(
        roomId: String,
        nextOwnerId: String,
    ): Unit = error("방 선택 시트는 방장 위임을 하지 않는다.")
}
