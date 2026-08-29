package team.mino.core.domain.repository

import team.mino.core.domain.model.OnboardingProgress
import team.mino.core.domain.model.OnboardingStep

/**
 * 온보딩 진행 상태의 저장·조회 계약. **온보딩 feature만 쓰는 계약이 아니다** — 완료 표시는 스플래시가 읽는다.
 *
 * `Flow`를 흘리지 않는다 — 진행 상태를 관찰해야 하는 화면이 없고, 읽는 시점은 온보딩 진입과
 * 스플래시 분기 두 번뿐이다. 실패를 `Result`로 감싸지도 않는다 — 실패는 던진다.
 *
 * 다만 이 계약의 실패는 `MinoDomainException`으로 매핑되는 예상 가능한 실패가 **아니다.**
 * 로컬 저장 실패(디스크 I/O·직렬화 오류)는 버그이며, `docs/conventions/error_handling.md` §3의
 * 화이트리스트 밖이라 그대로 rethrow되어 CEH로 간다. 이 계약은 그것을 도메인 예외로 위장하지 않는다.
 * 취소는 그대로 전파한다.
 *
 * 세 쓰기를 한 함수로 합치지 않는다. 서로 다른 사건이고, 합치면 호출자가 바뀌지 않은 나머지 필드를
 * 매번 실어 보내야 해서 덮어쓰기 사고가 생긴다.
 */
interface OnboardingProgressRepository {
    /**
     * 저장된 진행 상태를 한 번 조회한다. 저장된 값이 없는 키는 [OnboardingProgress]의 기본값으로 채워진다.
     *
     * **열어야 할 스텝을 판정하지 않는다** — 저장된 값을 그대로 조립해 돌려주고, 어긋난 조합을
     * 떨어뜨리는 판정은 `ResolveOnboardingStepUseCase`가 소유한다.
     */
    suspend fun getProgress(): OnboardingProgress

    /**
     * 마지막으로 머무른 스텝을 [step]으로 기록한다. **새 스텝을 열기 전에** 불린다 —
     * 호출 순서와 그 이유는 `docs/specs/onboarding-flow/contracts/onboarding-flow-ui.md`가 소유한다.
     */
    suspend fun setCurrentStep(step: OnboardingStep)

    /**
     * 온보딩에서 만든 공동방의 id를 기록한다.
     */
    suspend fun setCreatedRoomId(roomId: String)

    /**
     * 온보딩 완료를 기록한다. 되돌리는 함수는 두지 않는다.
     */
    suspend fun markCompleted()
}
