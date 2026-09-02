package team.mino.core.data.network.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import team.mino.core.data.network.dto.response.MinoResponse
import team.mino.core.data.network.dto.response.PinResponse
import javax.inject.Inject

/**
 * 방에 저장된 핀(장소) 목록 조회 API — `docs/specs/room-detail/contracts/place-repository.md` 근거.
 *
 * 응답은 공통 인터셉터 없이 `{ "data": ... }` 봉투를 그대로 받으므로, [RoomApiService]와 같이
 * 이 서비스가 직접 벗긴다.
 */
internal class PlaceApiService @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun getPins(roomId: String): List<PinResponse> =
        client
            .get("api/v1/pins") {
                parameter("roomId", roomId)
            }.body<MinoResponse<List<PinResponse>>>()
            .data
}
