package team.mino.core.domain.model

/**
 * 방이 가질 수 있는 대표 색.
 *
 * 색의 실체(hex·표시 이름·캐릭터 에셋)는 갖지 않는다. 팔레트는 `:core:design-system`이, 이 값과 팔레트의 대응은
 * feature가 소유한다 — `docs/adr/2026-08-14-room-color-palette-in-design-system.md`.
 *
 * 선언 순서는 색 선택 칩 그리드의 배치 순서를 그대로 따른다. 그리드는 이 순서로 순회한다.
 *
 * [GRAY]는 "값 없음"이 아니라 **색을 고르지 않은 방이 갖게 되는 색**이다. 사용자가 직접 고를 수 없으므로
 * [selectable]에서 빠진다. 미선택 상태는 `RoomDraft.color`의 `null`이 표현한다.
 */
enum class RoomColor {
    RED,
    RED_ORANGE,
    ORANGE,
    LIME,
    GREEN,
    CYAN,
    VIOLET,
    PINK,
    BLUE,
    BROWN,
    LIGHT_BLUE,
    PURPLE,
    GRAY,
    ;

    companion object {
        /** 사용자가 고를 수 있는 색. 칩 그리드가 순회할 목록의 단일 출처다. */
        val selectable: List<RoomColor> = entries - GRAY
    }
}
