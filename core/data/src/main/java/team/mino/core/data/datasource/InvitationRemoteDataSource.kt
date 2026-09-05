package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.InvitationPreviewResponse
import team.mino.core.data.network.dto.response.InvitationResponse

/**
 * 초대의 원격 출처. 계약은 `docs/specs/onboarding-flow/contracts/invite-link.md` §2가 소유한다.
 *
 * 같은 `room` 리소스를 다루는 [RoomRemoteDataSource]와 나뉘어 있는 것은 의도다 — 초대는 방과 다른
 * 서버 태그이자 다른 관심사다(`docs/adr/2026-08-28-api-service-owned-per-server-tag.md`).
 */
internal interface InvitationRemoteDataSource {
    /**
     * [roomId] 방의 초대 코드를 발급받는다. 멤버당 초대가 하나이고 만료가 없어 서버가 멱등을 보장하므로
     * 이 출처는 코드를 캐시하지 않는다(계약 §1.1).
     *
     * 네트워크·서버 오류와 `401`·`403`·`404`는 `MinoDomainException`으로 전파된다.
     */
    suspend fun issueInvitation(roomId: String): InvitationResponse

    /**
     * [code]가 가리키는 방을 참여 전에 미리 들여다본다.
     *
     * 없는 코드(`404`)를 포함한 실패는 `MinoDomainException`으로 전파된다.
     */
    suspend fun previewInvitation(code: String): InvitationPreviewResponse
}
