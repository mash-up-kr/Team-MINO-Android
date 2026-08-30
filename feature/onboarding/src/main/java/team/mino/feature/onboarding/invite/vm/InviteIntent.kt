package team.mino.feature.onboarding.invite.vm

import team.mino.core.common.android.architecture.Intent

/**
 * 친구 초대 스텝에서 사용자가 일으키는 일(`contracts/onboarding-flow-ui.md` §3.3).
 *
 * **우상단 [X]가 여기에 없는 것은 의도다.** 스텝을 넘기는 조작이라 이 화면이 아니라 플로우 ViewModel이
 * 소유하고, Route가 `onClose` 콜백으로 올려보낸다(같은 계약 §2.2 `InviteClosed`).
 *
 * [ShareLink]와 [CopyLink]는 서로에게 아무 조건도 걸지 않는다 — 복사한 뒤에 이어서 공유하거나 연달아
 * 복사하는 경로가 그것으로 성립한다. 둘 다 스텝을 넘기지 않는다.
 */
internal sealed interface InviteIntent : Intent {
    /** 화면 최초 진입 1회. 초대 링크를 확보한다. */
    data object Load : InviteIntent

    /** [친구 초대하기]. 링크가 없어도 눌린다. */
    data object ShareLink : InviteIntent

    /** [초대 링크 복사]. 링크가 없어도 눌린다. */
    data object CopyLink : InviteIntent
}
