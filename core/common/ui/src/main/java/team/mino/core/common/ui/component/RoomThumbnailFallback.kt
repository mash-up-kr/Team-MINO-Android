package team.mino.core.common.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import team.mino.core.common.ui.R
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor

/**
 * 썸네일로 쓸 장소 이미지가 없는 방을 대신 그리는 캐릭터 이미지.
 *
 * 방 대표 색마다 캐릭터 한 장이 정해져 있고, 색 배경과 캐릭터가 한 장에 담겨 있어 배경을 따로
 * 깔지 않는다.
 *
 * 크기와 모서리는 이 컴포넌트가 정하지 않는다 — 콜라주 자리를 대신 채우는 컴포넌트라 그 기하는
 * 자리를 아는 호출부가 [modifier]로 준다.
 *
 * @param color 방 대표 색. `null`은 색을 고르지 않은 회색 방이다
 *   (`docs/adr/2026-08-14-room-color-palette-in-design-system.md`).
 */
@Composable
fun RoomThumbnailFallback(
    color: MinoRoomColor?,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(color.thumbnailRes),
        contentDescription = null,
        modifier = modifier,
    )
}

@get:DrawableRes
private val MinoRoomColor?.thumbnailRes: Int
    get() = when (this) {
        MinoRoomColor.Red -> R.drawable.room_thumbnail_red
        MinoRoomColor.RedOrange -> R.drawable.room_thumbnail_red_orange
        MinoRoomColor.Orange -> R.drawable.room_thumbnail_orange
        MinoRoomColor.Lime -> R.drawable.room_thumbnail_lime
        MinoRoomColor.Green -> R.drawable.room_thumbnail_green
        MinoRoomColor.Cyan -> R.drawable.room_thumbnail_cyan
        MinoRoomColor.Violet -> R.drawable.room_thumbnail_violet
        MinoRoomColor.Pink -> R.drawable.room_thumbnail_pink
        MinoRoomColor.Blue -> R.drawable.room_thumbnail_blue
        MinoRoomColor.Brown -> R.drawable.room_thumbnail_brown
        MinoRoomColor.LightBlue -> R.drawable.room_thumbnail_light_blue
        MinoRoomColor.Purple -> R.drawable.room_thumbnail_purple
        null -> R.drawable.room_thumbnail_gray
    }
