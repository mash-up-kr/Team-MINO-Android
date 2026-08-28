package team.mino.core.data.repository.mapper

import team.mino.core.data.network.dto.response.CardCreatedByResponse
import team.mino.core.data.network.dto.response.CardResponse
import team.mino.core.domain.model.PlaceCard
import team.mino.core.domain.model.PlaceLabel
import team.mino.core.domain.model.Registrant

/**
 * 라벨의 서버 표현. 표의 소유자는 `docs/specs/home-deck-exploration/contracts/deck-api.md` §2.2다.
 *
 * `place.category`(`카페`·`음식점` 축)와 섞지 않는다. 값 체계가 다르고, 섞으면 틀린 라벨이 화면에 뜬다
 * (research.md R-002).
 */
private val LABELS_BY_IDENTIFIER: Map<String, PlaceLabel> =
    mapOf(
        "worthVisiting" to PlaceLabel.WORTH_VISITING,
        "manySaves" to PlaceLabel.MANY_SAVES,
        "manyComments" to PlaceLabel.MANY_COMMENTS,
        "manyViews" to PlaceLabel.MANY_VIEWS,
    )

/**
 * 카드 한 장을 [PlaceCard]로 읽는다.
 *
 * 서버 값이 계약을 벗어나도 **던지지 않고 흡수한다** — 카드 한 장 때문에 덱 전체가 실패하면 안 된다.
 * `RoomSummaryMapper`와 같은 규칙이며, 그래서 [PlaceCard]에는 검증이 없고 규칙은 전부 이 파일이 집행한다.
 *
 * 순서도 장수도 건드리지 않는다. 응답 순서가 곧 노출 순위이고 10장 절단은 서버가 이미 했다(계약 §2.3).
 */
internal fun CardResponse.toDomain(): PlaceCard =
    PlaceCard(
        pinId = id,
        placeName = place.name,
        address = place.address,
        imageUrls = images,
        label = labelGroup.toPlaceLabel(),
        registrant = createdBy.toRegistrant(),
    )

/**
 * 모르는 식별자는 [PlaceLabel.WORTH_VISITING]으로 읽는다.
 *
 * 임의의 폴백이 아니다 — 서버가 정원을 채우지 못한 분량을 흡수시키는 라벨이 바로 이것이고(계약 §2.4),
 * FR-008이 **라벨은 항상 존재한다**고 못박았으므로 "라벨 없음"으로 떨어뜨릴 자리가 없다.
 * 서버가 라벨을 늘렸다는 이유로 덱 조회가 실패해서도 안 된다.
 */
private fun String.toPlaceLabel(): PlaceLabel = LABELS_BY_IDENTIFIER[this] ?: PlaceLabel.WORTH_VISITING

/**
 * 등록자가 없는 카드(탈퇴 등)는 빈 [Registrant]로 읽는다.
 *
 * 여기서 「알 수 없음」 같은 문구를 채우지 않는 이유는 그것이 표시 문구이기 때문이다. 빈 값으로 무엇을 그릴지는
 * feature가 정한다 — `RoomSummary.description`이 `null`을 빈 문자열로 흡수하는 것과 같은 규칙이다.
 *
 * [CardCreatedByResponse.avatar]가 `null`인 것은 결손이 아니라 **아바타를 고르지 않은 상태**이므로
 * [Registrant.avatarId]에 `null`을 그대로 싣는다.
 */
private fun CardCreatedByResponse?.toRegistrant(): Registrant =
    Registrant(
        userId = this?.userId.orEmpty(),
        nickname = this?.nickname.orEmpty(),
        avatarId = this?.avatar?.id,
    )
