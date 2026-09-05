package team.mino.core.data.repository.mapper

import team.mino.core.data.network.dto.response.RoomSummaryResponse
import team.mino.core.domain.model.RoomSummary
import team.mino.core.domain.model.RoomType

/**
 * 개인방의 서버 표현. 표의 소유자는 `docs/specs/shared-link-receiver/contracts/room-list-api.md` §1.1이다.
 *
 * 공동방(`"shared"`)을 짝으로 두지 않는 이유는 아래 [toRoomType]에 적었다.
 *
 * `internal`인 이유 — `HomeDeckRepositoryImpl.getRoomSummaries`가 개인방을 최상단에 고정하는 순회 순서를
 * 확정할 때도 같은 식별자로 판정해야 하므로(`docs/specs/home-deck-exploration/contracts/deck-api.md` §3.1),
 * 이 파일 하나에 갇혀 있으면 안 된다.
 */
internal const val PERSONAL_TYPE_IDENTIFIER = "personal"

/**
 * 썸네일 콜라주가 그릴 수 있는 최대 장수.
 *
 * 이 상한을 여기서 확정하기 때문에 콜라주 컴포넌트가 5장 이상 분기를 두지 않는다. 서버가 계약(0~4장)을 넘겨
 * 내려주더라도 도메인 밖으로 새어 나가지 않아야 한다.
 */
private const val MAX_THUMBNAIL_COUNT = 4

/**
 * 썸네일 원소가 이미지 URL인지 가르는 기준.
 *
 * `thumbnailList`는 URL 목록이거나 색상 키 1개이며, 색상 키는 `red`·`gray` 같은 소문자 단어라 스킴을 갖지
 * 않는다. 그래서 스킴 하나로 갈린다(`docs/specs/shared-link-receiver/contracts/room-list-api.md` §2).
 */
private val IMAGE_URL_SCHEMES = listOf("http://", "https://")

/**
 * 방 목록 응답 한 건을 [RoomSummary]로 읽는다.
 *
 * 서버가 준 값이 계약을 벗어나도 **던지지 않고 흡수한다** — 방 하나의 값이 어긋났다는 이유로 목록 전체가
 * 실패하면 안 되기 때문이다. 그래서 [RoomSummary]에는 검증이 없고, 규칙은 전부 이 파일이 집행한다
 * (`docs/specs/shared-link-receiver/data-model.md` §1.2).
 *
 * 순서는 건드리지 않는다. 개인방 최상단 고정은 `GetRoomPickerRoomsUseCase`의 몫이다.
 *
 * `hasPlace`는 **`null`을 `false`로 메우지 않는다.** `?showHasPlaceId=`를 지정하지 않은 조회에서 서버가 이
 * 필드를 아예 싣지 않으므로, 메우면 "물어보지 않았다"가 "저장돼 있지 않다"로 둔갑한다
 * (`docs/specs/place-detail/data-model.md` §3).
 *
 * `matchedPinId`는 반대로 **`hasPlace`가 `true`가 아니면 지운다.** 서버 스키마가 이 필드를 nullable로 표시하지
 * 않아 저장돼 있지 않은 방에도 값이 실려 올 수 있는데, 그대로 올리면 화면이 그것을 전환 대상으로 삼는다
 * (`docs/specs/place-detail/contracts/place-api.md` §4.2). 규칙을 여기서 집행하므로 도메인에서는
 * `matchedPinId != null`이 곧 `hasPlace == true`를 뜻한다.
 */
internal fun RoomSummaryResponse.toDomain(): RoomSummary =
    RoomSummary(
        id = id,
        name = name,
        description = description.orEmpty(),
        type = type.toRoomType(),
        color = color.toRoomColor(),
        placeCount = pinCount,
        thumbnailImageUrls = thumbnailList.filter(String::isImageUrl).take(MAX_THUMBNAIL_COUNT),
        hasPlace = hasPlace,
        matchedPinId = matchedPinId?.takeIf { hasPlace == true },
    )

/**
 * 개인방이 아닌 모든 문자열은 [RoomType.GROUP]으로 읽는다. 아는 갈래를 늘어놓고 나머지를 실패로 두지 않는 것은,
 * 이 값이 판정하는 것이 **최상단에 고정할 방인가** 하나뿐이기 때문이다. 개인방은 사용자당 하나뿐이라
 * 서버가 새 갈래를 더하더라도 고정 대상이 될 수 없다.
 */
private fun String.toRoomType(): RoomType = if (this == PERSONAL_TYPE_IDENTIFIER) RoomType.PERSONAL else RoomType.GROUP

/**
 * 색상 키를 버리고 이미지 URL만 남긴다.
 *
 * 색상 키를 버려도 정보가 사라지지 않는다 — 같은 내용이 `color`에 있고 폴백은 그 값으로 그려진다
 * (`docs/specs/shared-link-receiver/research.md` R-019·R-022). 전부 걸러지면 빈 목록이 되고,
 * 빈 목록은 이미 폴백 경로다.
 */
private fun String.isImageUrl(): Boolean = IMAGE_URL_SCHEMES.any { startsWith(it, ignoreCase = true) }
