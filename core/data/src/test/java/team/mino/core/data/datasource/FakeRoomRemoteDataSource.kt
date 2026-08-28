package team.mino.core.data.datasource

import team.mino.core.data.network.dto.request.RoomRequest
import team.mino.core.data.network.dto.response.RoomResponse
import team.mino.core.data.network.dto.response.RoomSummaryResponse

internal class FakeRoomRemoteDataSource : RoomRemoteDataSource {
    var rooms: List<RoomSummaryResponse> = emptyList()
    var room: RoomResponse? = null

    override suspend fun listRooms(): List<RoomSummaryResponse> = rooms

    override suspend fun getRoom(roomId: String): RoomResponse = checkNotNull(room) { "room을 채운 뒤 getRoom을 부른다." }

    override suspend fun createRoom(request: RoomRequest): RoomResponse =
        checkNotNull(room) { "room을 채운 뒤 createRoom을 부른다." }

    override suspend fun updateRoom(
        roomId: String,
        request: RoomRequest,
    ): RoomResponse = checkNotNull(room) { "room을 채운 뒤 updateRoom을 부른다." }
}
