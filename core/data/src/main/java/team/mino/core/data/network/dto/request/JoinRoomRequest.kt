package team.mino.core.data.network.dto.request

import kotlinx.serialization.Serializable

/** 초대 코드로 방에 참여하는 요청. `POST /api/v1/rooms/{roomId}/members`. */
@Serializable
internal data class JoinRoomRequest(
    val inviteCode: String,
)
