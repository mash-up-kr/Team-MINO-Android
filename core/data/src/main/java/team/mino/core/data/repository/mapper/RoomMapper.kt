package team.mino.core.data.repository.mapper

import team.mino.core.data.network.dto.response.RoomSummaryResponse
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomMemberSummary
import team.mino.core.domain.model.RoomThumbnail
import kotlin.time.ExperimentalTime

/**
 * `RoomSummaryResponse.toDomain()` — draft API가 아직 제공하지 않는 필드는 임시 목데이터/플레이스홀더로
 * 채운다(근거: docs/specs/room-list/research.md D12, contracts/room-repository.md 「구현 위치」).
 * 백엔드가 필드를 확정하면 이 매퍼만 교체한다.
 */
@OptIn(ExperimentalTime::class)
internal fun RoomSummaryResponse.toDomain(): Room =
    Room(
        id = id,
        name = name,
        description = description,
        color = color,
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
