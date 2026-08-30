package team.mino.core.data.invite

import team.mino.core.domain.invite.InviteLinkBuilder
import javax.inject.Inject

/**
 * 호스트와 경로 형식을 아는 **유일한 자리**다
 * (`docs/adr/2026-08-24-invite-link-assembly-domain-interface.md`).
 *
 * **전 flavor(dev·qa·prod)가 같은 프로덕션 호스트를 쓴다고 가정한다** — flavor 분기도 `BuildConfig` 참조도 두지 않는다.
 * 서버 API 문서가 발급 엔드포인트 설명에 `gguk.org/r/{code}`를 적은 것이 현재 알려진 전부이고,
 * dev·qa 호스트가 다른지는 **확인되지 않았다**(`docs/specs/onboarding-flow/research.md` R-021 · 협의 항목 S-1).
 *
 * 가정이 깨지면 그 빌드에서 만들어진 초대 링크는 열리지 않는다. QA에서 초대 링크로 방에 들어가는 시나리오가
 * 실패하면 이 가정부터 의심하고, **고칠 자리는 이 파일 하나다** — 여기에 flavor 분기를 넣는 것으로 끝나며
 * 도메인·화면·계약은 바뀌지 않는다.
 */
internal class InviteLinkBuilderImpl @Inject constructor() : InviteLinkBuilder {
    override fun build(inviteCode: String): String = "$INVITE_LINK_BASE_URL$inviteCode"

    private companion object {
        const val INVITE_LINK_BASE_URL = "https://gguk.org/r/"
    }
}
