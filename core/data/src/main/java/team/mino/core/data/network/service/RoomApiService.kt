package team.mino.core.data.network.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import team.mino.core.data.network.dto.request.TransferOwnerRequest
import team.mino.core.data.network.dto.response.RoomInvitationResponse
import team.mino.core.data.network.dto.response.RoomMemberDetailResponse
import team.mino.core.data.network.dto.response.RoomSummaryResponse
import javax.inject.Inject

internal class RoomApiService @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun getRooms(): List<RoomSummaryResponse> = client.get("api/v1/rooms").body()

    /** `GET /api/v1/rooms/{roomId}/members`. */
    suspend fun getMembers(roomId: String): List<RoomMemberDetailResponse> =
        client.get("api/v1/rooms/$roomId/members").body()

    /** `POST /api/v1/rooms/{roomId}/invitations`. 개인방이면 서버가 `403 PERSONAL_ROOM_NOT_ALLOWED`. */
    suspend fun createInvitation(roomId: String): RoomInvitationResponse =
        client.post("api/v1/rooms/$roomId/invitations").body()

    /** `DELETE /api/v1/rooms/{roomId}/members/me`. 방장이 다른 멤버와 함께 호출하면 `409 OWNER_TRANSFER_REQUIRED`. */
    suspend fun leaveRoom(roomId: String) {
        client.delete("api/v1/rooms/$roomId/members/me")
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
