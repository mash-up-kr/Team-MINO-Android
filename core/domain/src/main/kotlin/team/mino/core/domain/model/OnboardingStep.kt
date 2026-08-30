package team.mino.core.domain.model

/**
 * 온보딩에서 머무를 수 있는 스텝. 선언 순서가 곧 진행 순서다.
 *
 * 다만 `ROOM_FORM`에서 [TUTORIAL]로 건너뛰는 경로가 있어 "다음 값"이 순서의 소유자가 아니다 —
 * 전이 표는 `docs/specs/onboarding-flow/contracts/onboarding-flow-ui.md`가 소유한다.
 *
 * **완료를 값으로 두지 않는다.** 완료는 스텝이 아니라 [OnboardingProgress.isCompleted]가 든다 —
 * 완료된 설치에는 머무르는 스텝이 없기 때문이다.
 *
 * 튜토리얼 **내부** 스텝(1~5)은 이 enum에 없다. 복원 대상이 아니라 항상 첫 스텝부터 시작한다.
 */
enum class OnboardingStep {
    PROFILE,
    ROOM_FORM,
    INVITE,
    TUTORIAL,
}
