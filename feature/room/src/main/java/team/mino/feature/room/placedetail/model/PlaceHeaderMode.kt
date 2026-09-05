package team.mino.feature.room.placedetail.model

/**
 * 시트 상단 헤더가 그리는 밀도.
 *
 * **[PlaceSheetLevel]에서 파생시키지 않는다.** 축소형으로 바뀌는 근거는 시트가 `FULL`이라는 사실이 아니라 **콘텐츠의
 * 스크롤 위치**다 — `FULL`이어도 최상단이면 확장형이고, 콘텐츠가 화면보다 짧아 스크롤이 없으면 계속 확장형이다
 * (spec FR-008 · EC-007, `docs/specs/place-detail/research.md` D5). 두 타입은 서로 독립이다.
 *
 * 각 모드가 무엇을 담는지(확장형: 등록자·라벨·장소명·주소, 축소형: 장소명 + [나가기])는 spec FR-003·FR-008이 소유하며
 * 그리는 것은 `PlaceDetailHeader`의 몫이다.
 */
internal enum class PlaceHeaderMode {
    /** 진입 기본값. */
    EXPANDED,

    COLLAPSED,
}
