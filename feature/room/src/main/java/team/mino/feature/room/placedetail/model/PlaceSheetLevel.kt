package team.mino.feature.room.placedetail.model

/**
 * 장소 상세 바텀시트가 멈춰 서는 단계.
 *
 * **높이를 알지 않는다.** 각 단계의 dp는 `docs/specs/place-detail/spec.md` FR-001이 소유하며, 그 값을 시트에 적용하는
 * 것은 `PlaceDetailSheet`의 몫이다. 여기에 높이를 들이면 같은 수치가 spec과 이 타입 두 곳에 남아 갈린다.
 *
 * 단계는 이 둘뿐이다. `Peek`이 없다는 것이 spec §4의 명시적 가정이고, 아래로 조금 끌었다 놓아도 중간 단계로 머무르지
 * 않는다(TS-015). `:feature:room`의 3단 시트와 단계 집합이 달라 그쪽 타입을 재사용하지 않는다
 * (`docs/specs/place-detail/research.md` D5).
 */
internal enum class PlaceSheetLevel {
    /** 진입 기본값. */
    HALF,

    FULL,
}
