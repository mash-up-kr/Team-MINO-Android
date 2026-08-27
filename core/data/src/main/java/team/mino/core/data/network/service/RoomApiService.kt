package team.mino.core.data.network.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import team.mino.core.data.network.dto.response.MinoResponse
import team.mino.core.data.network.dto.response.RoomSummaryResponse
import javax.inject.Inject

/**
 * 방 엔드포인트를 호출하는 서비스.
 *
 * 신원 증명 헤더는 `MinoIdentityProofPlugin`이 싣고, 예외는 `convertDomainException`이
 * `MinoDomainException`으로 바꿔 던지므로 여기서 잡지 않는다.
 */
internal class RoomApiService @Inject constructor(
    private val client: HttpClient,
) {
    /**
     * 참여 중인 방 목록을 조회한다. 나간 방은 서버가 제외한다.
     *
     * `?showHasPlaceId=` · `?showUsers=true` 쿼리는 붙이지 않는다 —
     * 근거는 `docs/specs/shared-link-receiver/contracts/room-list-api.md` §3.
     */
    suspend fun listRooms(): List<RoomSummaryResponse> =
        client
            .get("api/v1/rooms")
            .body<MinoResponse<List<RoomSummaryResponse>>>()
            .data
}
