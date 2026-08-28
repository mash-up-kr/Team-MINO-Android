package team.mino.core.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/pins`·`GET /api/v1/pins/{pinId}`(`Pin`) 응답 DTO.
 *
 * 배포된 서버 API(`https://api.gguk.org`, `GET /api-docs-json`, 근거: docs/specs/room-detail/research.md D14)
 * 기준 필드만 표현한다. `commentCount`·`isGgukPick`은 서버 응답에 없어 이 DTO에 포함하지 않고,
 * `repository/mapper/PlaceMapper.kt`에서 임시 목데이터/플레이스홀더로 채운다.
 *
 * [id]는 핀 식별자다. 도메인 `Place.id`도 이 값을 그대로 쓴다(`Pin.place.id`가 아니다) —
 * 근거: docs/specs/room-detail/data-model.md §1.
 *
 * [createdBy]는 실기기 확인 결과 문자열이 아니라 `{ "userId": ... }` 객체다 — 도메인 어디도 이 값을
 * 쓰지 않아 필드 자체를 지울 수도 있지만, 서버 응답 스키마를 그대로 반영해 두면 나중에 작성자 표시가
 * 필요해졌을 때 이 DTO만 보고 바로 알 수 있다.
 */
@Serializable
internal data class PinResponse(
    val id: String,
    val roomId: String,
    val place: PlaceResponse,
    val images: List<String>,
    val createdBy: PinCreatedByResponse,
    val createdAt: String,
)

/** [PinResponse.createdBy]의 서버 표현. */
@Serializable
internal data class PinCreatedByResponse(
    val userId: String,
)

/**
 * `Pin.place` 서버 스키마 — 장소 자체의 표현.
 *
 * 방 상세 카드 렌더링에 쓰는 필드(`name`·`address`·`category`)만 도메인 `Place`로 매핑되고,
 * 나머지 필드는 이번 spec 범위 밖([SCR-006] 장소 상세)이라 DTO에는 남기되 매핑하지 않는다.
 */
@Serializable
internal data class PlaceResponse(
    val id: String,
    val provider: String,
    val providerPlaceId: String,
    val name: String,
    val address: String,
    val city: String? = null,
    val district: String? = null,
    val lat: Double,
    val lng: Double,
    val category: String? = null,
    val phone: String? = null,
    val mapUrl: String? = null,
    val createdAt: String,
    val updatedAt: String,
)
