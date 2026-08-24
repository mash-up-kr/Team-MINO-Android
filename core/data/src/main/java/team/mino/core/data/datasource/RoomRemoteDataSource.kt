package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.RoomSummaryResponse

internal interface RoomRemoteDataSource {
    suspend fun getRooms(): List<RoomSummaryResponse>
}
