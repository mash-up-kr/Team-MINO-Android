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
        thumbnailUrls = images,
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
 *
 * **정확히 일치가 아니라 포함 여부로 묶는다.** `place.category`는 `GET /pins`의 `category` 쿼리 값
 * (`cafe`/`restaurant`, 상위 그룹)이 아니라 카카오 장소 API의 계층형 카테고리 경로 원문(예:
 * `"음식점 > 카페 > 커피전문점"`, `"음식점 > 카페"`, 실서버 응답 확인 2026-09-04)으로 내려온다. 최상위가
 * `음식점`이어도 그 아래 `카페`가 있으면 실제로는 카페라, 최상위 세그먼트만 비교하면(구 구현) 카페 필터가
 * 전부 [PlaceCategoryFilter.ALL]로 새 버려 카페·음식점 칩을 눌러도 아무 핀도 안 남는 결함이 났다. `카페`
 * 포함 여부를 `음식점`보다 먼저 검사하는 순서가 이 중첩 구조를 반영한다.
 */
private fun String?.toPlaceCategoryFilter(): PlaceCategoryFilter {
    val category = this?.trim()?.lowercase() ?: return PlaceCategoryFilter.ALL
    return when {
        CafeKeywords.any { category.contains(it) } -> PlaceCategoryFilter.CAFE
        RestaurantKeywords.any { category.contains(it) } -> PlaceCategoryFilter.RESTAURANT
        else -> PlaceCategoryFilter.ALL
    }
}

private val CafeKeywords = listOf("카페", "cafe", "디저트", "베이커리", "커피전문점")
private val RestaurantKeywords = listOf("음식점", "restaurant", "식당")
