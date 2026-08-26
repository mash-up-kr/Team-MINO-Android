package team.mino.feature.roomform.form.model

import androidx.annotation.DrawableRes
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor
import team.mino.core.domain.model.RoomColor
import team.mino.feature.roomform.R

/**
 * 이 색을 나타내는 팔레트 칩. 고를 수 없는 [RoomColor.GRAY]는 칩이 없어 `null`이다.
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

/**
 * 방을 대표하는 캐릭터 썸네일. 색에서 파생되므로 캐릭터를 따로 고르는 입력이 없다.
 *
 * 아직 색을 고르지 않은 방은 [RoomColor.GRAY]의 것으로 그린다 — 미선택은 저장 시 회색으로 확정되고,
 * 회색 방의 캐릭터도 이 에셋 하나로 정해져 있다.
 */
@get:DrawableRes
internal val RoomColor?.thumbnailRes: Int
    get() = when (this) {
        RoomColor.RED -> R.drawable.room_thumbnail_red
        RoomColor.RED_ORANGE -> R.drawable.room_thumbnail_red_orange
        RoomColor.ORANGE -> R.drawable.room_thumbnail_orange
        RoomColor.LIME -> R.drawable.room_thumbnail_lime
        RoomColor.GREEN -> R.drawable.room_thumbnail_green
        RoomColor.CYAN -> R.drawable.room_thumbnail_cyan
        RoomColor.VIOLET -> R.drawable.room_thumbnail_violet
        RoomColor.PINK -> R.drawable.room_thumbnail_pink
        RoomColor.BLUE -> R.drawable.room_thumbnail_blue
        RoomColor.BROWN -> R.drawable.room_thumbnail_brown
        RoomColor.LIGHT_BLUE -> R.drawable.room_thumbnail_light_blue
        RoomColor.PURPLE -> R.drawable.room_thumbnail_purple
        RoomColor.GRAY, null -> R.drawable.room_thumbnail_gray
    }
