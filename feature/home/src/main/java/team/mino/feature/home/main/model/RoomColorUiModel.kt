package team.mino.feature.home.main.model

import androidx.annotation.DrawableRes
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor
import team.mino.core.domain.model.RoomColor
import team.mino.feature.home.R

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

/**
 * 이 색의 방 캐릭터 에셋(Figma `Home_Avatar`, `4306:63718`). [chip]과 달리 `null`이 없다 — 색을
 * 고르지 않은 방([RoomColor.GRAY])도 "미선택" 캐릭터(`black`)를 그린다.
 *
 * [RoomColor.BROWN]은 `Home_Avatar`에 대응 variant가 없다. 협의 전까지 `black`으로 떨어뜨린다
 * (`docs/specs/home-deck-exploration/research.md` R-015).
 */
@get:DrawableRes
internal val RoomColor.character: Int
    get() = when (this) {
        RoomColor.RED -> R.drawable.home_room_character_red
        RoomColor.RED_ORANGE -> R.drawable.home_room_character_red_orange
        RoomColor.ORANGE -> R.drawable.home_room_character_orange
        RoomColor.LIME -> R.drawable.home_room_character_lime
        RoomColor.GREEN -> R.drawable.home_room_character_green
        RoomColor.CYAN -> R.drawable.home_room_character_cyan
        RoomColor.VIOLET -> R.drawable.home_room_character_violet
        RoomColor.PINK -> R.drawable.home_room_character_pink
        RoomColor.BLUE -> R.drawable.home_room_character_blue
        RoomColor.BROWN -> R.drawable.home_room_character_black
        RoomColor.LIGHT_BLUE -> R.drawable.home_room_character_light_blue
        RoomColor.PURPLE -> R.drawable.home_room_character_purple
        RoomColor.GRAY -> R.drawable.home_room_character_black
    }
