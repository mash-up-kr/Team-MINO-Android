package team.mino.core.data.network.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import team.mino.core.data.network.dto.request.RoomRequest
import team.mino.core.data.network.dto.response.MinoResponse
import team.mino.core.data.network.dto.response.RoomResponse
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

    /**
     * 방 하나를 조회한다. 계약은 `docs/specs/group-room-form/contracts/room-api.md` §1이 소유한다.
     *
     * 서버가 `pinCount`·`memberCount`를 더 내려주지만 [RoomResponse]에 두지 않는다 —
     * `ignoreUnknownKeys = true`가 흡수한다.
     */
    suspend fun getRoom(roomId: String): RoomResponse =
        client
            .get("api/v1/rooms/$roomId")
            .body<MinoResponse<RoomResponse>>()
            .data

    /**
     * 방을 만든다. 생성자가 방장이 되는 것은 서버가 보장하므로 요청에 방장 정보를 싣지 않는다
     * (`docs/specs/group-room-form/contracts/room-api.md` §1).
     */
    suspend fun createRoom(request: RoomRequest): RoomResponse =
        client
            .post("api/v1/rooms") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<MinoResponse<RoomResponse>>()
            .data

    /**
     * 방을 수정한다. 서버의 `required: []`를 부분 전송으로 쓰지 않고 폼의 세 값을 항상 함께 보낸다 —
     * 근거는 `docs/specs/group-room-form/contracts/room-api.md` §5.
     */
    suspend fun updateRoom(
        roomId: String,
        request: RoomRequest,
    ): RoomResponse =
        client
            .patch("api/v1/rooms/$roomId") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<MinoResponse<RoomResponse>>()
            .data
}
