package team.mino.core.domain.model

/**
 * [FR-011] `Peek`/`Half` 지도 마커 정렬 드롭다운 옵션.
 * [RoomListSortOption]과 통합하지 않는다 — docs/specs/room-list/research.md D7 참조.
 *
 * 선언 순서가 곧 드롭다운 노출 순서다(`entries` 순회) — Figma(node 2542-125408) 실측 순서
 * 그대로 `꾹 Pick → 전체 → 최신순 → 거리순 → 코멘트순`을 따른다.
 */
enum class MapMarkerSortOption {
    GGUK_PICK,
    ALL,
    LATEST,
    NEARBY,
    MOST_COMMENTED,
}
