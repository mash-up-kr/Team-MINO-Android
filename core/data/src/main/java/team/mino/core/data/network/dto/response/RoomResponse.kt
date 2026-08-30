package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 방 조회·생성·수정 응답이 공통으로 담는 방 표현.
 *
 * 폼이 쓰는 필드만 둔다. 서버가 초대 코드·생성 시각 등을 더 내려줘도 `ignoreUnknownKeys = true`가 흡수하므로
 * 파싱이 깨지지 않는다.
 *
 * [color]는 색 식별자 문자열이다. 이 문자열과 도메인 색의 대응은 `RoomMapper`만 안다.
 *
 * [type]은 `docs/specs/group-room-form/contracts/room-api.md` "`GET /rooms/{roomId}`만 `pinCount`·
 * `memberCount` 두 필드를 더 내려준다"가 "이 feature는 셋 다(`type`·`createdAt`·집계 수) 읽지 않는다"고
 * 남긴 대로 원래 이 DTO엔 없었다 — 하지만 `room-detail`이 같은 `GET /rooms/{roomId}`·같은 [RoomResponse]를
 * 재사용해 방장이 아닌 개인방(`isPersonal`) 판정에 이 값이 필요해졌다. 안 읽으면 방 상세는 어떤 방이든
 * 항상 공동방으로 잘못 판정한다(실기기 확인된 결함 — 개인방 헤더에 초대·더보기 버튼이 계속 남아있었다).
 */
@Serializable
internal data class RoomResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val color: String,
    val ownerId: String,
    val type: String? = null,
)
