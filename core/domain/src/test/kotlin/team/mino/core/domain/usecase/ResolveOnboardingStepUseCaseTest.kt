package team.mino.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import team.mino.core.domain.model.OnboardingProgress
import team.mino.core.domain.model.OnboardingStep

/**
 * 저장된 진행 상태에서 **열어야 할 스텝**을 계산하는 규칙을 본다
 * (`contracts/onboarding-progress.md` §3의 다섯 갈래).
 *
 * 판정하는 것은 **[OnboardingProgress] → [OnboardingStep]** 뿐이다. 조회는 호출자가 하므로 Repository를 세우지
 * 않는다 — 이 함수는 Repository를 주입받지 않는 순수 함수이고 `suspend`도 아니다(§3).
 *
 * [OnboardingProgress.isCompleted]는 넣지 않는다. 완료된 설치는 온보딩을 열지 않아 이 함수에 도달하지 않으며,
 * 그 판정의 소유자는 `ResolveSplashEntryUseCase`다(§3·§4). 여기서 `isCompleted`를 가르는 테스트를 두면
 * 이 함수가 보지 않기로 한 값을 보게 만든다.
 */
class ResolveOnboardingStepUseCaseTest {
    private val resolveOnboardingStep = ResolveOnboardingStepUseCase()

    @Test
    fun `프로필에서 중단했으면 프로필을 연다`() {
        // FR-001
        val progress = OnboardingProgress(lastStep = OnboardingStep.PROFILE)

        assertEquals(OnboardingStep.PROFILE, resolveOnboardingStep(progress))
    }

    @Test
    fun `공동방 폼에서 중단했으면 공동방 폼을 연다`() {
        // FR-023 · TS-037
        val progress = OnboardingProgress(lastStep = OnboardingStep.ROOM_FORM)

        assertEquals(OnboardingStep.ROOM_FORM, resolveOnboardingStep(progress))
    }

    @Test
    fun `친구 초대에서 중단했고 만든 방이 있으면 친구 초대를 연다`() {
        // FR-023 · EC-021 — 초대 링크를 저장된 id로 다시 확보할 수 있다.
        val progress =
            OnboardingProgress(
                lastStep = OnboardingStep.INVITE,
                createdRoomId = "room-1",
            )

        assertEquals(OnboardingStep.INVITE, resolveOnboardingStep(progress))
    }

    @Test
    fun `친구 초대에서 중단했어도 만든 방이 없으면 튜토리얼로 떨어진다`() {
        // FR-004 · SC-004 — 방이 없으면 초대할 대상도 없다. 저장 값이 손상되거나 스텝 구성이 바뀌면
        // 만들어질 수 있는 조합이며, 이 방어 규칙이 이 판정을 UseCase로 세운 이유다(§3).
        val progress =
            OnboardingProgress(
                lastStep = OnboardingStep.INVITE,
                createdRoomId = null,
            )

        assertEquals(OnboardingStep.TUTORIAL, resolveOnboardingStep(progress))
    }

    @Test
    fun `튜토리얼에서 중단했으면 튜토리얼을 연다`() {
        // FR-023 · EC-022 — 튜토리얼 내부 위치는 복원하지 않으므로 항상 스텝 1부터다.
        val progress = OnboardingProgress(lastStep = OnboardingStep.TUTORIAL)

        assertEquals(OnboardingStep.TUTORIAL, resolveOnboardingStep(progress))
    }
}
