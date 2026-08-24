package team.mino.core.data.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/rooms`(`RoomSummary`) 응답 DTO.
 *
 * 백엔드 draft OpenAPI(`Team-MINO-Node`, `KKardy/GM-111-outline-prd` 브랜치, `info.version: 0.1.0-draft`)
 * 기준 필드만 표현한다 — 근거: docs/specs/room-list/research.md D12.
 * spec이 요구하는 썸네일 콜라주·아바타 URL·최근 저장일·코멘트 수는 draft에 없어 이 DTO에 포함하지 않고,
 * `repository/mapper/RoomMapper.kt`에서 임시 목데이터/플레이스홀더로 채운다.
 */
@Serializable
data class RoomSummaryResponse(
    val id: String,
    val type: String,
    val name: String,
    val description: String?,
    val color: String?,
    @SerialName("ownerId") val ownerId: String,
    @SerialName("inviteCode") val inviteCode: String,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("pinCount") val pinCount: Int,
    @SerialName("memberCount") val memberCount: Int,
    @SerialName("hasPlace") val hasPlace: Boolean? = null,
    val users: List<RoomMemberResponse>? = null,
)

@Serializable
data class RoomMemberResponse(
    val id: String,
    val avatar: RoomMemberAvatarResponse? = null,
)

@Serializable
data class RoomMemberAvatarResponse(
    val id: Int,
)
