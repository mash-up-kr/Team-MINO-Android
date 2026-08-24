package team.mino.core.domain.model

/**
 * [FR-011] `Peek`/`Half` 지도 마커 정렬 드롭다운 옵션.
 * [RoomListSortOption]과 통합하지 않는다 — docs/specs/room-list/research.md D7 참조.
 */
enum class MapMarkerSortOption {
    ALL,
    GGUK_PICK,
    LATEST,
    NEARBY,
    MOST_COMMENTED,
}
