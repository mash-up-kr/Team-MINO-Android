package team.mino.core.domain.model

/**
 * 장소분류 라벨.
 *
 * **홈 카드 덱의 타입이다.** [PlaceCard.label]이 유일한 보유자이고, 값은 홈 카드 응답의 `labelGroup`에서만
 * 온다. 장소 상세는 이 값을 쓰지 않는다 — 라벨을 상세 어디에도 노출하지 않고 홈 카드로 진입해도 넘겨받지
 * 않는다는 것이 `docs/specs/place-detail/spec.md` FR-005·EC-005의 규정이다.
 *
 * 표시 문구는 갖지 않는다 — 문자열 리소스는 feature가 소유한다. 서버 문자열과 이 값의 대응은 Mapper만
 * 안다 — `core/domain/README.md` §5.
 */
enum class PlaceLabel {
    WORTH_VISITING,
    MANY_COMMENTS,
    MANY_SAVES,
    MANY_VIEWS,
}
