package team.mino.feature.onboarding.invite.vm

import team.mino.core.common.android.architecture.UiState

/**
 * 친구 초대 스텝의 상태(`contracts/onboarding-flow-ui.md` §3.2).
 *
 * **두 액션의 활성 여부를 든 필드가 없다** — 링크를 확보하지 못해도 [친구 초대하기]·[초대 링크 복사]는
 * 언제나 누를 수 있고, 눌린 뒤에 실패를 알린다(같은 계약 §3.4). 링크 유무로 버튼을 잠그는 파생 값을
 * 여기에 두면 그 요구사항이 뒤집힌다.
 *
 * 진행률·참여자 목록을 든 필드도 없다. 이 화면은 몇 번째 스텝인지도, 누가 들어와 있는지도 보여주지 않는다.
 *
 * 진입 인자 `roomId`는 상태에 두지 않는다 — 화면이 읽지 않고 ViewModel만 쓰는 값이다.
 *
 * @property isLoading 링크를 확보하는 동안 참. 화면 상태를 sealed로 가르지 않고 필드로 두는 이유는
 *  `docs/adr/2026-07-25-uistate-isloading-over-sealed-status.md`.
 * @property inviteLink 확보한 초대 링크. `null`이면 아직 확보 전이거나 확보에 실패한 것이고, 둘 중
 *  어느 쪽이어도 화면은 같다 — 실패해도 오류 화면으로 갈아 끼우지 않는다.
 */
internal data class InviteUiState(
    val isLoading: Boolean = true,
    val inviteLink: String? = null,
) : UiState
