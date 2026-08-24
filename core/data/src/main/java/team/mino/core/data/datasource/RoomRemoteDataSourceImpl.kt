package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.RoomSummaryResponse
import team.mino.core.data.network.service.RoomApiService
import javax.inject.Inject

internal class RoomRemoteDataSourceImpl @Inject constructor(
    private val service: RoomApiService,
) : RoomRemoteDataSource {
    override suspend fun getRooms(): List<RoomSummaryResponse> = service.getRooms()
}
