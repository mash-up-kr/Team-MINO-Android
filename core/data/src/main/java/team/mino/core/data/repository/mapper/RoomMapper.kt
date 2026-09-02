@file:OptIn(ExperimentalTime::class)

package team.mino.core.data.repository.mapper

import team.mino.core.data.network.dto.request.RoomRequest
import team.mino.core.data.network.dto.response.AvatarResponse
import team.mino.core.data.network.dto.response.RoomMemberDetailResponse
import team.mino.core.data.network.dto.response.RoomResponse
import team.mino.core.data.network.dto.response.RoomSummaryResponse
import team.mino.core.domain.model.ProfileAvatar
import team.mino.core.domain.model.Room
import team.mino.core.domain.model.RoomColor
import team.mino.core.domain.model.RoomDraft
import team.mino.core.domain.model.RoomMember
import team.mino.core.domain.model.RoomMemberSummary
import team.mino.core.domain.model.RoomThumbnail
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 색의 서버 표현. 표의 소유자는 `docs/specs/group-room-form/contracts/room-api.md` §2다.
 *
 * 서버가 색을 다른 표현으로 바꾸면 고칠 곳은 이 표 하나다. 도메인·UI 어디에도 이 문자열이 새어 나가지
 * 않는다. 열거 상수 이름에서 파생하지 않는 이유도 같다 — 도메인 이름이 바뀌었을 때 서버 계약이 조용히
 * 따라 바뀌면 안 된다.
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
        isPersonal = type == ROOM_TYPE_PERSONAL,
        // 단건 조회 응답에는 목록 전용 집계 필드(장소 수 등)가 없다 — room-list가 다시 채울 때까지 플레이스홀더.
        placeCount = 0,
        thumbnail = RoomThumbnail.ColorAndCharacter(color = color),
        memberSummary = RoomMemberSummary(visibleAvatars = emptyList(), overflowCount = 0),
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
internal fun String.toRoomColor(): RoomColor = COLORS_BY_IDENTIFIER[this] ?: RoomColor.GRAY

/**
 * 사용자 아바타의 색을 읽는다. **아바타가 없거나 표에 없는 색이면 `null`** — 받는 쪽이 기본 아바타로 그린다.
 *
 * 방의 색([toRoomColor])과 달리 [RoomColor.GRAY]로 메우지 않는다. 방은 색을 고르지 않아도 회색 방으로 보이지만,
 * 아바타는 「색을 모른다」와 「회색을 골랐다」가 다른 그림이기 때문이다. `gray`는 표에 있으므로 여기서도
 * `null`이 아니라 [RoomColor.GRAY]로 읽힌다 — `null`은 아바타가 없거나 팔레트 밖 값일 때만이다.
 *
 * 장소 상세와 코멘트가 같은 13색 팔레트를 공유하는데 **서버 두 자리의 `enum` 제약이 서로 어긋나 있어**
 * (한쪽만 enum) 모르는 값이 실려 올 수 있다. 그 방어를 매퍼마다 되풀이하지 않도록 색 표가 있는 이 자리에
 * 함께 둔다 — `docs/specs/place-detail/contracts/place-api.md` §1.3.
 */
internal fun AvatarResponse?.toRoomColorOrNull(): RoomColor? = this?.let { COLORS_BY_IDENTIFIER[it.color] }

/**
 * room-list 전용 — [RoomSummaryResponse]를 (`RoomSummaryMapper`가 방 선택 시트를 위해 읽는 얕은
 * [team.mino.core.domain.model.RoomSummary]가 아니라) room-list 화면이 쓰는 풍부한 [Room]으로 읽는다.
 * 두 매퍼가 같은 응답 DTO에 같은 이름(`toDomain`)의 확장 함수를 갖게 되는 충돌을 피하려고 이름을 다르게
 * 뒀다.
 *
 * 이 응답에는 멤버 아바타가 없다 — [memberSummary]는 빈 값으로 두고, 실제 아바타는 room-list 화면이
 * `GET /rooms/{roomId}/members`로 방마다 따로 채운다(`RoomListViewModel.loadRoomMembers`).
 */
internal fun RoomSummaryResponse.toRoomListDomain(): Room =
    Room(
        id = id,
        name = name,
        description = description.orEmpty(),
        color = color.toRoomColor(),
        ownerId = ownerId,
        isPersonal = type == ROOM_TYPE_PERSONAL,
        placeCount = pinCount,
        thumbnail = toThumbnail(),
        memberSummary = RoomMemberSummary(visibleAvatars = emptyList(), overflowCount = 0),
        // 목록 응답에 없는 필드 — 서버가 확정하면 채운다.
        lastPlaceSavedAt = null,
        commentCount = 0,
    )

private fun RoomSummaryResponse.toThumbnail(): RoomThumbnail {
    val imageUrls = thumbnailList.filter { it.startsWith("http://") || it.startsWith("https://") }
    return if (imageUrls.isEmpty()) {
        RoomThumbnail.ColorAndCharacter(color = color)
    } else {
        RoomThumbnail.Collage(imageUrls = imageUrls.take(MAX_COLLAGE_IMAGE_COUNT))
    }
}

/** `GET /api/v1/rooms/{roomId}/members` 응답 원소 → 도메인. */
internal fun RoomMemberDetailResponse.toDomain(): RoomMember =
    RoomMember(
        userId = userId,
        nickname = nickname,
        avatar = avatar?.let { AVATARS_BY_COLOR[it.color] } ?: ProfileAvatar.Default,
        isOwner = isOwner,
        joinedAt = Instant.parse(joinedAt),
    )

private const val ROOM_TYPE_PERSONAL = "personal"
private const val MAX_COLLAGE_IMAGE_COUNT = 4
