package team.mino.core.domain.invite

/**
 * 초대 코드를 초대 링크 문자열로 조립하는 계약.
 *
 * 이 계층은 **"코드로부터 링크가 만들어진다"는 사실만 안다.** 호스트도 경로도 모른다 —
 * 그것을 아는 구현은 `:core:data`가 갖는다
 * (`docs/adr/2026-08-24-invite-link-assembly-domain-interface.md`).
 */
interface InviteLinkBuilder {
    /**
     * [inviteCode]를 공유 가능한 초대 링크 문자열로 만든다.
     *
     * 조립 형식이 바뀌면 고칠 자리는 구현 한 파일이다. 화면이 문자열을 이어 붙여 링크를 만들지 않는다.
     */
    fun build(inviteCode: String): String
}
