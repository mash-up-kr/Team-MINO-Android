package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.RoomSummaryResponse

internal class FakeRoomRemoteDataSource : RoomRemoteDataSource {
    var rooms: List<RoomSummaryResponse> = emptyList()

    override suspend fun getRooms(): List<RoomSummaryResponse> = rooms
}
