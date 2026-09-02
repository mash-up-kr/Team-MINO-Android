package team.mino.core.data.repository.mapper

import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.data.network.dto.response.PinDetailCreatedByResponse
import team.mino.core.data.network.dto.response.PinDetailResponse
import team.mino.core.domain.model.PlaceDetail
import team.mino.core.domain.model.PlaceRegistrant

/**
 * 핀 상세 응답을 장소 상세 화면의 도메인 모델로 읽는다.
 *
 * **두 식별자를 서로 다른 자리에서 든다** — [PlaceDetail.pinId]는 응답의 `id`(모든 서버 호출의 키),
 * [PlaceDetail.placeId]는 `place.id`(`?showHasPlaceId=` 질의의 키)다. 핀은 (장소, 방) 쌍이라 둘이 다르며
 * 섞으면 「다른방에 공유」와 [저장된 방]이 엉뚱한 방을 묻는다(`docs/specs/place-detail/data-model.md` §1).
 *
 * **`roomId`를 함께 옮긴다.** 탭 간 진입이 어느 방을 보고 있는지 해석하는 근거이며(FR-027,
 * `contracts/place-detail-entry.md` §3.4), 이 응답 말고는 그 값의 출처가 없다.
 *
 * **서버가 주는 것을 다 담지 않는다** — `provider`·`providerPlaceId`·`city`·`district`·`category`·`phone`·
 * `createdAt`·`updatedAt`은 장소 상세 어디에도 노출하지 않기로 한 값이라 도메인으로 올리지 않는다
 * (`contracts/place-api.md` §1.2). DTO가 그 필드를 들고 있는 것은 서버 응답의 거울이기 때문이고, 무엇을
 * 올릴지 정하는 자리가 여기다.
 *
 * **비어 있음을 메우지 않는다.** `images`가 비면 빈 목록 그대로, `sourceUrl`·`mapUrl`이 `null`이면 그대로
 * `null`이다 — 캐러셀 영역을 지울지(EC-009), [원문보기]를 비활성으로 둘지(EC-017)는 화면의 판정이다.
 *
 * `createdAt`을 읽지 않아 [PlaceMapper]와 달리 `Instant` 변환이 없다 — 저장 경과일을 표시하지 않기 때문이다
 * (spec §3.2).
 */
internal fun PinDetailResponse.toDomain(): PlaceDetail =
    PlaceDetail(
        pinId = id,
        roomId = roomId,
        placeId = place.id,
        name = place.name,
        address = place.address,
        location = GeoPoint(latitude = place.lat, longitude = place.lng),
        imageUrls = images,
        registrant = createdBy?.toDomain(),
        sourceUrl = sourceUrl,
        mapUrl = place.mapUrl,
    )

/**
 * 등록자를 도메인으로 읽는다. **등록자가 없으면 이 함수에 닿지 않고 `registrant`가 `null`로 남는다** — 기본
 * 아바타 대체는 화면이 판정하므로 매퍼가 빈 등록자를 지어내지 않는다(EC-004).
 *
 * **유저 식별자 키가 코멘트 쪽과 어긋나는 것을 여기서 흡수한다** — 핀 상세 응답은 `createdBy.userId`,
 * 코멘트 응답은 `author.id`인데 도메인은 둘 다 `userId`다(`contracts/place-api.md` §1.3).
 *
 * 아바타 색은 [toRoomColorOrNull]이 13색 팔레트로 읽고 모르는 값은 `null`로 떨어진다 — 이 엔드포인트에는
 * `enum` 제약이 없어 팔레트 밖 값이 실려 올 수 있다(같은 절). 코멘트 쪽과 같은 헬퍼를 쓴다.
 */
private fun PinDetailCreatedByResponse.toDomain(): PlaceRegistrant =
    PlaceRegistrant(
        userId = userId,
        nickname = nickname,
        avatarColor = avatar.toRoomColorOrNull(),
    )
