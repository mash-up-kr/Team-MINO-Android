package team.mino.feature.onboarding.flow.vm

import team.mino.core.common.android.architecture.UiState
import team.mino.core.domain.model.OnboardingStep

/**
 * 온보딩 플로우의 상태. 스텝 전이의 기준이 되는 [step]을 든다.
 *
 * **진행률을 든 필드가 없다** — 남은 스텝 수도 전체 스텝 수도 두지 않는 것이 온보딩에 진행 표시를
 * 두지 않는다는 요구사항의 표현이다(`contracts/onboarding-flow-ui.md` §2.1·§5).
 *
 * @property isLoading 저장된 진행 상태를 읽는 동안 참. 계약 §2.1이 요구한 필드이지만 **아직 그리는
 *  화면이 없다** — 첫 스텝은 다른 Activity에 위임되고 그 사이 릴레이 화면이 배경만 그린다. 화면 상태를
 *  sealed로 가르지 않고 필드로 두는 이유는 `docs/adr/2026-07-25-uistate-isloading-over-sealed-status.md`.
 * @property step 지금 머무르는 스텝. 들어온 Intent를 처리할지는 이 값이 가른다(같은 계약 §2.4).
 * @property createdRoomId 이 온보딩에서 만든 공동방. 공동방 폼을 건너뛰었으면 끝까지 `null`이다.
 * @property invitedRoomId 초대 딥링크(SYS-010)로 자동 참여까지 끝난 방. 튜토리얼을 마쳤을 때
 *  [OnboardingFlowSideEffect.NavigateToHomeWithRoom]으로 보낼지 [OnboardingFlowSideEffect.NavigateToHome]
 *  으로 보낼지를 이 값이 가른다. 초대로 들어오지 않았거나 참여에 실패했으면 끝까지 `null`이다.
 */
internal data class OnboardingFlowUiState(
    val isLoading: Boolean = true,
    val step: OnboardingStep = OnboardingStep.PROFILE,
    val createdRoomId: String? = null,
    val invitedRoomId: String? = null,
) : UiState
