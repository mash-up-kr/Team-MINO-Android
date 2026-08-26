package team.mino.core.data.datasource

import team.mino.core.data.network.dto.request.RoomRequest
import team.mino.core.data.network.dto.response.RoomResponse
import team.mino.core.data.network.dto.response.RoomSummaryResponse

/**
 * 방의 원격 출처. [getRoom]·[createRoom]·[updateRoom]의 계약은
 * `docs/specs/group-room-form/contracts/room-api-mock.md` §3이 소유한다.
 *
 * 유일한 구현([RoomRemoteDataSourceImpl])이 [getRooms]는 실서버(`RoomApiService`)에, 나머지 셋은
 * mock(`RoomMockStore`)에 위임한다 — 실서버가 단건 조회·생성·수정까지 갖추면 그 구현 하나만 바뀐다.
 */
internal interface RoomRemoteDataSource {
    /** 방 리스트 조회. `GET /api/v1/rooms`. */
    suspend fun getRooms(): List<RoomSummaryResponse>

    /** 없는 `roomId`면 `MinoDomainException.Http(404, ...)`가 전파된다. */
    suspend fun getRoom(roomId: String): RoomResponse

    suspend fun createRoom(request: RoomRequest): RoomResponse

    /** 없는 `roomId`면 `MinoDomainException.Http(404, ...)`가 전파된다. */
    suspend fun updateRoom(
        roomId: String,
        request: RoomRequest,
    ): RoomResponse
}
