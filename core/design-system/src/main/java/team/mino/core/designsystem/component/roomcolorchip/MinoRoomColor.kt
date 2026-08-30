package team.mino.core.designsystem.component.roomcolorchip

/**
 * 방을 대표하는 색 12종의 팔레트.
 *
 * 선언 순서는 Figma 색상 칩 그리드의 나열 순서를 따른다. 팔레트가 곧 순서이므로 호출부가
 * 다시 정렬하지 않는다.
 *
 * **이 enum은 팔레트일 뿐 도메인 규칙을 갖지 않는다**
 * (`docs/adr/2026-08-14-room-color-palette-in-design-system.md` 참조).
 * 회색 기본값·표시 이름·서버 식별자·그리드 배치는 여기에 없다.
 *
 * - **미선택**은 소비처가 `MinoRoomColor?`의 `null`로 표현한다. 팔레트에 "없음" 항목을 두지 않는다.
 * - **서버 식별자와의 매핑**은 그것을 소비하는 바깥 레이어가 갖는다.
 * - **배치**는 화면의 구성이다. 칩은 자기 한 칸만 안다.
 */
enum class MinoRoomColor {
    Red,
    RedOrange,
    Orange,
    Lime,
    Green,
    Cyan,
    Violet,
    Pink,
    Blue,
    Brown,
    LightBlue,
    Purple,
}
