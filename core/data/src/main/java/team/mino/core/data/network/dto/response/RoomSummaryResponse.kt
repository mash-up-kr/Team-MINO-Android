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

/**
 * `GET /api/v1/rooms/{roomId}/members` 응답 원소 — 초대 시트 참여자 목록·방장 위임 대상 선택이 공유한다
 * (`docs/specs/room-detail/contracts/place-repository.md` "RoomRepository 확장").
 *
 * 목록 요약([RoomSummaryResponse.users])이 쓰는 [RoomMemberResponse]와 이름은 비슷하지만 다른 응답이다 —
 * 저 쪽은 `id`·`avatar{id}`만 담는 카드 축약 표현이고, 이 쪽은 멤버 전체 목록 화면이 쓰는 상세 표현이다.
 */
@Serializable
data class RoomMemberDetailResponse(
    val userId: String,
    val nickname: String,
    val avatar: String? = null,
    val isOwner: Boolean,
    val joinedAt: String,
)

/** `POST /api/v1/rooms/{roomId}/invitations` 응답 — 초대 코드(6자 대문자+숫자)만 담는다. */
@Serializable
data class RoomInvitationResponse(
    val code: String,
)
