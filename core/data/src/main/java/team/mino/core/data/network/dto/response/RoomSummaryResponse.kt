package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 방 목록 조회(`GET /api/v1/rooms`) 응답의 방 한 건.
 *
 * 방 선택 시트가 쓰는 필드만 둔다. 서버가 `ownerId`·`createdAt`·`memberCount`를 함께 내려주지만
 * 시트가 쓰지 않으므로 담지 않고 `ignoreUnknownKeys = true`가 흡수한다.
 *
 * [type]·[color]는 서버가 준 식별자 문자열이다. 도메인 값과의 대응은 `RoomSummaryMapper`만 안다.
 *
 * [thumbnailList]는 **두 가지를 담는다** — 최근 핀의 대표 이미지 URL 목록이거나, 저장된 핀이 없을 때의
 * 방장 아바타 색상 키 1개다. 그 판정과 폐기는 `RoomSummaryMapper`가 하며 도메인에는 URL만 올라간다
 * (`docs/specs/shared-link-receiver/research.md` R-022). 필드가 없는 응답에서도 파싱이 깨지지 않도록
 * 기본값을 둔다.
 *
 * 계약은 `docs/specs/shared-link-receiver/contracts/room-list-api.md` §1·§2가 소유한다.
 */
@Serializable
internal data class RoomSummaryResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val type: String,
    val color: String,
    val pinCount: Int,
    val thumbnailList: List<String> = emptyList(),
)
