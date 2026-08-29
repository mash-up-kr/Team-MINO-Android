package team.mino.core.domain.model

/**
 * 장소분류 라벨.
 *
 * 서버 `labelGroup` enum과 1:1로 대응한다. 표시 문구는 갖지 않는다 — 문자열 리소스는 feature가 소유한다.
 *
 * **핀 상세 응답에는 이 값이 없다.** `labelGroup`은 홈 카드 응답에만 있어 지도 마커·방 상세·알림으로 들어온
 * 장소는 값을 알 길이 없고, 그래서 지금은 어느 진입점이든 [DEFAULT]로 그린다 —
 * `docs/specs/place-detail/research.md` D12. 서버가 핀 상세에 필드를 추가하면 Mapper 한 곳만 고치면 되도록
 * 타입을 미리 둔다.
 * 표시 문구는 feature가 소유한다. 서버 문자열과 이 값의 대응은 Mapper만 안다 — `core/domain/README.md` §5.
 */
enum class PlaceLabel {
    WORTH_VISITING,
    MANY_COMMENTS,
    MANY_SAVES,
    MANY_VIEWS,
    ;

    companion object {
        /**
         * 상위 세 라벨에 걸리지 않은 장소가 갖는 값.
         *
         * 라벨 자리가 비는 장소는 없다는 것이 `spec.md` EC-005의 규정이므로, 값을 모를 때 라벨을 지우는 대신
         * 이 값을 쓴다.
         */
        val DEFAULT: PlaceLabel = WORTH_VISITING
    }
}
