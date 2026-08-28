package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.RoomSummaryResponse
import team.mino.core.data.network.service.RoomApiService
import javax.inject.Inject

internal class RoomListRemoteDataSourceImpl @Inject constructor(
    private val service: RoomApiService,
) : RoomListRemoteDataSource {
    override suspend fun listRooms(): List<RoomSummaryResponse> = service.listRooms()
}
