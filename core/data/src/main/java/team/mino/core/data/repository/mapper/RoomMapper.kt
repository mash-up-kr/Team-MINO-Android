@file:OptIn(ExperimentalTime::class)

package team.mino.core.data.repository.mapper

import team.mino.core.data.network.dto.request.RoomRequest
import team.mino.core.data.network.dto.response.RoomResponse
import team.mino.core.data.network.dto.response.RoomSummaryResponse
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomDraft
import team.mino.core.domain.model.RoomMemberSummary
import team.mino.core.domain.model.RoomThumbnail
import kotlin.time.ExperimentalTime

/**
 * 색의 서버 표현. 표의 소유자는 `docs/specs/group-room-form/contracts/room-api-mock.md` §2다.
 *
 * 서버가 색을 다른 표현(hex 등)으로 확정하면 고칠 곳은 이 표 하나다. 도메인·UI·mock 저장소 어디에도
 * 이 문자열이 새어 나가지 않는다. 열거 상수 이름에서 파생하지 않는 이유도 같다 — 도메인 이름이 바뀌었을 때
 * 서버 계약이 조용히 따라 바뀌면 안 된다.
 *
 * [RoomSummaryResponse.color]도 같은 표를 쓴다 — room-list와 group-room-form이 같은 서버 팔레트를
 * 공유한다는 전제다.
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
        // group-room-form의 mock 응답에는 목록 전용 집계 필드가 없다 — room-list 화면에 이 경로로
        // 만든 방이 나타나지 않으므로(RoomRepositoryImpl 참고) 플레이스홀더로 채워도 드러나지 않는다.
        isPersonal = false,
        placeCount = 0,
        thumbnail = RoomThumbnail.ColorAndCharacter(color = color),
        memberSummary = RoomMemberSummary(visibleAvatarUrls = emptyList(), overflowCount = 0),
        lastPlaceSavedAt = null,
        commentCount = 0,
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
private fun String?.toRoomColor(): RoomColor = COLORS_BY_IDENTIFIER[this] ?: RoomColor.GRAY

/**
 * `RoomSummaryResponse.toDomain()` — draft API가 아직 제공하지 않는 필드는 임시 목데이터/플레이스홀더로
 * 채운다(근거: docs/specs/room-list/research.md D12, contracts/room-repository.md 「구현 위치」).
 * 백엔드가 필드를 확정하면 이 매퍼만 교체한다.
 */
internal fun RoomSummaryResponse.toDomain(): Room =
    Room(
        id = id,
        name = name,
        description = description.orEmpty(),
        color = color.toRoomColor(),
        ownerId = ownerId,
        isPersonal = type == ROOM_TYPE_PERSONAL,
        placeCount = pinCount,
        thumbnail = toThumbnail(),
        memberSummary = toMemberSummary(),
        // draft에 없는 필드 — 백엔드 확정 전까지 플레이스홀더.
        lastPlaceSavedAt = null,
        commentCount = 0,
    )

private fun RoomSummaryResponse.toThumbnail(): RoomThumbnail =
    if (pinCount <= 0) {
        RoomThumbnail.ColorAndCharacter(color = color)
    } else {
        // draft API는 콜라주 이미지 URL을 제공하지 않는다 — 임시 플레이스홀더로 채운다.
        RoomThumbnail.Collage(
            imageUrls = List(pinCount.coerceIn(1, MAX_COLLAGE_IMAGE_COUNT)) { PLACEHOLDER_THUMBNAIL_IMAGE_URL },
        )
    }

private fun RoomSummaryResponse.toMemberSummary(): RoomMemberSummary {
    // draft API는 avatar { id: integer }만 제공하고 URL 매핑이 없다 — null로 채운다.
    return if (memberCount <= MAX_VISIBLE_AVATAR_COUNT) {
        RoomMemberSummary(
            visibleAvatarUrls = List(memberCount) { null },
            overflowCount = 0,
        )
    } else {
        RoomMemberSummary(
            visibleAvatarUrls = List(OVERFLOW_VISIBLE_AVATAR_COUNT) { null },
            overflowCount = memberCount - OVERFLOW_VISIBLE_AVATAR_COUNT,
        )
    }
}

private const val ROOM_TYPE_PERSONAL = "personal"
private const val MAX_COLLAGE_IMAGE_COUNT = 4
private const val MAX_VISIBLE_AVATAR_COUNT = 4
private const val OVERFLOW_VISIBLE_AVATAR_COUNT = 3
private const val PLACEHOLDER_THUMBNAIL_IMAGE_URL = "https://mino.app/placeholder/room-thumbnail.png"
