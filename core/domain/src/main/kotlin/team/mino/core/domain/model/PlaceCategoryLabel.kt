package team.mino.core.domain.model

/**
 * 장소분류 라벨 4종. 판정은 서버 소관이며 도메인은 값만 갖는다.
 *
 * 표시 문구는 UI 레이어가 갖는다.
 */
enum class PlaceCategoryLabel {
    /** 클릭수 상위 */
    FRIENDS_MOST_VIEWED,

    /** 코멘트수 상위 */
    MOST_TALKED,

    /** 중복 저장 상위 */
    MOST_SAVED,

    /** 위 셋에 걸리지 않은 장소의 기본값 */
    WORTH_VISITING,
}
