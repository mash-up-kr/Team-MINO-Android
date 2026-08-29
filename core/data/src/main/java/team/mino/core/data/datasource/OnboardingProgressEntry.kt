package team.mino.core.data.datasource

/**
 * 온보딩 진행 상태의 로컬 표현. DataStore에 담긴 세 값을 원시 타입 그대로 든다.
 *
 * [lastStepName]은 저장된 문자열이지 스텝 타입이 아니다 — 저장 값이 낡거나 손상돼 어느 스텝도
 * 가리키지 못할 수 있고, 그때의 폴백은 데이터 출처가 아니라 도메인 경계인
 * `OnboardingProgressRepositoryImpl`이 정한다(`docs/specs/onboarding-flow/data-model.md` §4.1).
 * 저장된 적이 없으면 [lastStepName]·[createdRoomId]는 `null`이다.
 */
internal data class OnboardingProgressEntry(
    val lastStepName: String?,
    val createdRoomId: String?,
    val isCompleted: Boolean,
)
