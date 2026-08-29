package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 초대 발급 응답이 담는 초대 코드.
 *
 * [code]는 6자 대문자·숫자 문자열이다. 링크 조립은 이 값을 받은 도메인 위쪽이 하므로 여기에 호스트·경로가 없다
 * (`docs/adr/2026-08-24-invite-link-assembly-domain-interface.md`).
 *
 * 계약은 `docs/specs/onboarding-flow/contracts/invite-link.md` §1.1이 소유한다.
 */
@Serializable
internal data class InvitationResponse(
    val code: String,
)
