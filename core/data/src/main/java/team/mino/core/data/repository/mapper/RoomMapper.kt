package team.mino.core.data.repository.mapper

import team.mino.core.data.network.dto.request.RoomRequest
import team.mino.core.data.network.dto.response.RoomResponse
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomDraft

/**
 * 색의 서버 표현. 표의 소유자는 `docs/specs/group-room-form/contracts/room-api-mock.md` §2다.
 *
 * 서버가 색을 다른 표현(hex 등)으로 확정하면 고칠 곳은 이 표 하나다. 도메인·UI·mock 저장소 어디에도
 * 이 문자열이 새어 나가지 않는다. 열거 상수 이름에서 파생하지 않는 이유도 같다 — 도메인 이름이 바뀌었을 때
 * 서버 계약이 조용히 따라 바뀌면 안 된다.
 */
private val COLOR_IDENTIFIERS: Map<RoomColor, String> =
    mapOf(
        RoomColor.RED to "red",
        RoomColor.RED_ORANGE to "red_orange",
        RoomColor.ORANGE to "orange",
        RoomColor.LIME to "lime",
        RoomColor.GREEN to "green",
        RoomColor.CYAN to "cyan",
        RoomColor.VIOLET to "violet",
        RoomColor.PINK to "pink",
        RoomColor.BLUE to "blue",
        RoomColor.BROWN to "brown",
        RoomColor.LIGHT_BLUE to "light_blue",
        RoomColor.PURPLE to "purple",
        RoomColor.GRAY to "gray",
    )

private val COLORS_BY_IDENTIFIER: Map<String, RoomColor> =
    COLOR_IDENTIFIERS.entries.associate { (color, identifier) -> identifier to color }

internal fun RoomResponse.toDomain(): Room =
    Room(
        id = id,
        name = name,
        description = description.orEmpty(),
        color = color.toRoomColor(),
        ownerId = ownerId,
    )

internal fun RoomDraft.toRequest(): RoomRequest =
    RoomRequest(
        name = name,
        description = description.toRequestDescription(),
        color = color.toIdentifier(),
    )

/** 설명이 없는 방은 빈 문자열이 아니라 `null`을 보낸다. */
private fun String.toRequestDescription(): String? = takeIf { it.isNotEmpty() }

/**
 * 고르지 않은 색은 여기서 [RoomColor.GRAY]로 확정된다. 요청에 "색 없음"이 없기 때문이다 —
 * 미선택을 유지하는 것은 폼까지의 책임이고, 저장 경로를 넘어가면 방은 이미 색을 가진다.
 */
internal fun RoomColor?.toIdentifier(): String = COLOR_IDENTIFIERS.getValue(this ?: RoomColor.GRAY)

/**
 * 아는 식별자가 아니면 [RoomColor.GRAY]로 읽는다. 서버가 팔레트를 넓혔다는 이유로 방 조회가 실패하면 안 되고,
 * 색을 갖지 않은 방이 이미 [RoomColor.GRAY]로 보이므로 표현이 어긋나지 않는다.
 */
internal fun String.toRoomColor(): RoomColor = COLORS_BY_IDENTIFIER[this] ?: RoomColor.GRAY
