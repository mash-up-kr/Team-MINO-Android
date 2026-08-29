package team.mino.feature.onboarding.flow.vm

import team.mino.core.common.android.architecture.Intent

/**
 * 온보딩 스텝을 넘기는 신호. 모두 사용자 조작이나 위임 Activity의 결과 수신에서 온다.
 *
 * **시간 경과로 발화하는 값이 없다** — 저절로 넘어가는 스텝을 두지 않는다는 요구사항이 이 목록에
 * 타이머발 Intent가 없다는 것으로 표현된다(`contracts/onboarding-flow-ui.md` §2.2).
 *
 * 각 값은 현재 스텝이 전이 표(같은 계약 §2.4)의 왼쪽 칸과 맞을 때만 처리된다. 같은 버튼을 연달아
 * 눌러도 두 번째는 이미 바뀐 스텝과 맞지 않아 버려지므로, 화면 쪽에서 버튼을 잠그지 않는다.
 *
 * 프로필 스텝에는 건너뛰기가 없다 — 그래서 이 목록에도 그에 해당하는 값이 없다.
 */
internal sealed interface OnboardingFlowIntent : Intent {
    /** Activity 최초 생성 1회. 저장된 진행 상태를 읽어 재개할 스텝을 정한다. */
    data object Start : OnboardingFlowIntent

    /** 프로필 Activity가 `RESULT_OK`로 끝났다. */
    data object ProfileSaved : OnboardingFlowIntent

    /** 공동방 폼이 방을 만들고 끝났다. */
    data class RoomCreated(
        val roomId: String,
    ) : OnboardingFlowIntent

    /** 공동방 폼의 [건너뛰기]. 방을 만들지 않고 다음 스텝으로 간다. */
    data object RoomFormSkipped : OnboardingFlowIntent

    /** 공동방 폼이 `RESULT_CANCELED`로 끝났다. 스텝을 넘기지 않고 폼을 다시 연다. */
    data object RoomFormCanceled : OnboardingFlowIntent

    /** 친구 초대 화면 우상단 [X]. */
    data object InviteClosed : OnboardingFlowIntent

    /** 튜토리얼의 [건너뛰기] 또는 마지막 스텝의 CTA. 둘을 구분하지 않는다. */
    data object TutorialFinished : OnboardingFlowIntent
}
