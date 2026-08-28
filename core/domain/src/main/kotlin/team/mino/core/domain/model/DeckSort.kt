package team.mino.core.domain.model

/**
 * 홈 카드 덱의 정렬. 후보 집합을 좁히고 순위까지 정하는 값이라, 홈은 서버가 준 순서를 그대로 쓴다.
 *
 * **선언 순서가 곧 우선순위다.** 같은 방에서 다음 덱을 고를 때 미소진 정렬 중 이 순서로 가장 앞선 것을 택한다 —
 * `docs/specs/home-deck-exploration/contracts/home-ui.md` §4.1. 순서를 바꾸면 전환 규칙이 바뀐다.
 *
 * 서버 문자열(`ggukPick`·`latest`·`nearby`)과의 대응은 Mapper만 안다 — `core/domain/README.md` §5.
 *
 * [NEAREST]는 좌표를 필요로 하는 유일한 정렬이다. 좌표가 없으면 요청을 보내지 않고 빈 덱으로 다룬다.
 */
enum class DeckSort {
    GGUK_PICK,
    LATEST,
    NEAREST,
}
