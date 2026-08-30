package team.mino.core.domain.repository

/**
 * 방의 초대 코드 발급 계약.
 *
 * [RoomRepository]에 얹지 않는다 — 도메인 Repository의 단위는 서버 태그가 아니라 관심사이고,
 * "초대 코드 발급"은 방의 목록·조회·생성·수정과 다른 관심사다
 * (`docs/adr/2026-08-28-api-service-owned-per-server-tag.md`).
 */
interface RoomInvitationRepository {
    /**
     * [roomId] 방의 초대 코드를 발급받아 돌려준다. **링크가 아니라 코드다** — 링크로의 조립은
     * [team.mino.core.domain.invite.InviteLinkBuilder]가 한다.
     *
     * 멤버당 초대는 하나이고 만료가 없다. 이미 발급한 코드가 있으면 서버가 같은 값을 돌려주므로
     * 이 계약은 코드를 캐시하지 않는다 — 재개 경로에서 다시 불러도 같은 링크가 된다.
     *
     * 실패는 `MinoDomainException`으로 던지고 취소는 그대로 전파한다. `null`이나 빈 문자열로 뭉개지 않는다.
     * 서버가 내려주는 `errorCode`를 도메인 리프로 세우지 않는다
     * (`docs/adr/2026-08-28-error-body-type-and-no-error-code-leaf.md`).
     */
    suspend fun issueInviteCode(roomId: String): String
}
