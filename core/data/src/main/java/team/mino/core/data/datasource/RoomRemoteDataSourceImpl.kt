package team.mino.core.data.datasource

import team.mino.core.data.datasource.mock.RoomMockStore
import team.mino.core.data.network.dto.request.RoomRequest
import team.mino.core.data.network.dto.response.RoomInvitationResponse
import team.mino.core.data.network.dto.response.RoomMemberDetailResponse
import team.mino.core.data.network.dto.response.RoomResponse
import team.mino.core.data.network.dto.response.RoomSummaryResponse
import team.mino.core.data.network.service.RoomApiService
import javax.inject.Inject

/**
 * [RoomRemoteDataSource]의 유일한 구현. [getRooms]는 실서버(`RoomApiService`)를 부르고, 나머지 셋은
 * 서버가 아직 없어 mock([RoomMockStore])에 위임한다.
 *
 * 지연·식별자 생성·없는 방의 404는 모두 원천인 [RoomMockStore]가 갖는다. mock 쪽 메서드는 출처 호출만
 * 한다 — 실서버 구현이 `RoomApiService`에 위임만 하는 것과 같은 모양이어야, 전환 때 바뀌는 곳이
 * `docs/specs/group-room-form/contracts/room-api-mock.md` §4가 적은 세 곳으로 유지된다.
 */
internal class RoomRemoteDataSourceImpl @Inject constructor(
    private val service: RoomApiService,
    private val store: RoomMockStore,
) : RoomRemoteDataSource {
    override suspend fun getRooms(): List<RoomSummaryResponse> = service.getRooms()

    override suspend fun getRoom(roomId: String): RoomResponse = store.getRoom(roomId)

    override suspend fun createRoom(request: RoomRequest): RoomResponse = store.createRoom(request)

    override suspend fun updateRoom(
        roomId: String,
        request: RoomRequest,
    ): RoomResponse = store.updateRoom(roomId, request)

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
