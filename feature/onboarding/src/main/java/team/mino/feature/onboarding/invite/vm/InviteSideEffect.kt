package team.mino.feature.onboarding.invite.vm

import team.mino.core.common.android.architecture.SideEffect

/**
 * 초대 링크를 화면 밖으로 내보내는 두 통로(`contracts/onboarding-flow-ui.md` §3.3).
 *
 * **어느 쪽에도 네비게이션이 없다** — 공유도 복사도 스텝을 넘기지 않는다는 요구사항이 그렇게 표현된다.
 *
 * 링크를 페이로드로 싣는 것은 실행 지점이 상태를 다시 읽지 않게 하기 위해서다. 실행 시점에 상태를
 * 읽으면 그 사이에 `null`이 된 링크로 빈 문자열을 공유·복사할 수 있다.
 */
internal sealed interface InviteSideEffect : SideEffect {
    /** 공유 시트를 연다. 외부 앱 전환이라 Activity가 실행하고, 결과는 읽지 않는다. */
    data class ShareInviteLink(
        val link: String,
    ) : InviteSideEffect

    /** 클립보드에 쓰고 복사 완료 토스트를 띄운다. 실행은 Route가 한다. */
    data class CopyInviteLink(
        val link: String,
    ) : InviteSideEffect
}
