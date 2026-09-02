package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 홈 카드 덱 조회(`GET /api/v1/rooms/{roomId}/cards`)에서 봉투 `data`가 담는 객체.
 *
 * 서버가 `room`(홈 헤더용 방 메타)을 함께 실으면서 카드가 `cards` 아래로 들어갔다. 홈은 방 메타를
 * `GET /api/v1/rooms`로 이미 받고 있어 여기서는 쓰지 않으므로 [cards]만 담고 `room`은
 * `ignoreUnknownKeys = true`가 흡수한다.
 *
 * 봉투는 여전히 `MinoResponse` 하나다 — 이 타입은 봉투가 아니라 그 안의 페이로드다.
 * 계약은 `docs/specs/home-deck-exploration/contracts/deck-api.md` §2.2가 소유한다.
 */
@Serializable
internal data class CardFeedResponse(
    val cards: List<CardResponse> = emptyList(),
)

/**
 * 홈 카드 덱 조회 응답의 카드 한 장.
 *
 * 홈 카드가 쓰는 필드만 둔다. 서버가 `roomId`·`createdAt`과 장소의 좌표·분류·전화번호 등을 함께 내려주지만
 * 카드 표시에 쓰지 않으므로 담지 않고 `ignoreUnknownKeys = true`가 흡수한다.
 *
 * [labelGroup]은 서버가 배정한 라벨 식별자 문자열(`worthVisiting`·`manySaves`·`manyComments`·`manyViews`)이다.
 * **홈은 판정에 관여하지 않고 값을 표시만 하며**, 도메인 `PlaceLabel`과의 대응은 `DeckMapper`만 안다.
 *
 * 계약은 `docs/specs/home-deck-exploration/contracts/deck-api.md` §2.2가 소유한다.
 *
 * **모든 필드에 기본값을 둔다.** 배포된 스키마에 `required`가 없어 한 필드라도 빠지면 직렬화가 실패하는데,
 * 그 실패는 카드 한 장이 아니라 **덱 전체**를 떨어뜨린다. 서버 값이 계약을 벗어나도 흡수한다는 `DeckMapper`의
 * 규칙을 응답을 읽는 이 자리까지 이어 둔 것이다. 값이 비어 그려질 것이 없는 카드가 될 수는 있어도, 그것 때문에
 * 홈이 오류 화면이 되지는 않는다.
 */
@Serializable
internal data class CardResponse(
    val id: String = "",
    val place: CardPlaceResponse = CardPlaceResponse(),
    val images: List<String> = emptyList(),
    val createdBy: CardCreatedByResponse? = null,
    val labelGroup: String = "",
)

/** [CardResponse]가 가리키는 장소. 카드 앞면이 쓰는 이름·주소만 담는다. */
@Serializable
internal data class CardPlaceResponse(
    val name: String = "",
    val address: String = "",
)

/**
 * 카드를 등록한 사람. 카드 헤더의 아바타와 닉네임에 쓴다.
 *
 * 계약상 [CardResponse.createdBy] 자체가 `null`일 수 있고(탈퇴 등), 아바타를 고르지 않은 사용자면
 * [avatar]도 `null`이다. 두 부재를 어떻게 메울지는 `DeckMapper`가 정한다.
 *
 * **아바타는 프로필과 같은 [AvatarResponse]다.** 색 하나로 표현하며, 아바타를 고르지 않은 사용자면 객체가
 * 비어 온다.
 */
@Serializable
internal data class CardCreatedByResponse(
    val userId: String = "",
    val nickname: String = "",
    val avatar: AvatarResponse? = null,
)
