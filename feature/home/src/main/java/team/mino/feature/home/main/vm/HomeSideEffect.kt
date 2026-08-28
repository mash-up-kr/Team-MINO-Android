package team.mino.feature.home.main.vm

import team.mino.core.common.android.architecture.SideEffect

/**
 * 홈이 한 번만 흘려보내는 신호 —
 * `docs/specs/home-deck-exploration/contracts/home-ui.md` §3이 목록을 소유한다.
 *
 * 홈 안에서 끝나는 전환(방 시트·액션 메뉴·가이드)은 여기 오지 않는다. 전부 `HomeUiState`의 상태다.
 *
 * **도메인 에러도 여기로 흘리지 않는다** — `DomainErrorEmitter`를 `HomeRoute`가 수집해 스낵바로 띄운다
 * (`docs/conventions/error_handling.md` §5·§6).
 */
internal sealed interface HomeSideEffect : SideEffect {
    /** 상세로 넘어간다. 경과일 초기화 알림은 이것과 별개로 이미 나갔고, 실패해도 전환을 막지 않는다. */
    data class NavigateToPlaceDetail(
        val pinId: String,
    ) : HomeSideEffect

    /** 방 만들기 폼으로 넘어간다(spec EC-015). */
    data object NavigateToRoomForm : HomeSideEffect

    /** 위치 권한을 물어야 한다. 응답은 [HomeIntent.LocationPermissionResult]로 되돌아온다. */
    data object RequestLocationPermission : HomeSideEffect

    /**
     * 다른 방 저장이 끝났다는 알림. 문구는 화면이 정한다(spec FR-005).
     *
     * **성공만 여기로 온다** — 저장 실패는 사용자 액션의 일회성 실패라 `DomainErrorEmitter`로 나간다
     * (`docs/conventions/error_handling.md` §5). 성패를 담아 실패까지 여기로 흘리면 위 단서와 어긋난다.
     */
    data object ShowSaveResult : HomeSideEffect
}
