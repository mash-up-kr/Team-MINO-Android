package team.mino.feature.room.placedetail.vm

import team.mino.core.common.android.architecture.SideEffect

/**
 * 화면 밖으로 나가야 끝나는 일들.
 *
 * **오류를 나르지 않는다.** `MinoDomainException`은 `CollectDomainError`가 공통 스낵바로 처리하므로 이 화면이
 * 다시 실어 나르면 같은 오류가 두 번 뜬다(`docs/conventions/error_handling.md`).
 */
internal sealed interface PlaceDetailSideEffect : SideEffect {
    /** 화면을 끝낸다. 끝낸 뒤 어디에 남을지는 진입점이 안다. */
    data object Exit : PlaceDetailSideEffect

    /**
     * 외부 지도로 장소를 연다.
     *
     * @property mapUrl 서버가 준 지도 링크. 없으면 `null`이며 받는 쪽이 [query]로 대신 연다.
     * @property query 장소명. [mapUrl]이 없을 때의 검색어다.
     */
    data class OpenExternalMap(
        val mapUrl: String?,
        val query: String,
    ) : PlaceDetailSideEffect

    /** 장소의 원문 링크를 연다. */
    data class OpenSourceLink(
        val url: String,
    ) : PlaceDetailSideEffect

    /** 공유가 끝났음을 알린다. 문구와 표시 방법은 화면이 정한다. */
    data object ShowShareCompleted : PlaceDetailSideEffect

    /**
     * 「지금 보고 있는 방」을 [roomId]로 바꾼다(spec FR-025).
     *
     * 상태 변경인데도 SideEffect인 것은 **바꿔야 할 값이 다른 ViewModel의 것**이기 때문이다. 이 화면은 자기
     * 상태만 쓸 수 있으므로 방을 쥔 쪽으로 올려보낸다
     * (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §5).
     *
     * @property pinId 옮겨 갈 방에서 이 장소를 가리키는 핀. 이 값이 새 상세 화면의 진입 인자가 된다.
     * @property roomId 옮겨 간 뒤의 「지금 보고 있는 방」. [pinId]와 함께 갱신되어야 마커 양식·코멘트·[나가기]
     *  목적지가 서로 다른 방을 가리키는 상태가 생기지 않는다(spec SC-009).
     */
    data class SwitchRoom(
        val pinId: String,
        val roomId: String,
    ) : PlaceDetailSideEffect
}
