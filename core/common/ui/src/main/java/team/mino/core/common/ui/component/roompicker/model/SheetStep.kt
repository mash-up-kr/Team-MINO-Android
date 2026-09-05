package team.mino.core.common.ui.component.roompicker.model

/**
 * 방 선택 시트가 멈춰 서는 단계.
 *
 * **높이를 알지 않는다.** 각 단계의 dp와 방 개수별 분기는
 * `docs/specs/shared-link-receiver/contracts/room-picker-sheet-ui.md` §3.1이 소유하며, 그 값을 시트에 적용하는 것은
 * `RoomPickerSheet`의 몫이다. 여기에 높이를 들이면 같은 수치가 계약과 이 타입 두 곳에 남아 갈린다.
 *
 * 방이 몇 개든 단계는 이 둘뿐이다(spec EC-005 · TS-020). 방이 적으면 카드 아래가 빌 뿐 단계가 줄지 않는다.
 */
enum class SheetStep {
    /** 진입 기본값. */
    PEEK,

    FULL,
}
