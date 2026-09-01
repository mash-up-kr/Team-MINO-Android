package team.mino.feature.room.main.model

import team.mino.core.designsystem.component.roomcolorchip.MinoRoomColor
import team.mino.core.domain.model.Place

/**
 * 지도에 얹을 핀 하나 — 어느 장소인지와 어떤 색으로 그릴지를 함께 든다.
 *
 * 개인 방 장소는 항상 `null`(기본 검정 핀), 공동방 장소는 그 방의 대표 색([RoomColorMapping.chip])을
 * [color]로 받는다 — [RoomListMap]은 그 결정을 다시 하지 않고 그대로 그린다.
 */
data class MapPinUiModel(
    val place: Place,
    val color: MinoRoomColor?,
)
