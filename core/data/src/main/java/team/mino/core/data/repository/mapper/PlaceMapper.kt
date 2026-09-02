@file:OptIn(ExperimentalTime::class)

package team.mino.core.data.repository.mapper

import team.mino.core.common.kotlin.geo.GeoPoint
import team.mino.core.data.network.dto.response.PinResponse
import team.mino.core.domain.model.Place
import team.mino.core.domain.model.PlaceCategoryFilter
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * `PinResponse.toDomain()` — `Pin`과 그 안의 `place`를 합쳐 도메인 `Place`로 변환한다.
 *
 * **식별자를 둘 나른다** — [Place.id]는 `Pin.id`(공유·삭제가 쓰는 값), [Place.placeId]는 `Pin.place.id`
 * (어느 방에 이미 담겼는지 묻는 값)다. 근거: docs/specs/room-detail/data-model.md §1.
 *
 * [Place.commentCount]·[Place.isGgukPick]은 서버 응답에 없어 임시 목데이터/플레이스홀더로 채운다
 * (근거: docs/specs/room-detail/data-model.md §4 `[TBD]`, contracts/place-repository.md "DTO 갭 대응").
 * 백엔드가 필드를 확정하면 이 매퍼만 교체한다.
 *
 * [Place.distanceMeters]는 클라이언트 위치 계산이 필요해 이 매퍼 책임 밖이다 — 항상 `null`로 둔다.
 */
internal fun PinResponse.toDomain(): Place =
    Place(
        id = id,
        placeId = place.id,
        name = place.name,
        address = place.address,
        category = place.category.toPlaceCategoryFilter(),
        thumbnailUrl = images.firstOrNull(),
        savedAt = Instant.parse(createdAt),
        // 서버 미노출 필드 — 백엔드 확정 전까지 플레이스홀더.
        commentCount = 0,
        isGgukPick = false,
        // 클라이언트 위치 계산이 필요한 필드 — 이 매퍼 책임 밖.
        distanceMeters = null,
        location = GeoPoint(latitude = place.lat, longitude = place.lng),
    )

/**
 * 아는 값이 아니면 [PlaceCategoryFilter.ALL]로 읽는다. 서버가 카테고리를 넓혀도 목록 조회가 실패하면 안 되고,
 * `ALL`은 필터 전용이라 개별 장소 카드가 이 값으로 보여도 렌더링이 깨지지 않는다.
 */
private fun String?.toPlaceCategoryFilter(): PlaceCategoryFilter =
    when (this?.lowercase()) {
        "cafe" -> PlaceCategoryFilter.CAFE
        "restaurant" -> PlaceCategoryFilter.RESTAURANT
        else -> PlaceCategoryFilter.ALL
    }
