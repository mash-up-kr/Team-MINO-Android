package team.mino.core.data.network.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import team.mino.core.data.network.dto.response.RoomSummaryResponse
import javax.inject.Inject

internal class RoomApiService @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun getRooms(): List<RoomSummaryResponse> = client.get("api/v1/rooms").body()
}
