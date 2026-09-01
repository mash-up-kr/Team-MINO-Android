package team.mino.core.data.datasource

import team.mino.core.data.network.dto.request.RoomRequest
import team.mino.core.data.network.dto.response.RoomInvitationResponse
import team.mino.core.data.network.dto.response.RoomMemberDetailResponse
import team.mino.core.data.network.dto.response.RoomResponse
import team.mino.core.data.network.dto.response.RoomSummaryResponse
import team.mino.core.data.network.service.RoomApiService
import javax.inject.Inject

/**
 * [RoomRemoteDataSource]의 실서버 구현. 계약은
 * `docs/specs/group-room-form/contracts/room-api.md` §4가 소유한다.
 *
 * 네 함수 모두 [RoomApiService]에 위임만 한다 — 봉투 해제는 서비스가, 도메인 변환은 Repository가 하므로
 * 이 클래스에는 변환도 비즈니스 로직도 없다(`core/data/README.md` §5).
 */
internal class RoomRemoteDataSourceImpl @Inject constructor(
    private val service: RoomApiService,
) : RoomRemoteDataSource {
    override suspend fun listRooms(): List<RoomSummaryResponse> = service.listRooms()

    override suspend fun getRoom(roomId: String): RoomResponse = service.getRoom(roomId)

    override suspend fun createRoom(request: RoomRequest): RoomResponse = service.createRoom(request)

    override suspend fun updateRoom(
        roomId: String,
        request: RoomRequest,
    ): RoomResponse = service.updateRoom(roomId, request)

    override suspend fun getMembers(roomId: String): List<RoomMemberDetailResponse> = service.getMembers(roomId)

    override suspend fun createInvitation(roomId: String): RoomInvitationResponse = service.createInvitation(roomId)

    override suspend fun leaveRoom(roomId: String) {
        service.leaveRoom(roomId)
    }

    override suspend fun transferOwner(
        roomId: String,
        nextOwnerId: String,
    ) {
        service.transferOwner(roomId, nextOwnerId)
    }
}
