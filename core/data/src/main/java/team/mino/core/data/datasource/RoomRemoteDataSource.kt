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
    /**
     * 참여 중인 방 목록을 조회한다. 나간 방은 서버가 제외한다.
     *
     * 세션이 없거나(`401`) 네트워크·서버 오류면 `MinoDomainException`이 전파된다.
     * 그 셋을 빈 목록으로 수렴시키는 것은 화면의 몫이다. 이 함수의 실패 계약은
     * 자리를 옮긴 뒤에도 `docs/specs/shared-link-receiver/contracts/room-list-api.md` §6이 소유한다.
     *
     * @param showHasPlaceId 장소 UUID. 주면 각 응답에 `hasPlace`·`matchedPinId`가 함께 온다
     *  (`docs/specs/place-detail/contracts/place-api.md` §4).
     */
    suspend fun listRooms(showHasPlaceId: String? = null): List<RoomSummaryResponse>

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
