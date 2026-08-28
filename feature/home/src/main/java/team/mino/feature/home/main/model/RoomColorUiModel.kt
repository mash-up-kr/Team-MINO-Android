package team.mino.feature.home.main.model

import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor
import team.mino.core.domain.model.RoomColor

/**
 * 이 색을 나타내는 팔레트 값. 팔레트에 없는 [RoomColor.GRAY]는 `null`이다.
 *
 * 팔레트는 `:core:design-system`이 갖고 도메인 값은 `:core:domain`이 갖는다. 팔레트는 도메인을 모르고
 * 도메인은 팔레트를 모르므로, 둘의 대응은 양쪽을 모두 아는 feature가 소유한다
 * (`docs/adr/2026-08-14-room-color-palette-in-design-system.md`).
 */
internal val RoomColor.chip: MinoRoomColor?
    get() = when (this) {
        RoomColor.RED -> MinoRoomColor.Red
        RoomColor.RED_ORANGE -> MinoRoomColor.RedOrange
        RoomColor.ORANGE -> MinoRoomColor.Orange
        RoomColor.LIME -> MinoRoomColor.Lime
        RoomColor.GREEN -> MinoRoomColor.Green
        RoomColor.CYAN -> MinoRoomColor.Cyan
        RoomColor.VIOLET -> MinoRoomColor.Violet
        RoomColor.PINK -> MinoRoomColor.Pink
        RoomColor.BLUE -> MinoRoomColor.Blue
        RoomColor.BROWN -> MinoRoomColor.Brown
        RoomColor.LIGHT_BLUE -> MinoRoomColor.LightBlue
        RoomColor.PURPLE -> MinoRoomColor.Purple
        RoomColor.GRAY -> null
    }
