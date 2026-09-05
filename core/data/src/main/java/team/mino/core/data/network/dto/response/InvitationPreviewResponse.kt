package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 초대 코드 미리보기 응답. `GET /api/v1/invitations/{code}`.
 *
 * 지금 필요한 건 참여 API 호출에 쓸 방 id뿐이다 — 초대자·방 이름 등 나머지 필드는 도메인에 없어
 * `ignoreUnknownKeys = true`가 흡수하도록 여기 담지 않는다.
 */
@Serializable
internal data class InvitationPreviewResponse(
    val room: RoomIdOnly,
) {
    @Serializable
    internal data class RoomIdOnly(
        val id: String,
    )
}
