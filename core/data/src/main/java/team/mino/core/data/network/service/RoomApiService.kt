package team.mino.core.data.network.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import team.mino.core.data.network.dto.request.JoinRoomRequest
import team.mino.core.data.network.dto.request.RoomRequest
import team.mino.core.data.network.dto.request.TransferOwnerRequest
import team.mino.core.data.network.dto.response.MinoResponse
import team.mino.core.data.network.dto.response.RoomInvitationResponse
import team.mino.core.data.network.dto.response.RoomMemberDetailResponse
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
     * `?showUsers=true`는 붙이지 않는다 — 근거는
     * `docs/specs/shared-link-receiver/contracts/room-list-api.md` §3.
     *
     * @param showHasPlaceId 장소 UUID. 주면 각 방에 `hasPlace`·`matchedPinId`가 함께 온다
     *  (`docs/specs/place-detail/contracts/place-api.md` §4). `null`이면 `parameter`가 쿼리를 붙이지 않아
     *  기존 요청과 바이트 단위로 같다.
     */
    suspend fun listRooms(showHasPlaceId: String? = null): List<RoomSummaryResponse> =
        client
            .get("api/v1/rooms") {
                parameter("showHasPlaceId", showHasPlaceId)
            }.body<MinoResponse<List<RoomSummaryResponse>>>()
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

    /** `GET /api/v1/rooms/{roomId}/members`. */
    suspend fun getMembers(roomId: String): List<RoomMemberDetailResponse> =
        client
            .get("api/v1/rooms/$roomId/members")
            .body<MinoResponse<List<RoomMemberDetailResponse>>>()
            .data

    /** `POST /api/v1/rooms/{roomId}/invitations`. 개인방이면 서버가 `403 PERSONAL_ROOM_NOT_ALLOWED`. */
    suspend fun createInvitation(roomId: String): RoomInvitationResponse =
        client
            .post("api/v1/rooms/$roomId/invitations")
            .body<MinoResponse<RoomInvitationResponse>>()
            .data

    /** `DELETE /api/v1/rooms/{roomId}/members/me`. 방장이 다른 멤버와 함께 호출하면 `409 OWNER_TRANSFER_REQUIRED`. */
    suspend fun leaveRoom(roomId: String) {
        client.delete("api/v1/rooms/$roomId/members/me")
    }

    /**
     * `POST /api/v1/rooms/{roomId}/members`. 초대 코드로 방에 참여한다.
     *
     * 이미 멤버면 서버가 에러 대신 멱등하게 성공으로 응답한다. 응답 본문은 `{"ok":true}` 뿐이라
     * 읽지 않는다.
     */
    suspend fun joinRoom(
        roomId: String,
        inviteCode: String,
    ) {
        client.post("api/v1/rooms/$roomId/members") {
            contentType(ContentType.Application.Json)
            setBody(JoinRoomRequest(inviteCode = inviteCode))
        }
    }

    /** `PUT /api/v1/rooms/{roomId}/owner`. */
    suspend fun transferOwner(
        roomId: String,
        nextOwnerId: String,
    ) {
        client.put("api/v1/rooms/$roomId/owner") {
            contentType(ContentType.Application.Json)
            setBody(TransferOwnerRequest(nextOwnerId = nextOwnerId))
        }
    }
}
