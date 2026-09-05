package team.mino.feature.home.main.vm

import team.mino.core.common.android.architecture.Intent
import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.domain.model.DeckSort

/**
 * 홈에서 사용자가 일으키는 일. 조작 하나에 하나씩 대응한다 —
 * `docs/specs/home-deck-exploration/contracts/home-ui.md` §2가 목록을 소유한다.
 *
 * **거르는 규칙 둘**을 `HomeViewModel`이 여기 들어온 값에 적용한다.
 * - `isGuideVisible`이면 [DismissGuide]를 뺀 전부를 버린다(spec FR-019).
 * - `isTransitioning`이면 [SwipeForward]·[SwipeBackward]를 버린다(spec UX-001).
 *
 * 버린다는 것은 **미루지 않는다**는 뜻이다 — 나중에 재생할 큐를 두지 않는다(`research.md` R-007).
 */
internal sealed interface HomeIntent : Intent {
    /** 좌→우 드래그 완료. 덱에서 카드를 덜어낼 뿐 서버를 부르지 않는다(spec FR-001·023). */
    data object SwipeForward : HomeIntent

    /**
     * 우→좌 드래그 완료. 직전 [SwipeForward]만 취소한다.
     *
     * 이미 나간 경과일 초기화는 되돌리지 않는다 — 카드만 돌아온다(spec EC-017).
     */
    data object SwipeBackward : HomeIntent

    /** 전환 애니메이션이 끝났다. 사용자가 아니라 애니메이션이 일으키는 유일한 의도다. */
    data object TransitionSettled : HomeIntent

    /** 정렬 칩 탭. 소진 여부와 무관하게 사용자가 직접 고른 것이다(spec FR-010). */
    data class SelectSort(
        val sort: DeckSort,
    ) : HomeIntent

    /** 카드의 `[...]` 탭. */
    data class OpenActionMenu(
        val pinId: String,
    ) : HomeIntent

    /** 메뉴 바깥 탭·스와이프 등 메뉴를 닫는 모든 경로가 결과가 같아 하나로 받는다(spec EC-004·005). */
    data object DismissActionMenu : HomeIntent

    /** 액션 메뉴의 `다른 방 저장`. 어느 방에 저장할지는 이어지는 흐름이 정한다(spec FR-005). */
    data class SaveToAnotherRoom(
        val pinId: String,
    ) : HomeIntent

    /** 「방 선택 시트」의 체크박스 탭. 같은 방을 다시 탭하면 선택이 풀린다(spec FR-005). */
    data class ToggleSaveTargetRoom(
        val roomId: String,
    ) : HomeIntent

    /** 「방 선택 시트」의 `저장하기` 탭. 선택된 방이 없으면 애초에 눌리지 않는다(spec EC-018). */
    data object ConfirmSaveTargets : HomeIntent

    /** 방을 고르지 않고 「방 선택 시트」를 닫는다. */
    data object DismissSavePicker : HomeIntent

    /**
     * 카드 본문 탭. 경과일 초기화를 알리고 상세로 넘어간다.
     *
     * **덱은 그대로 둔다** — 잔여 카드 수·소진 여부·되돌리기 이력 어느 것도 바뀌지 않는다(spec FR-023).
     */
    data class OpenPlaceDetail(
        val pinId: String,
    ) : HomeIntent

    /** 방 뱃지·캐릭터 탭. 두 곳 다 같은 시트를 연다(spec FR-017). */
    data object OpenRoomSheet : HomeIntent

    /** 시트에서 방 선택. 지금 보던 방을 다시 골라도 들어온다(spec EC-014). */
    data class SelectRoom(
        val roomId: String,
    ) : HomeIntent

    /** 방을 고르지 않고 시트를 닫는다. */
    data object DismissRoomSheet : HomeIntent

    /** 가이드 우측 상단 닫기. 가이드가 떠 있는 동안 통과하는 유일한 의도다(spec FR-019). */
    data object DismissGuide : HomeIntent

    /**
     * 위치 권한 다이얼로그 응답. 허용이면 좌표, 거부면 `null`이다.
     *
     * 거부를 실패로 다루지 않는다 — 좌표 없는 `NEAREST`는 빈 덱이 되어 「소진」으로 흡수된다
     * (spec EC-009, `research.md` R-009·R-013).
     */
    data class LocationPermissionResult(
        val location: GeoPoint?,
    ) : HomeIntent
}
