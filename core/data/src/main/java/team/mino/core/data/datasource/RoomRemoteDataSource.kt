package team.mino.core.data.datasource

import team.mino.core.data.network.dto.request.RoomRequest
import team.mino.core.data.network.dto.response.RoomResponse

/**
 * 방의 원격 출처. 계약은 `docs/specs/group-room-form/contracts/room-api-mock.md` §3이 소유한다.
 *
 * 지금의 유일한 구현은 mock이며, 실서버가 붙어도 이 인터페이스는 바뀌지 않는다 — 바뀌는 것은 바인딩 대상뿐이다.
 */
internal interface RoomRemoteDataSource {
    /** 없는 `roomId`면 `MinoDomainException.Http(404, ...)`가 전파된다. */
    suspend fun getRoom(roomId: String): RoomResponse

    suspend fun createRoom(request: RoomRequest): RoomResponse

    /** 없는 `roomId`면 `MinoDomainException.Http(404, ...)`가 전파된다. */
    suspend fun updateRoom(
        roomId: String,
        request: RoomRequest,
    ): RoomResponse
}
