package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 홈 카드 덱 조회(`GET /api/v1/rooms/{roomId}/cards`) 응답의 카드 한 장.
 *
 * 홈 카드가 쓰는 필드만 둔다. 서버가 `roomId`·`createdAt`과 장소의 좌표·분류·전화번호 등을 함께 내려주지만
 * 카드 표시에 쓰지 않으므로 담지 않고 `ignoreUnknownKeys = true`가 흡수한다.
 *
 * [labelGroup]은 서버가 배정한 라벨 식별자 문자열(`worthVisiting`·`manySaves`·`manyComments`·`manyViews`)이다.
 * **홈은 판정에 관여하지 않고 값을 표시만 하며**, 도메인 `PlaceLabel`과의 대응은 `DeckMapper`만 안다.
 *
 * 계약은 `docs/specs/home-deck-exploration/contracts/deck-api.md` §2.2가 소유한다.
 */
@Serializable
internal data class CardResponse(
    val id: String,
    val place: CardPlaceResponse,
    val images: List<String> = emptyList(),
    val createdBy: CardCreatedByResponse? = null,
    val labelGroup: String,
)

/** [CardResponse]가 가리키는 장소. 카드 앞면이 쓰는 이름·주소만 담는다. */
@Serializable
internal data class CardPlaceResponse(
    val name: String,
    val address: String,
)

/**
 * 카드를 등록한 사람. 카드 헤더의 아바타와 닉네임에 쓴다.
 *
 * 계약상 [CardResponse.createdBy] 자체가 `null`일 수 있고(탈퇴 등), 아바타를 고르지 않은 사용자면
 * [avatar]도 `null`이다. 두 부재를 어떻게 메울지는 `DeckMapper`가 정한다.
 */
@Serializable
internal data class CardCreatedByResponse(
    val userId: String,
    val nickname: String,
    val avatar: CardAvatarResponse? = null,
)

/** 아바타 식별자 봉투. 서버 계약이 `Avatar { id: integer }` 객체이므로 정수 하나여도 타입을 둔다. */
@Serializable
internal data class CardAvatarResponse(
    val id: Int,
)
