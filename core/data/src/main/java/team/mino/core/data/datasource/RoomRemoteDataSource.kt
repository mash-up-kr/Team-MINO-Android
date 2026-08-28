package team.mino.core.data.datasource

import team.mino.core.data.network.dto.request.RoomRequest
import team.mino.core.data.network.dto.response.RoomResponse
import team.mino.core.data.network.dto.response.RoomSummaryResponse

/**
 * 방의 원격 출처. 계약은 `docs/specs/group-room-form/contracts/room-api.md` §4가 소유한다.
 *
 * 같은 `room` 리소스를 다루던 `RoomListRemoteDataSource`가 이 인터페이스로 합쳐졌다 —
 * 근거는 `docs/specs/group-room-form/research.md` R-032.
 */
internal interface RoomRemoteDataSource {
    /**
     * 참여 중인 방 목록을 조회한다. 나간 방은 서버가 제외한다.
     *
     * 세션이 없거나(`401`) 네트워크·서버 오류면 `MinoDomainException`이 전파된다.
     * 그 셋을 빈 목록으로 수렴시키는 것은 화면의 몫이다. 이 함수의 시그니처·실패 계약은
     * 자리를 옮긴 뒤에도 `docs/specs/shared-link-receiver/contracts/room-list-api.md` §6이 소유한다.
     */
    suspend fun listRooms(): List<RoomSummaryResponse>

    /** 없는 `roomId`면 `MinoDomainException.Http(404, ...)`가 전파된다. */
    suspend fun getRoom(roomId: String): RoomResponse

    suspend fun createRoom(request: RoomRequest): RoomResponse

    /** 없는 `roomId`면 `MinoDomainException.Http(404, ...)`가 전파된다. */
    suspend fun updateRoom(
        roomId: String,
        request: RoomRequest,
    ): RoomResponse
}
