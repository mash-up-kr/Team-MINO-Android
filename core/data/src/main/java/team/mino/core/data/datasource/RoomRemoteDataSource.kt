package team.mino.core.data.datasource

import team.mino.core.data.network.dto.request.RoomRequest
import team.mino.core.data.network.dto.response.RoomInvitationResponse
import team.mino.core.data.network.dto.response.RoomMemberDetailResponse
import team.mino.core.data.network.dto.response.RoomResponse
import team.mino.core.data.network.dto.response.RoomSummaryResponse

/**
 * 방의 원격 출처. [getRoom]·[createRoom]·[updateRoom]의 계약은
 * `docs/specs/group-room-form/contracts/room-api-mock.md` §3이 소유한다. [getMembers]·[createInvitation]·
 * [leaveRoom]·[transferOwner]의 계약은 `docs/specs/room-detail/contracts/place-repository.md`
 * "RoomRepository 확장"이 소유한다.
 *
 * 유일한 구현([RoomRemoteDataSourceImpl])이 [getRooms]·[getMembers]·[createInvitation]·[leaveRoom]·
 * [transferOwner]는 실서버(`RoomApiService`)에, [getRoom]·[createRoom]·[updateRoom]은
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

    /** 방 멤버 전체 목록. `GET /api/v1/rooms/{roomId}/members`. */
    suspend fun getMembers(roomId: String): List<RoomMemberDetailResponse>

    /** 내 초대 링크 코드 발급. `POST /api/v1/rooms/{roomId}/invitations`. */
    suspend fun createInvitation(roomId: String): RoomInvitationResponse

    /** 방 나가기. `DELETE /api/v1/rooms/{roomId}/members/me`. */
    suspend fun leaveRoom(roomId: String)

    /** 방장 위임. `PUT /api/v1/rooms/{roomId}/owner`. */
    suspend fun transferOwner(
        roomId: String,
        nextOwnerId: String,
    )
}
