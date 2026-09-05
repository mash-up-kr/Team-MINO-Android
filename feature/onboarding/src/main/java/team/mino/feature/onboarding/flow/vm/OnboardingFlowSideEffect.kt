package team.mino.feature.onboarding.flow.vm

import team.mino.core.common.android.architecture.SideEffect

/**
 * 스텝 전환 지시. 이 스트림의 소비자는 셸 하나다 — 다른 Activity로의 위임과 그래프 이동이 섞여
 * 있는데 채널은 소비자가 하나뿐이라, 수집기를 나누면 한쪽이 이벤트를 가져가 버린다. 셸이 하나로
 * 받아 그래프 이동은 직접 실행하고 Activity가 해야 하는 것만 콜백으로 올린다
 * (`feature-navigation.md` 1장,
 * `contracts/onboarding-flow-ui.md` §2.3).
 */
internal sealed interface OnboardingFlowSideEffect : SideEffect {
    /** 프로필 Activity를 결과 받는 형태로 연다. */
    data object LaunchProfile : OnboardingFlowSideEffect

    /** 공동방 폼 Activity를 결과 받는 형태로 연다. */
    data object LaunchRoomForm : OnboardingFlowSideEffect

    /** 온보딩 그래프의 친구 초대 화면으로 이동한다. */
    data class NavigateToInvite(
        val roomId: String,
    ) : OnboardingFlowSideEffect

    /** 온보딩 그래프의 튜토리얼 화면으로 이동한다. */
    data object NavigateToTutorial : OnboardingFlowSideEffect

    /** 온보딩을 끝내고 메인으로 넘긴다. */
    data object NavigateToHome : OnboardingFlowSideEffect

    /**
     * 튜토리얼을 마쳤고, 그 온보딩이 초대 딥링크로 자동 참여까지 끝난 것이었다(PRD SYS-010 Flow A,
     * 신규 유저). 공동방 생성 유도·친구 초대 스텝은 건너뛰었지만 튜토리얼은 거쳤고, 끝나는 시점에
     * 평소 홈이 아니라 참여한 그 방으로 바로 들어간다.
     */
    data class NavigateToHomeWithRoom(val roomId: String) : OnboardingFlowSideEffect
}
