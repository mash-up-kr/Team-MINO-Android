package team.mino.core.domain.model

/**
 * 카드에 붙는 추천 라벨. 서버 `labelGroup`과 1:1로 대응한다 —
 * `docs/specs/home-deck-exploration/contracts/deck-api.md` §2.2.
 *
 * **배정은 전적으로 서버 소관이다.** 앱은 판정에 관여하지 않고 받은 값을 표시만 한다. 라벨은 뽑힌 카드에 붙는 표시일 뿐
 * 덱의 순서를 바꾸지 않는다.
 *
 * 표시 문구는 feature가 소유한다. 서버 문자열과 이 값의 대응은 Mapper만 안다 — `core/domain/README.md` §5.
 */
enum class PlaceLabel {
    WORTH_VISITING,
    MANY_SAVES,
    MANY_COMMENTS,
    MANY_VIEWS,
}
