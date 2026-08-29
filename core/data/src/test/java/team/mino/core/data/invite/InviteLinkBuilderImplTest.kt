package team.mino.core.data.invite

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 호스트·경로를 아는 유일한 자리가 이 구현이므로, 형식이 바뀌었는지는 여기서만 관찰된다.
 *
 * flavor별 분기를 두지 않는 것은 의도다 — 전 flavor가 같은 프로덕션 호스트를 쓴다는 가정 위에 있고,
 * 그 가정이 깨지면 고칠 자리도 이 한 파일이다
 * (`docs/specs/onboarding-flow/contracts/invite-link.md` §4 · research.md R-021).
 */
class InviteLinkBuilderImplTest {
    private val builder = InviteLinkBuilderImpl()

    @Test
    fun `초대 코드로 gguk 호스트의 r 경로 링크를 만든다`() {
        assertEquals("https://gguk.org/r/K7Q2MZ", builder.build(inviteCode = "K7Q2MZ"))
    }
}
