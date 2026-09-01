package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 방 목록 조회(`GET /api/v1/rooms`) 응답의 방 한 건.
 *
 * 방 선택 시트(`RoomSummaryMapper`)와 room-list(`RoomMapper`)가 같은 응답을 서로 다른 도메인 모델로
 * 읽는다 — 시트는 [id]·[name]·[description]·[type]·[color]·[pinCount]·[thumbnailList]만, room-list는
 * 여기에 [ownerId]·[memberCount]까지 함께 쓴다. 서버가 `hasPlace`·`users`도 함께 내려줄 수 있지만
 * (`?showHasPlaceId=`·`?showUsers=true` 지정 시) 어느 쪽 소비자도 쓰지 않아 담지 않고
 * `ignoreUnknownKeys = true`가 흡수한다.
 *
 * [type]·[color]는 서버가 준 식별자 문자열이다. 도메인 값과의 대응은 각 소비자의 매퍼만 안다.
 *
 * [thumbnailList]는 **두 가지를 담는다** — 최근 핀의 대표 이미지 URL 목록이거나, 저장된 핀이 없을 때의
 * 방장 아바타 색상 키 1개다. 그 판정과 폐기는 각 소비자의 매퍼가 하며 도메인에는 URL만 올라간다
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
    val ownerId: String,
    val pinCount: Int,
    val memberCount: Int,
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
internal data class RoomMemberDetailResponse(
    val userId: String,
    val nickname: String,
    val avatar: AvatarResponse? = null,
    val isOwner: Boolean,
    val joinedAt: String,
)

/** `POST /api/v1/rooms/{roomId}/invitations` 응답 — 초대 코드(6자 대문자+숫자)만 담는다. */
@Serializable
data class RoomInvitationResponse(
    val code: String,
)
