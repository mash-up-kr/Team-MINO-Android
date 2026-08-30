package team.mino.core.domain.usecase

import team.mino.core.domain.invite.InviteLinkBuilder
import team.mino.core.domain.repository.RoomInvitationRepository
import javax.inject.Inject

/**
 * 방의 초대 코드를 발급받아 공유 가능한 초대 링크로 조립한다
 * (`docs/specs/onboarding-flow/contracts/invite-link.md` §3).
 *
 * **캐시하지 않는다**(EC-012) — 부를 때마다 발급을 요청한다. 서버가 멱등을 보장해 이미 발급한 방이면
 * 같은 코드가 오므로, 재개 경로에서 다시 불러도 같은 링크가 된다. 한 번 받은 값을 들고 있는 것은 화면의 몫이다.
 *
 * 발급 실패는 **잡지 않고 그대로 전파한다.** `null`이나 빈 문자열로 뭉개면 화면이 링크를 확보한 것으로 오판해
 * 빈 링크를 공유·복사한다(EC-008). 코드를 받지 못하면 조립도 하지 않는다.
 */
class GetInviteLinkUseCase @Inject constructor(
    private val roomInvitationRepository: RoomInvitationRepository,
    private val inviteLinkBuilder: InviteLinkBuilder,
) {
    suspend operator fun invoke(roomId: String): String =
        inviteLinkBuilder.build(roomInvitationRepository.issueInviteCode(roomId))
}
