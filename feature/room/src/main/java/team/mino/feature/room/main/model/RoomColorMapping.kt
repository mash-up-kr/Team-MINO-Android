package team.mino.feature.room.main.model

import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor
import team.mino.core.domain.model.RoomColor

/**
 * 방 대표 색과 팔레트 칩의 대응. `:feature:roomform`의 `RoomColorUiModel.chip`과 같은 표다 — 팔레트는
 * 도메인을 모르고 도메인은 팔레트를 모르므로, 둘의 대응은 양쪽을 모두 아는 feature가 소유한다
 * (`docs/adr/2026-08-14-room-color-palette-in-design-system.md`).
 *
 * 지도 핀(`RoomListMap`)이 공동방 핀 색을 정하는 데 쓴다 — 개인 방([RoomColor.GRAY])은 칩이 없어
 * `null`이고, 그 자리는 내 프로필 색([ProfileAvatarMapping.roomColor])이 대신한다.
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

/**
 * 도메인 색 식별자 문자열([team.mino.core.domain.model.RoomThumbnail.ColorAndCharacter.color])과
 * 팔레트의 대응 — `RoomListBottomSheet`·`RoomInviteSheet` 둘 다 같은 표를 각자 `private`로 들고
 * 있던 걸 여기로 모았다. 아는 식별자가 아니면 회색(`null`)으로 읽는다([RoomMapper.toRoomColor]와
 * 같은 이유).
 */
internal fun String.toMinoRoomColor(): MinoRoomColor? =
    when (this) {
        "red" -> MinoRoomColor.Red
        "red_orange" -> MinoRoomColor.RedOrange
        "orange" -> MinoRoomColor.Orange
        "lime" -> MinoRoomColor.Lime
        "green" -> MinoRoomColor.Green
        "cyan" -> MinoRoomColor.Cyan
        "violet" -> MinoRoomColor.Violet
        "pink" -> MinoRoomColor.Pink
        "blue" -> MinoRoomColor.Blue
        "brown" -> MinoRoomColor.Brown
        "light_blue" -> MinoRoomColor.LightBlue
        "purple" -> MinoRoomColor.Purple
        else -> null
    }
