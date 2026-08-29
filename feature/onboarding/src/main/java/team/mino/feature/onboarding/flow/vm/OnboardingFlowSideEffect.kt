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
}
