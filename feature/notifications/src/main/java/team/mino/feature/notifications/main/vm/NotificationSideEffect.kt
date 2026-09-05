package team.mino.feature.notifications.main.vm

import team.mino.core.common.android.architecture.SideEffect

/**
 * 알림 탭이 한 번만 흘려보내는 신호 —
 * `docs/specs/notifications/contracts/notification-ui.md` §3이 목록을 소유한다.
 *
 * 목록 갱신·오류 표시·추가 로드처럼 모듈 안에서 끝나는 상태 변화는 여기 오지 않는다. 전부
 * `NotificationUiState`다. 목록 조회 실패도 마찬가지로 `DomainErrorEmitter`를 `NotificationRoute`가
 * 수집한다(`docs/conventions/error_handling.md` §5·§6).
 */
internal sealed interface NotificationSideEffect : SideEffect {
    /**
     * 장소 상세로 넘어간다. 목적지가 저장 탭 안의 화면이라 `notificationGraph`의 콜백으로 올린다 —
     * 요청 홀더도 탭 목록도 셸의 것이다(`research.md` D14).
     */
    data class NavigateToPlaceDetail(
        val pinId: String,
    ) : NotificationSideEffect

    /** 방 상세로 넘어간다. [NavigateToPlaceDetail]과 같은 경로로 셸이 받는다(`research.md` D10). */
    data class NavigateToRoomDetail(
        val roomId: String,
    ) : NotificationSideEffect

    /**
     * 저장 오류 안내 화면으로 넘어간다. 같은 그래프 안의 전환이지만 `NavController` 조작이라
     * Route가 해야 한다 — ViewModel은 `NavController`를 알지 않는다(`research.md` D2).
     */
    data object NavigateToSaveErrorGuide : NotificationSideEffect

    /** 안내 화면에서 목록으로 되돌린다(spec FR-011). */
    data object NavigateBack : NotificationSideEffect
}
