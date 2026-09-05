package team.mino.feature.notifications.main.vm

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.common.android.architecture.UiState
import team.mino.feature.notifications.main.model.NotificationItemUiModel

/**
 * 알림 목록 화면의 상태(`docs/specs/notifications/data-model.md` §2.3,
 * `docs/specs/notifications/contracts/notification-ui.md` §4.2).
 *
 * **읽음 여부·권한 상태·배너 노출을 담지 않는다.** spec SC-009가 화면 상태를 목록·빈 상태·오류 셋으로 못
 * 박았고 FR-016·FR-017이 나머지를 금지한다 — 목록은 화면 최상단부터 시작하며 배너 자리를 두지 않는다.
 */
internal data class NotificationUiState(
    /**
     * 지금까지 이어 붙인 전체. **어느 경로에서도 줄어들지 않는다**(spec UX-012).
     *
     * `PersistentList`로 좁힌 것은 묶음을 이을 때 앞의 항목을 다시 복사하지 않기 위해서다 — `ImmutableList`로
     * 두면 `+`가 stdlib의 전량 복사로 해석돼 묶음마다 누적 길이만큼 복사가 일어난다.
     */
    val items: PersistentList<NotificationItemUiModel> = persistentListOf(),
    val phase: NotificationPhase = NotificationPhase.Loading,
    /** 다음 묶음을 받는 중. 목록 끝의 진행 표시이자 중복 요청을 막는 값이다(spec EC-018). */
    val isAppending: Boolean = false,
    /**
     * 추가 로드가 실패해 목록 끝에 재시도 표시를 그려야 하는지(spec UX-012·EC-016).
     *
     * 이 실패는 [phase]를 건드리지 않는다 — 이미 그린 목록을 오류 화면으로 덮으면 사용자가 보고 있던 것이
     * 사라진다.
     */
    val appendError: Boolean = false,
    /**
     * 더 받을 것이 남았는가(spec EC-018). 첫 조회 전에는 `false`라 목록 끝 감지가 헛돌지 않는다.
     */
    val hasNext: Boolean = false,
) : UiState

/**
 * 목록 전체가 지금 어느 얼굴을 하고 있는지.
 *
 * [Loading]과 [Empty]를 가르는 것이 요구다 — 조회가 끝나 0건임이 확정되기 전에는 빈 상태 문구를 그리면 안
 * 된다(spec UX-001).
 */
internal enum class NotificationPhase {
    /** 첫 묶음을 받는 중. 빈 상태 문구를 그리지 않는다. */
    Loading,

    /** 그릴 항목이 있다. 추가 로드의 진행·실패는 이 상태 안에서 표시된다. */
    Content,

    /** 조회가 끝났고 받은 것이 0건이며 더 받을 것도 없다(spec FR-006·UX-001). */
    Empty,

    /** **첫 페이지 실패 전용**이다(spec UX-002·EC-001). 추가 로드 실패는 `appendError`가 든다. */
    Error,
}
