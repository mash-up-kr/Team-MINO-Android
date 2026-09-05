package team.mino.core.data.datasource

import team.mino.core.data.network.dto.request.RoomRequest
import team.mino.core.data.network.dto.response.RoomInvitationResponse
import team.mino.core.data.network.dto.response.RoomMemberDetailResponse
import team.mino.core.data.network.dto.response.RoomResponse
import team.mino.core.data.network.dto.response.RoomSummaryResponse

internal class FakeRoomRemoteDataSource : RoomRemoteDataSource {
    var rooms: List<RoomSummaryResponse> = emptyList()
    var room: RoomResponse? = null

    /** [listRooms]에 마지막으로 넘어온 `showHasPlaceId`. 쿼리가 실제로 흘러왔는지 확인한다. */
    var lastShowHasPlaceId: String? = null
        private set

    override suspend fun listRooms(showHasPlaceId: String?): List<RoomSummaryResponse> {
        lastShowHasPlaceId = showHasPlaceId
        return rooms
    }

    override suspend fun getRoom(roomId: String): RoomResponse = checkNotNull(room) { "room을 채운 뒤 getRoom을 부른다." }

    override suspend fun createRoom(request: RoomRequest): RoomResponse =
        checkNotNull(room) { "room을 채운 뒤 createRoom을 부른다." }

    override suspend fun updateRoom(
        roomId: String,
        request: RoomRequest,
    ): RoomResponse = checkNotNull(room) { "room을 채운 뒤 updateRoom을 부른다." }

    override suspend fun getMembers(roomId: String): List<RoomMemberDetailResponse> =
        error("FakeRoomRemoteDataSource는 getMembers를 지원하지 않는다.")

    override suspend fun createInvitation(roomId: String): RoomInvitationResponse =
        error("FakeRoomRemoteDataSource는 createInvitation을 지원하지 않는다.")

    override suspend fun leaveRoom(roomId: String): Unit = error("FakeRoomRemoteDataSource는 leaveRoom을 지원하지 않는다.")

    override suspend fun transferOwner(
        roomId: String,
        nextOwnerId: String,
    ): Unit = error("FakeRoomRemoteDataSource는 transferOwner를 지원하지 않는다.")

    var joinRoomError: Throwable? = null

    /** [joinRoom]에 마지막으로 넘어온 `roomId`·`inviteCode`. */
    var lastJoinRoomId: String? = null
        private set
    var lastJoinInviteCode: String? = null
        private set

    override suspend fun joinRoom(
        roomId: String,
        inviteCode: String,
    ) {
        lastJoinRoomId = roomId
        lastJoinInviteCode = inviteCode
        joinRoomError?.let { throw it }
    }
}
