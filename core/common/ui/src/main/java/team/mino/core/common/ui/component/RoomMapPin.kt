package team.mino.core.common.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import team.mino.core.common.ui.R
import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor

/**
 * 지도 위 장소 핀. 방 대표 색마다 이미지 한 장이 정해져 있다(Figma `Pin` 컴포넌트셋).
 *
 * [RoomThumbnailFallback]과 같은 이유로 색과 그림이 한 장에 담겨 있어 배경을 따로 깔지 않는다.
 * 크기는 이 컴포넌트가 정하지 않는다 — 지도 마커 크기는 호출부([modifier])가 안다.
 *
 * @param color 핀이 속한 방의 대표 색. `null`은 색을 고르지 않은 방(개인 방 등)의 기본 핀이다
 *   (`docs/adr/2026-08-14-room-color-palette-in-design-system.md`).
 * @param selected 눌려서 강조된 상태인지(Figma `mode=on`, "Attention" 핀). 장소 상세가 열린 핀에 쓴다.
 */
@Composable
fun RoomMapPin(
    color: MinoRoomColor?,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(pinRes(color, selected)),
        contentDescription = null,
        modifier = modifier,
    )
}

@DrawableRes
private fun pinRes(
    color: MinoRoomColor?,
    selected: Boolean,
): Int = if (selected) selectedPinRes(color) else normalPinRes(color)

@DrawableRes
private fun normalPinRes(color: MinoRoomColor?): Int =
    when (color) {
        MinoRoomColor.Red -> R.drawable.pin_red
        MinoRoomColor.RedOrange -> R.drawable.pin_red_orange
        MinoRoomColor.Orange -> R.drawable.pin_orange
        MinoRoomColor.Lime -> R.drawable.pin_lime
        MinoRoomColor.Green -> R.drawable.pin_green
        MinoRoomColor.Cyan -> R.drawable.pin_cyan
        MinoRoomColor.Violet -> R.drawable.pin_violet
        MinoRoomColor.Pink -> R.drawable.pin_pink
        MinoRoomColor.Blue -> R.drawable.pin_blue
        MinoRoomColor.Brown -> R.drawable.pin_brown
        MinoRoomColor.LightBlue -> R.drawable.pin_light_blue
        MinoRoomColor.Purple -> R.drawable.pin_purple
        null -> R.drawable.pin_default
    }

@DrawableRes
private fun selectedPinRes(color: MinoRoomColor?): Int =
    when (color) {
        MinoRoomColor.Red -> R.drawable.pin_red_selected
        MinoRoomColor.RedOrange -> R.drawable.pin_red_orange_selected
        MinoRoomColor.Orange -> R.drawable.pin_orange_selected
        MinoRoomColor.Lime -> R.drawable.pin_lime_selected
        MinoRoomColor.Green -> R.drawable.pin_green_selected
        MinoRoomColor.Cyan -> R.drawable.pin_cyan_selected
        MinoRoomColor.Violet -> R.drawable.pin_violet_selected
        MinoRoomColor.Pink -> R.drawable.pin_pink_selected
        MinoRoomColor.Blue -> R.drawable.pin_blue_selected
        MinoRoomColor.Brown -> R.drawable.pin_brown_selected
        MinoRoomColor.LightBlue -> R.drawable.pin_light_blue_selected
        MinoRoomColor.Purple -> R.drawable.pin_purple_selected
        null -> R.drawable.pin_default_selected
    }
