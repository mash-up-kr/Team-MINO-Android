package team.mino.feature.placedetail.main.vm

import team.mino.core.common.android.architecture.Intent
import team.mino.feature.placedetail.main.model.PlaceSheetLevel

/**
 * 장소 상세 화면에서 사용자가 일으키는 일.
 *
 * **[저장된 방] 버튼의 Intent가 없다.** 그 버튼은 항상 비활성이라 눌리는 일이 없다
 * (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §6).
 */
internal sealed interface PlaceDetailIntent : Intent {
    /** 시트가 다음 단계로 멈춰 섰다. 사용자의 드래그와 화면이 정하는 스냅이 같은 자리로 들어온다. */
    data class OnSheetLevelChange(
        val level: PlaceSheetLevel,
    ) : PlaceDetailIntent

    /**
     * 시트 콘텐츠의 스크롤이 최상단인지 바뀌었다.
     *
     * 헤더 밀도를 가르는 유일한 근거다. 스크롤 양을 올리지 않는 것은 화면이 이미 판정한 결과만 받으면 되기 때문이다.
     */
    data class OnScrollOffsetChange(
        val isAtTop: Boolean,
    ) : PlaceDetailIntent

    /**
     * [나가기] 버튼과 시트를 아래로 끌어 닫는 동작.
     *
     * 둘은 같은 처리라는 것이 spec EC-003의 규정이므로 Intent를 가르지 않는다.
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
     * 주 데이터 조회가 실패해 선 오류 화면의 [다시 시도].
     *
     * 실패한 조회 하나를 고르지 않는다 — 오류 화면은 어느 조회가 깨졌는지 구분해 보여 주지 않으므로
     * 사용자가 요청하는 것도 「다시 불러오기」 하나다.
     */
    data object OnRetryLoadClick : PlaceDetailIntent
}
