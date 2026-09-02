package team.mino.core.domain.model

import team.mino.core.common.kotlin.geo.GeoPoint

/**
 * 장소 상세 화면이 그리는 핀 하나.
 *
 * **핀은 (장소, 방) 쌍이다.** 같은 장소라도 방이 다르면 다른 핀이고, 따라서 [pinId]와 [placeId]가 모두 필요하다 —
 * [pinId]는 모든 서버 호출의 키이고, [placeId]는 「다른방에 공유」 시트가 방마다 이미 저장돼 있는지를 묻는 키다
 * (`docs/specs/place-detail/research.md` D4·D9).
 *
 * 이름이 `Place`가 아닌 이유는 room-detail이 다른 브랜치에서 목록용 `Place`를 신설하기 때문이다. 이름을 갈라 두어야
 * 두 타입이 머지 시점에 충돌하지 않는다 — 같은 문서 D8.
 *
 * **방 대표 색을 담지 않는다.** 핀 상세 응답에 없어 이 타입이 만들어낼 수 없는 값이다. 마커 색은 화면이 방 목록에서
 * [roomId]로 찾아 든다 — `docs/specs/place-detail/contracts/place-detail-main-contract.md` §5.1. [roomId]는
 * 「지금 보고 있는 방」(FR-027)이자 그 조회의 키다.
 *
 * 카테고리·저장 경과일은 서버가 주더라도 담지 않는다. 장소 상세 어디에도 노출하지 않기로 한 값이다(FR-005·spec §3.2).
 *
 * [imageUrls]가 비면 캐러셀 영역 자체가 사라진다(EC-009). [registrant]가 `null`이면 기본 아바타를 그리고(EC-004),
 * [sourceUrl]이 `null`이면 [원문보기]를 비활성으로 둔다(EC-017).
 */
data class PlaceDetail(
    val pinId: String,
    val roomId: String,
    val placeId: String,
    val name: String,
    val address: String,
    val location: GeoPoint,
    val imageUrls: List<String>,
    val registrant: PlaceRegistrant?,
    val sourceUrl: String?,
    val mapUrl: String?,
)

/**
 * 장소를 등록한 사용자.
 *
 * [avatarColor]는 서버가 `avatar: { color }`로 주며, 같은 13색 팔레트라 [RoomColor]를 재사용한다. 같은 사용자
 * 아바타를 엔드포인트마다 다르게 표현하는 서버 계약의 불일치는 협의 항목으로 남아 있다 —
 * `docs/specs/place-detail/contracts/place-api.md` §4.
 */
data class PlaceRegistrant(
    val userId: String,
    val nickname: String,
    val avatarColor: RoomColor?,
)
