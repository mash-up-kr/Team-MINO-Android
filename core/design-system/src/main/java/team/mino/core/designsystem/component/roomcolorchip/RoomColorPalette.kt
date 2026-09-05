package team.mino.core.designsystem.component.roomcolorchip

import androidx.compose.ui.graphics.Color
import team.mino.core.designsystem.foundation.color.token.AtomicColorToken

/**
 * [MinoRoomColor]를 대표하는 단색 하나. 지도 마커·클러스터 배지(`:core:common:ui`)처럼 칩 밖에서
 * "이 방 색"을 단색 하나로 그려야 하는 다른 모듈이 쓴다 — `AtomicColorToken`이 이 모듈 내부([internal])라
 * 직접 참조할 수 없어서, 그 값을 이 공개 프로퍼티 하나로 노출한다.
 *
 * Figma 클러스터 배지 견본(node `2392-128633`~`2392-128643`) 실측값이다. [RoomColorChipTokens]의
 * 선택 상태 채움색(`selectedContainerColor`)과 값이 비슷하지만 `Green`·`Pink`·`Brown`·`LightBlue`
 * 넷은 다르다 — 그 매핑은 칩 전용이라 재사용하지 않고 이 프로퍼티를 새로 둔다.
 */
val MinoRoomColor.representativeColor: Color
    get() = when (this) {
        MinoRoomColor.Red -> AtomicColorToken.Red50
        MinoRoomColor.RedOrange -> AtomicColorToken.RedOrange50
        MinoRoomColor.Orange -> AtomicColorToken.Orange50
        MinoRoomColor.Lime -> AtomicColorToken.Lime50
        MinoRoomColor.Green -> AtomicColorToken.Green50
        MinoRoomColor.Cyan -> AtomicColorToken.Cyan50
        MinoRoomColor.Violet -> AtomicColorToken.Violet50
        MinoRoomColor.Pink -> AtomicColorToken.Pink70
        MinoRoomColor.Blue -> AtomicColorToken.Blue50
        MinoRoomColor.Brown -> Color(0xFFDBA679)
        MinoRoomColor.LightBlue -> AtomicColorToken.LightBlue70
        MinoRoomColor.Purple -> AtomicColorToken.Purple50
    }
