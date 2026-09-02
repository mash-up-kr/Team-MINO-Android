package team.mino.feature.room.placedetail.vm

import team.mino.core.common.android.architecture.Intent
import team.mino.feature.room.placedetail.model.PlaceSheetLevel

/** 장소 상세 화면에서 사용자가 일으키는 일. */
internal sealed interface PlaceDetailIntent : Intent {
    /** 시트가 다음 단계로 멈춰 섰다. 사용자의 드래그와 화면이 정하는 스냅이 같은 자리로 들어온다. */
    data class OnSheetLevelChange(
        val level: PlaceSheetLevel,
    ) : PlaceDetailIntent

    /**
     * 헤더를 확장형으로 둘지가 바뀌었다.
     *
     * 스크롤 위치가 그 근거이나(spec FR-008), 접을 수 있는지는 두 헤더의 높이까지 재야 갈린다 — 접힌 헤더가
     * 스크롤 범위를 줄여 스스로를 되돌리는 자리가 있기 때문이다. 그 판정을 화면이 하고 결과만 올린다.
     */
    data class OnHeaderExpansionChange(
        val isExpanded: Boolean,
    ) : PlaceDetailIntent

    /**
     * [나가기] 버튼.
     *
     * 시트를 아래로 끌어 닫는 경로는 없다 — `HALF`가 하한이라 드래그는 화면을 끝내지 못한다(spec FR-001 ·
     * EC-003). 시스템 뒤로가기는 `RoomListRoute`의 `BackHandler`가 자기 인텐트로 처리한다.
     */
    data object OnExitClick : PlaceDetailIntent

    /** 이미지 캐러셀의 현재 장이 바뀌었다. */
    data class OnCarouselPageChange(
        val page: Int,
    ) : PlaceDetailIntent

    /** [지도에서 보기]. 어느 앱으로 열지는 SideEffect를 받는 쪽이 정한다. */
    data object OnOpenMapClick : PlaceDetailIntent

    /** [원문보기]. 링크가 없는 장소에서는 버튼이 비활성이라 도달하지 않는다. */
    data object OnOpenSourceClick : PlaceDetailIntent

    /** 상한은 입력 컴포저블이 이미 막았다. 받은 값을 그대로 반영한다. */
    data class OnCommentDraftChange(
        val value: String,
    ) : PlaceDetailIntent

    /** 코멘트 전송. 공백만 남은 입력에서는 버튼이 비활성이라 도달하지 않는다. */
    data object OnSubmitCommentClick : PlaceDetailIntent

    /** 내가 쓴 코멘트의 [⋮] → [삭제]. */
    data class OnDeleteCommentClick(
        val commentId: String,
    ) : PlaceDetailIntent

    /** 코멘트 목록 최상단에 닿아 이전 페이지를 요청한다(역방향 페이징). */
    data object OnLoadOlderComments : PlaceDetailIntent

    /** [다른방에 공유]. 시트를 연다. */
    data object OnShareClick : PlaceDetailIntent

    /** 공유 시트의 방 카드 탭. 같은 방을 다시 누르면 선택이 풀린다. */
    data class OnShareRoomToggle(
        val roomId: String,
    ) : PlaceDetailIntent

    /** 공유 시트의 CTA. */
    data object OnShareConfirmClick : PlaceDetailIntent

    /** 공유 시트를 닫는 모든 경로 — 딤 바깥 탭·아래로 끌기·뒤로가기. 선택은 버려진다. */
    data object OnShareSheetDismiss : PlaceDetailIntent

    /**
     * [저장된 방]. 시트를 연다.
     *
     * 이 장소가 저장된 방이 하나뿐이면 버튼이 비활성이라(spec EC-024) 도달하지 않는다.
     */
    data object OnSavedRoomsClick : PlaceDetailIntent

    /**
     * [저장된 방] 시트의 방 카드 탭. 누르는 것이 곧 확정이라 별도 CTA가 없다(spec FR-024 · TS-043).
     *
     * @property pinId 옮겨 갈 방에서 이 장소를 가리키는 핀. 방 id가 아니라 핀 id를 싣는 것은 코멘트가
     *  (장소, 방) 쌍인 핀에 매달려 있어, 방만으로는 무엇을 다시 조회할지 정해지지 않기 때문이다.
     * @property roomId 옮겨 간 뒤 「지금 보고 있는 방」이 될 방. 마커 양식과 [나가기] 목적지가 이 값을 따른다
     *  (spec FR-025).
     */
    data class OnSavedRoomSelected(
        val pinId: String,
        val roomId: String,
    ) : PlaceDetailIntent

    /** [저장된 방] 시트를 닫는 모든 경로 — 딤 바깥 탭·아래로 끌기·뒤로가기. 보고 있는 방은 그대로다(spec EC-025). */
    data object OnSavedRoomsSheetDismiss : PlaceDetailIntent

    /**
     * 주 데이터 조회가 실패해 선 오류 화면의 [다시 시도].
     *
     * 실패한 조회 하나를 고르지 않는다 — 오류 화면은 어느 조회가 깨졌는지 구분해 보여 주지 않으므로
     * 사용자가 요청하는 것도 「다시 불러오기」 하나다.
     */
    data object OnRetryLoadClick : PlaceDetailIntent
}
