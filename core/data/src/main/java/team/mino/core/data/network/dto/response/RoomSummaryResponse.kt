package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 방 목록 조회(`GET /api/v1/rooms`) 응답의 방 한 건.
 *
 * 방 선택 시트(`RoomSummaryMapper`)와 room-list(`RoomMapper`)가 같은 응답을 서로 다른 도메인 모델로
 * 읽는다 — 시트는 [id]·[name]·[description]·[type]·[color]·[pinCount]·[thumbnailList]만, room-list는
 * 여기에 [ownerId]·[memberCount]까지 함께 쓴다. `?showUsers=true`가 더해 주는 `users`는 어느 쪽 소비자도
 * 쓰지 않아 담지 않고 `ignoreUnknownKeys = true`가 흡수한다.
 *
 * [hasPlace]·[matchedPinId]는 **`?showHasPlaceId=`를 지정했을 때만 서버가 싣는다.** 지정하지 않은 응답에서
 * 파싱이 깨지지 않도록 둘 다 기본값 `null`을 두며, 그 `null`은 "저장돼 있지 않다"가 아니라 "물어보지 않았다"다.
 * [matchedPinId]는 스키마상 nullable로 표시돼 있지 않지만 [hasPlace]가 `true`가 아닌 방에서는 의미가 없으므로
 * 여기서도 nullable로 받고, 버리는 판정은 `RoomSummaryMapper`가 한다
 * (`docs/specs/place-detail/contracts/place-api.md` §4·§4.2).
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
    val hasPlace: Boolean? = null,
    val matchedPinId: String? = null,
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
